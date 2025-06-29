package cc.fish.rfl.api.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@UtilityClass
public class JavaUtil {
  private final Logger LOGGER = LogManager.getLogger("Java Util");

  private static final Path WINDOWS_PATH = Paths.get("C:", "Program Files", "Java");
  private static final Path LINUX_PATH = Paths.get("/usr/lib/jvm");
  private static final Path MAC_PATH = Paths.get("/Library/Java/JavaVirtualMachines");

  public String findProperJava() {
    try {
      //int javaVersion = Integer.parseInt(System.getProperty("java.version").split("\\.")[0]);

      //if (javaVersion >= 21) { // if the user is already using Java 21 or higher
      //  return "java";
      //}

      Path javaPath = OsUtil.getOs() == OsUtil.OS.WINDOWS ? WINDOWS_PATH :
                      OsUtil.getOs() == OsUtil.OS.LINUX ? LINUX_PATH :
                      OsUtil.getOs() == OsUtil.OS.MAC ? MAC_PATH : null;

      // shouldn't happen.
      if (javaPath == null) {
        LOGGER.warn("Unsupported OS: {}", OsUtil.getOs());
        System.exit(1);
        return null;
      }

      // loop through all the directories in the Java path, to find a suitable Java version
      File[] files = javaPath.toFile().listFiles();
      if (files == null || files.length == 0) {
        LOGGER.warn("No Java installations found in: {}", javaPath);
        return "java"; // fallback to default java command
      }

      File javaInstallation = null;
      for (File file : files) {
        if (!file.isDirectory()) continue;

        String folderName = file.getName();
        String[] folderParts = folderName.split("-");
        if (folderParts.length <= 1) continue; // if the folder name doesn't contain a "-"

        String versionPart = folderParts[1];
        int folderJavaVersion = Integer.parseInt(versionPart.split("\\.")[0]);

        if (folderJavaVersion >= 21) {
          javaInstallation = file;
          break; // found a suitable Java installation
        }
      }

      // if no suitable Java installation was found, show a warning and exit
      if (javaInstallation == null) {
        JOptionPane.showMessageDialog(null, "Couldn't find a suitable Java version.\n" +
            "Please install Java 21 or higher.", "Invalid Java Version", JOptionPane.ERROR_MESSAGE);
        LOGGER.warn("No suitable Java version found in: {}", javaPath);
        System.exit(1);
        return null;
      }

      // if a suitable Java installation was found, return the path to the java executable
      return Paths.get(javaInstallation.getAbsolutePath(), "bin", "java").toString();
    } catch (Exception e) {
      LOGGER.warn("Failed to determine Java version: {}", e.getMessage());
      return "java"; // fallback to default java command
    }
  }
}