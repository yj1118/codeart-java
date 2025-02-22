package apros.codeart.ddd.launcher;

import apros.codeart.TestSupport;
import apros.codeart.ddd.saga.SAGAConfig;
import apros.codeart.i18n.Language;

@TestSupport
public class DomainContainer {
    public static void main(String[] args) {
        String serverName = "undefined";
        if (args.length > 0) {
            serverName = args[0];
        }
        parseArgs(args);

        ConsoleLauncher.start_container(serverName);
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