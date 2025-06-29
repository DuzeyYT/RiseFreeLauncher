package cc.fish.rfl.api.rise;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@UtilityClass
public class RiseLauncher {

    public final Logger LOGGER = LogManager.getLogger("Rise Launcher");

    public void launch(String java, boolean mcOutput) {
        LOGGER.info("Launching Rise with {}...", java);

         try {
           ProcessBuilder processBuilder = new ProcessBuilder(java, "-javaagent:agent.jar",
                   "-XX:+DisableAttachMechanism", "-noverify",
                   "-Djava.library.path=" + RiseUpdater.NATIVE_PATH, "-cp", RiseUpdater.COMPRESSED_PATH, "Start");
           if (mcOutput)
            processBuilder.inheritIO();
           processBuilder.start();
         } catch (Exception e) {
           LOGGER.error("Failed to launch Rise: {}", e.getMessage());
         }
    }
}
