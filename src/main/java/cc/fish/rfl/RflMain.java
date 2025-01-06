package cc.fish.rfl;

import cc.fish.rfl.api.rise.RiseConfigConverter;
import cc.fish.rfl.api.rise.RiseLauncher;
import cc.fish.rfl.api.rise.RiseServer;
import cc.fish.rfl.api.rise.RiseUpdater;
import cc.fish.rfl.api.utils.ConsoleUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class RflMain {
    private static RflMain instance;

    public static final Logger LOGGER = LogManager.getLogger("RFL");

    public void start(String[] args) {
        boolean noUpdate = false;
        boolean shouldDebugPackets = false;

        for (String arg : args) {
            if (arg.equals("--no-update"))
                noUpdate = true;
            if (arg.equals("--debug-packets"))
                shouldDebugPackets = true;
        }

        // für cool und so
        ConsoleUtil.clearConsole();
        ConsoleUtil.emptyLine();

        System.out.println(ConsoleUtil.getCustomArt("RISE FREE LAUNCHER", null));

        LOGGER.info("Welcome to Rise Free Launcher!");
        ConsoleUtil.emptyLine();

        // add option to disable auto updates in case this has been patched.
        if (!noUpdate) {
            RiseUpdater.checkAndUpdate();
            ConsoleUtil.emptyLine();
        }

        LOGGER.info("Starting Rise Client for Free...");
        new Thread(RiseLauncher::launch, "rise").start();

        LOGGER.info("Starting Emulated Rise Server...");
        new RiseServer().startServer(shouldDebugPackets);

        LOGGER.info("Thank you for using Rise Free Launcher!");
    }

    public static void main(String[] args) {
        RflMain.getInstance().start(args);
    }

    public static RflMain getInstance() {
        if (instance == null) {
            instance = new RflMain();
        }
        return instance;
    }
}
