package cc.fish.rfl;

import cc.fish.rfl.api.rise.RiseLauncher;
import cc.fish.rfl.api.rise.RiseServer;
import cc.fish.rfl.api.rise.RiseUpdater;
import cc.fish.rfl.api.utils.ConsoleUtil;
import cc.fish.rfl.api.utils.JavaUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RflMain {
    private static RflMain instance;

    public static final Logger LOGGER = LogManager.getLogger("RFL");

    public void start(CommandLine commandLine) {
        boolean standalone = commandLine.hasOption("standalone");
        boolean help = commandLine.hasOption("help");

        // print help message
        if (help) {
            new HelpFormatter().printHelp("Rise Free Launcher", createOptions());
            return;
        }

        // only start Rise Server if standalone mode is enabled
        if (standalone) {
            LOGGER.info("Running in standalone mode, only starting Rise Server...");
            new RiseServer().startServer(commandLine.hasOption("debug-packets"));
            return;
        }

        // für cool und so
        ConsoleUtil.clearConsole();
        ConsoleUtil.emptyLine();

        System.out.println(ConsoleUtil.getCustomArt("RISE FREE LAUNCHER", null));

        LOGGER.info("Welcome to Rise Free Launcher!");
        ConsoleUtil.emptyLine();

        // get proper java version
        String javaCommand = commandLine.hasOption("with-java-path")
            ? commandLine.getOptionValue("with-java-path")
            : JavaUtil.findProperJava();

        // add option to disable auto updates in case this has been patched.
        RiseUpdater.checkAndUpdate(commandLine.hasOption("no-update"));
        ConsoleUtil.emptyLine();

        LOGGER.info("Starting Rise Client for Free...");
        new Thread(() -> RiseLauncher.launch(javaCommand, commandLine.hasOption("enable-mc-output")), "rise").start();

        LOGGER.info("Starting Emulated Rise Server...");
        new RiseServer().startServer(commandLine.hasOption("debug-packets"));

        LOGGER.info("Thank you for using Rise Free Launcher!");
    }

    public static void main(String[] args) {
        Options options = createOptions();

        try {
            getInstance().start(new DefaultParser().parse(options, args));
        } catch (ParseException e) {
            new HelpFormatter().printHelp("Rise Free Launcher", options);
        }
    }

    // commons-cli
    private static Options createOptions() {
        Options options = new Options();

        {
            options.addOption(Option.builder()
                .longOpt("enable-mc-output")
                .desc("Enables Minecraft output in the console.").build());

            options.addOption(Option.builder()
                .longOpt("debug-packets")
                .desc("Enables debug packets for the Rise Server.").build());

            options.addOption(Option.builder()
                .longOpt("no-update")
                .desc("Disables automatic updates for Rise Client.").build());

            options.addOption(Option.builder()
                .longOpt("with-java-path").hasArg()
                .argName("java-path")
                .desc("Specifies the path to the Java executable to use.").build());

            // for developer purposes i guess idk
            options.addOption(Option.builder()
                .longOpt("standalone")
                .desc("Doesn't launch Rise, but instead only starts the Rise Server.").build());

            options.addOption(Option.builder()
                .longOpt("help")
                .desc("Displays this help message.").build());
        }

        return options;
    }

    public static RflMain getInstance() {
        if (instance == null) {
            instance = new RflMain();
        }
        return instance;
    }
}
