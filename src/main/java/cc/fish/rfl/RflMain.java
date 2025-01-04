package cc.fish.rfl;

import cc.fish.rfl.api.rise.RiseLauncher;
import cc.fish.rfl.api.rise.RiseServer;
import cc.fish.rfl.api.rise.RiseUpdater;
import cc.fish.rfl.api.utils.ConsoleUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RflMain {
    private static RflMain instance;

    public static final Logger LOGGER = LogManager.getLogger("RFL");

    public void start() {
        System.out.println(ConsoleUtil.getCustomArt("RISE FREE LAUNCHER", null));

        LOGGER.info("Welcome to Rise Free Launcher!");
        ConsoleUtil.emptyLine();

        RiseUpdater.checkAndUpdate();
        ConsoleUtil.emptyLine();

        LOGGER.info("Starting Rise Client for Free...");
        new Thread(RiseLauncher::launch).start();

        LOGGER.info("Starting Emulated Rise Server...");
        new RiseServer().startServer();

        LOGGER.info("Thank you for using Rise Free Launcher!");
    }

    public static void main(String[] args) {
        RflMain.getInstance().start();
    }

    public static RflMain getInstance() {
        if (instance == null) {
            instance = new RflMain();
        }
        return instance;
    }
}