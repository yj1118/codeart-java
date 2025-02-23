package apros.codeart.ddd.launcher;

import apros.codeart.TestSupport;
import apros.codeart.ddd.saga.SAGAConfig;

@TestSupport
public class DomainContainer {

    private static String _serverName = "undefined";

    public static void main(String[] args) {
        if (args.length > 0) {
            _serverName = args[0];
        }
        parseArgs(args);

        ConsoleLauncher.startContainer(_serverName);
    }

    public static void println(String content) {
        System.out.printf("%s%n", content);
    }

    public static void println(int content) {
        System.out.printf("%s%n", content);
    }

    private static void parseArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-de ")) {
                var domainEvents = arg.substring(4).split(",");
                SAGAConfig.specifiedEvents(domainEvents);
            }
        }
    }

}