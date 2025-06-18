package cc.fish.rfl.api.rise;

import cc.fish.rfl.api.utils.DownloadUtil;
import cc.fish.rfl.api.utils.InputStreamUtil;
import cc.fish.rfl.api.utils.OsUtil;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
            URLS.put("client", "https://gitlab.com/rise_update/rise-update/-/raw/main/Standalone.jar?ref_type=heads&inline=false");
            URLS.put("client-hash", "https://gitlab.com/rise_update/rise-update/-/raw/main/Standalone_Hash.txt?ref_type=heads&inline=false");
            URLS.put("libraries", "https://gitlab.com/rise_update/rise-update/-/raw/main/Libraries.jar?ref_type=heads&inline=false");
            URLS.put("libraries-hash", "https://gitlab.com/rise_update/rise-update/-/raw/main/Libraries_Hash.txt?ref_type=heads&inline=false");
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

            boolean needsUpdate = false;

            String clientLocalHash = getFileHash(CLIENT_PATH);
            String libraryLocalHash = getFileHash(LIBRARY_PATH);

            if (clientLocalHash == null || libraryLocalHash == null) {
                LOGGER.info("Client files not found, downloading...");
                needsUpdate = true;
            } else if (!noUpdate) {
                String commitsApi = DownloadUtil.readFromWeb("https://gitlab.com/api/v4/projects/66162618/repository/commits?per_page=1");
                if (commitsApi == null) {
                    LOGGER.error("Failed to get latest commit from the server");
                } else {
                    try {
                        JSONObject latestCommit = new JSONObject(commitsApi.substring(1, commitsApi.length() - 1));
                        String commitedDate = latestCommit.getString("committed_date");

                        OffsetDateTime dateTime = OffsetDateTime.parse(commitedDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                        long lastUpdatedTime = Math.min(
                                new File(CLIENT_PATH).lastModified(),
                                new File(LIBRARY_PATH).lastModified()
                        );

                        OffsetDateTime lastUpdatedDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(lastUpdatedTime), OffsetDateTime.now().getOffset());

                        if (dateTime.isAfter(lastUpdatedDate)) {
                            LOGGER.info("Client update found, downloading...");
                            needsUpdate = true;
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to parse latest commit date", e);
                    }
                }
            }

            if (needsUpdate) {
                LOGGER.info("Updating client files...");
                updateFiles(true);
                return;
            }

            File compressedFile = new File(COMPRESSED_PATH);
            if (!compressedFile.exists()) {
                LOGGER.info("Compressed file not found, creating...");
                createCompressedFile();
            }

            File nativeDir = new File(NATIVE_PATH);
            if (!nativeDir.exists()) {
                LOGGER.info("Native DLLs not found, extracting...");
                extractNatives();
            }

            LOGGER.info("Client is up to date");
        } catch (Exception e) {
            LOGGER.error("Failed to update client", e);
        }
    }

    private void updateFiles(boolean delete) {
        try {
            if (delete) {
                File[] filesToDelete = {
                        new File(CLIENT_PATH),
                        new File(LIBRARY_PATH),
                        new File(NATIVE_PATH),
                        new File(COMPRESSED_PATH)
                };

                for (File file : filesToDelete) {
                    if (file.exists()) {
                        FileUtils.forceDelete(file);
                        LOGGER.info("Deleted file: {}", file.getAbsolutePath());
                    }
                }
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
            for (ZipEntry entry : libraryZip.stream().collect(Collectors.toList())) {
                if (!entry.getName().endsWith("." + os.getNativeExtension()) || entry.getName().contains("/")) continue;

                nativeDll.put(entry.getName(), InputStreamUtil.readAllBytes(libraryZip.getInputStream(entry)));
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
                for (ZipEntry entry : clientZip.stream().collect(Collectors.toList())) {
                    writtenEntries.add(entry.getName());
                    zipOut.putNextEntry(new ZipEntry(entry.getName()));
                    zipOut.write(InputStreamUtil.readAllBytes(clientZip.getInputStream(entry)));
                    zipOut.closeEntry();
                }
            }

            try (ZipFile libraryZip = new ZipFile(LIBRARY_PATH)) {
                for (ZipEntry entry : libraryZip.stream().collect(Collectors.toList())) {
                    if (writtenEntries.contains(entry.getName())) continue;
                    if (entry.getName().startsWith("org/objectweb")) continue;
                    zipOut.putNextEntry(new ZipEntry(entry.getName()));
                    zipOut.write(InputStreamUtil.readAllBytes(libraryZip.getInputStream(entry)));
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
