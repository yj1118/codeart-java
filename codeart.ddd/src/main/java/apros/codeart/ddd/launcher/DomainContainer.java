package apros.codeart.ddd.launcher;

import apros.codeart.TestSupport;
import apros.codeart.ddd.saga.SAGAConfig;
import apros.codeart.dto.DTObject;
import apros.codeart.io.IOUtil;

@TestSupport
public class DomainContainer {

    private static String _serverName = "undefined";

    public static void main(String[] args) {
        if (args.length > 0) {
            _serverName = args[0];
        }

//        initLog();

        parseArgs(args);

        ConsoleLauncher.startContainer(_serverName);
    }

//    private static DTObject _log;
//    private static String _logFileName;
//
//    private static void initLog() {
//        String logName = String.format("%s_log", _serverName);
//        _logFileName = IOUtil.createTempFile(logName, false);
//        _log = DTObject.editable();
//        _log.save(_logFileName);
//    }

    public static void println(String content) {
        System.out.printf("%s%n", content);
//        if (_log != null) {
//            _log.pushString("rows", content);
//            _log.save(_logFileName);
//        }
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


//    public static DTObject getLog(String serverName) {
//        String logName = String.format("%s_log", serverName);
//        if (!IOUtil.existsTempFile(logName)) return DTObject.empty();
//        return DTObject.load(IOUtil.getTempFile(logName));
//    }


}