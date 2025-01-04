package cc.fish.rfl.api.rise;

import cc.fish.rfl.api.utils.DownloadUtil;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@UtilityClass
public class RiseUpdater {
    public final Logger LOGGER = LogManager.getLogger("Rise Updater");

    public final String RISE_LATEST_VERSION_URL = "https://raw.githubusercontent.com/risellc/LatestRiseVersion/main/Version";
    public final String RISE_LIBRARY_HASH_URL = "https://raw.githubusercontent.com/AlanW5/rise_update/refs/heads/main/Libraries_Hash.txt";
    public final String CLIENT_HASH_URL = "https://raw.githubusercontent.com/AlanW5/rise_update/refs/heads/main/Standalone_Hash.txt";

    public final String CLIENT_PATH = "client.jar";
    public final String LIBRARY_PATH = "libraries.jar";
    public final String COMPRESSED_PATH = "compressed.jar";

    public void checkAndUpdate() {
        LOGGER.info("Checking for client updates...");
        String latestVersion = DownloadUtil.readFromWeb(RISE_LATEST_VERSION_URL);
        LOGGER.info("(Latest version: {})", latestVersion);

        try {
            String clientHash = DownloadUtil.readFromWeb(CLIENT_HASH_URL);
            String libraryHash = DownloadUtil.readFromWeb(RISE_LIBRARY_HASH_URL);

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

            File compressedFile = new File(COMPRESSED_PATH);
            if (!compressedFile.exists()) {
                LOGGER.info("Compressed file not found, creating...");
                createCompressedFile();
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
            }

            DownloadUtil.downloadFile("https://raw.githubusercontent.com/AlanW5/rise_update/refs/heads/main/Standalone.jar", CLIENT_PATH);
            DownloadUtil.downloadFile("https://raw.githubusercontent.com/AlanW5/rise_update/refs/heads/main/Libraries.jar", LIBRARY_PATH);

            createCompressedFile();

            LOGGER.info("Client updated successfully");
        } catch (IOException e) {
            LOGGER.error("Failed to update client", e);
        }
    }

    private void createCompressedFile() {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(COMPRESSED_PATH))) {

            List<String> writtenEntries = new ArrayList<>();

            try (ZipFile libraryZip = new ZipFile(CLIENT_PATH)) {
                for (ZipEntry entry : libraryZip.stream().toList()) {
                    writtenEntries.add(entry.getName());
                    zipOut.putNextEntry(new ZipEntry(entry.getName()));
                    zipOut.write(libraryZip.getInputStream(entry).readAllBytes());
                    zipOut.closeEntry();
                }
            }

            try (ZipFile clientZip = new ZipFile(LIBRARY_PATH)) {
                for (ZipEntry entry : clientZip.stream().toList()) {
                    if (writtenEntries.contains(entry.getName())) continue;
                    if (entry.getName().startsWith("org/objectweb")) continue;
                    zipOut.putNextEntry(new ZipEntry(entry.getName()));
                    zipOut.write(clientZip.getInputStream(entry).readAllBytes());
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
            try (FileInputStream fis = new FileInputStream(filePath);){
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
        }
        catch (IOException | NoSuchAlgorithmException e) {
            return null;
        }
    }
}