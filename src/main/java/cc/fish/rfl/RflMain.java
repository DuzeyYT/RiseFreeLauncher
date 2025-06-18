package cc.fish.rfl;

import cc.fish.rfl.api.rise.RiseLauncher;
import cc.fish.rfl.api.rise.RiseServer;
import cc.fish.rfl.api.rise.RiseUpdater;
import cc.fish.rfl.api.utils.ConsoleUtil;
import cc.fish.rfl.api.utils.JavaUtil;
import cc.fish.rfl.api.utils.OptionParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RflMain {
    private static RflMain instance;

    public static final Logger LOGGER = LogManager.getLogger("RFL");

    public void start(String[] args) {
        OptionParser optionParser = new OptionParser(args);

        // für cool und so
        ConsoleUtil.clearConsole();
        ConsoleUtil.emptyLine();

        System.out.println(ConsoleUtil.getCustomArt("RISE FREE LAUNCHER", null));

        LOGGER.info("Welcome to Rise Free Launcher!");
        ConsoleUtil.emptyLine();

        // get proper java version
        String javaCommand = JavaUtil.findProperJava();

        // add option to disable auto updates in case this has been patched.
        RiseUpdater.checkAndUpdate(optionParser.isEnabled("no-update"));
        ConsoleUtil.emptyLine();

        LOGGER.info("Starting Rise Client for Free...");
        new Thread(() -> RiseLauncher.launch(javaCommand, optionParser.isEnabled("enable-mc-output")), "rise").start();

        LOGGER.info("Starting Emulated Rise Server...");
        new RiseServer().startServer(optionParser.isEnabled("debug-packets"));

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
