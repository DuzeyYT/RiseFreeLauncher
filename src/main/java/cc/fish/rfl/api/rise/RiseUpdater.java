package cc.fish.rfl.api.rise;

import cc.fish.rfl.api.utils.DownloadUtil;
import cc.fish.rfl.api.utils.OsUtil;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.OperatingSystemMXBean;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@UtilityClass
public class RiseUpdater {
    public final Logger LOGGER = LogManager.getLogger("Rise Updater");

    public JSONObject URLS;

    static {
        try {
            URLS = new JSONObject(FileUtils.readFileToString(new File("urls.json")));
        } catch (IOException e) {
            LOGGER.info("Failed to read urls.json, using default URLs");

            URLS = new JSONObject();
            URLS.put("version", "https://raw.githubusercontent.com/risellc/LatestRiseVersion/main/Version");
            URLS.put("client", "https://raw.githubusercontent.com/AlanW5/rise_update/refs/heads/main/Standalone.jar");
            URLS.put("client-hash", "https://raw.githubusercontent.com/AlanW5/rise_update/refs/heads/main/Standalone_Hash.txt");
            URLS.put("libraries", "https://raw.githubusercontent.com/AlanW5/rise_update/refs/heads/main/Libraries.jar");
            URLS.put("libraries-hash", "https://raw.githubusercontent.com/AlanW5/rise_update/refs/heads/main/Libraries_Hash.txt");
        }
    }

    public final String CLIENT_PATH = "files/client.jar";
    public final String NATIVE_PATH = "files/rise-natives";
    public final String LIBRARY_PATH = "files/libraries.jar";
    public final String COMPRESSED_PATH = "files/compressed.jar";
    public final String AGENT_PATH = "agent.jar";

    public void checkAndUpdate(boolean noUpdate) {
        LOGGER.info("Checking for client updates...");
        String latestVersion = DownloadUtil.readFromWeb(URLS.getString("version"));
        LOGGER.info("(Latest version: {})", latestVersion);

        File agentFile = new File(AGENT_PATH);
        if (!agentFile.exists()) {
            LOGGER.error(
                    "Agent file was not found. Please download it from the Github repository.");
            System.exit(1);
        }

        try {
            if (!noUpdate) {
                String clientHash = DownloadUtil.readFromWeb(URLS.getString("client-hash"));
                String libraryHash = DownloadUtil.readFromWeb(URLS.getString("libraries-hash"));

                if (clientHash == null || libraryHash == null) {
                    LOGGER.error("Failed to get hashes from the server");
                    return;
                }

                String clientLocalHash = getFileHash(CLIENT_PATH);
                String libraryLocalHash = getFileHash(LIBRARY_PATH);

                if (clientLocalHash == null || libraryLocalHash == null) {
                    LOGGER.info("Client files not found, downloading...");
                    updateFiles(false);
                    return;
                }

                if (!clientHash.equals(clientLocalHash) || !libraryHash.equals(libraryLocalHash)) {
                    LOGGER.info("Client update found, downloading...");
                    updateFiles(true);
                    return;
                }
            } else if (!new File(CLIENT_PATH).exists() || !new File(LIBRARY_PATH).exists()) {
                LOGGER.error("Client files not found, when running with --no-update, you must have the client files");
                return;
            }

            File compressedFile = new File(COMPRESSED_PATH);
            if (!compressedFile.exists()) {
                LOGGER.info("Compressed file not found, creating...");
                createCompressedFile();
                return;
            }

            File nativeDir = new File(NATIVE_PATH);
            if (!nativeDir.exists()) {
                LOGGER.info("Native DLLs not found, extracting...");
                extractNatives();
                return;
            }

            LOGGER.info("Client is up to date");
        } catch (Exception e) {
            LOGGER.error("Failed to update client", e);
        }
    }

    private void updateFiles(boolean delete) {
        try {
            if (delete) {
                FileUtils.forceDelete(new File(CLIENT_PATH));
                FileUtils.forceDelete(new File(LIBRARY_PATH));
                FileUtils.forceDelete(new File(NATIVE_PATH));
            }

            DownloadUtil.downloadFile(
                    URLS.getString("client"),
                    CLIENT_PATH);
            DownloadUtil.downloadFile(
                    URLS.getString("libraries"),
                    LIBRARY_PATH);
            extractNatives();
            createCompressedFile();

            LOGGER.info("Client updated successfully");
        } catch (IOException e) {
            LOGGER.error("Failed to update client", e);
        }
    }

    private void extractNatives() {
        OsUtil.OS os = OsUtil.getOs();

        if (os == null) {
            LOGGER.error("Unsupported Operating System");
            return;
        }

        Map<String, byte[]> nativeDll = new HashMap<>();

        try (ZipFile libraryZip = new ZipFile(LIBRARY_PATH)) {
            for (ZipEntry entry : libraryZip.stream().toList()) {
                System.out.println(entry.getName());
                if (!entry.getName().endsWith("." + os.getNativeExtension()) || entry.getName().contains("/")) continue;

                nativeDll.put(entry.getName(), libraryZip.getInputStream(entry).readAllBytes());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update client", e);
        }

        if (nativeDll.isEmpty()) {
            LOGGER.error("No natives found in client");
            return;
        }

        try {
            File nativeDir = new File(NATIVE_PATH);
            boolean created = nativeDir.mkdirs();

            for (Map.Entry<String, byte[]> entry : nativeDll.entrySet()) {
                File nativeFile = new File(NATIVE_PATH, entry.getKey());
                FileUtils.writeByteArrayToFile(nativeFile, entry.getValue());
            }

            LOGGER.info("Natives for {} extracted successfully", os.name().toLowerCase());
        } catch (IOException e) {
            LOGGER.error("Failed to update client", e);
        }
    }

    private void createCompressedFile() {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(COMPRESSED_PATH))) {

            List<String> writtenEntries = new ArrayList<>();

            try (ZipFile clientZip = new ZipFile(CLIENT_PATH)) {
                for (ZipEntry entry : clientZip.stream().toList()) {
                    writtenEntries.add(entry.getName());
                    zipOut.putNextEntry(new ZipEntry(entry.getName()));
                    zipOut.write(clientZip.getInputStream(entry).readAllBytes());
                    zipOut.closeEntry();
                }
            }

            try (ZipFile libraryZip = new ZipFile(LIBRARY_PATH)) {
                for (ZipEntry entry : libraryZip.stream().toList()) {
                    if (writtenEntries.contains(entry.getName())) continue;
                    if (entry.getName().startsWith("org/objectweb")) continue;
                    zipOut.putNextEntry(new ZipEntry(entry.getName()));
                    zipOut.write(libraryZip.getInputStream(entry).readAllBytes());
                    zipOut.closeEntry();
                }
            }
            LOGGER.info("Compressed file created successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to update client", e);
        }
    }

    // thanks rise 🙏
    public String getFileHash(String filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fis = new FileInputStream(filePath)) {
                int bytesCount;
                byte[] byteArray = new byte[1024];
                while ((bytesCount = fis.read(byteArray)) != -1) {
                    digest.update(byteArray, 0, bytesCount);
                }
            }
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            return null;
        }
    }
}
