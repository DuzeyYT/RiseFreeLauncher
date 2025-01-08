package cc.fish.rfl.api.rise;

import cc.fish.rfl.api.utils.ConsoleUtil;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@UtilityClass
public class RiseLauncher {

    public final Logger LOGGER = LogManager.getLogger("Rise Launcher");

    public void launch(boolean mcOutput) {
        LOGGER.info("Launching Rise...");

        if (!mcOutput){
            ConsoleUtil.runCommand(
                    "java -javaagent:agent.jar -XX:+DisableAttachMechanism -noverify -Djava.library.path="
                            + RiseUpdater.NATIVE_PATH
                            + " -cp "
                            + RiseUpdater.COMPRESSED_PATH
                            + " Start");
            return;
        }

         try {
           ProcessBuilder processBuilder = new ProcessBuilder("java", "-javaagent:agent.jar",
                   "-XX:+DisableAttachMechanism", "-noverify",
                   "-Djava.library.path=" + RiseUpdater.NATIVE_PATH, "-cp", RiseUpdater.COMPRESSED_PATH, "Start");
           processBuilder.inheritIO();
           processBuilder.start();
         } catch (Exception e) {
           LOGGER.error("Failed to launch Rise: {}", e.getMessage());
         }
    }
}
