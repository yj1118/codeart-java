package apros.codeart.log;

import apros.codeart.dto.DTObject;
import apros.codeart.io.IOUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import apros.codeart.AppConfig;

class FileLogger implements ILogger {

    public static final FileLogger INSTANCE;

    static {
        setupLogConfig();
        INSTANCE = new FileLogger();
    }

    private final Logger LOG4J;

    private FileLogger() {
        LOG4J = LogManager.getLogger(FileLogger.class);
    }

    private static void setupLogConfig() {

        var logConfig = AppConfig.section("log");

        var fileName = logConfig == null ? String.format("%s/app.log", IOUtil.getLogDirectory("app")) :
                logConfig.getString("fileName", "log/app/app.log"); // 默认是当前目录下的logs里的app.log文件

        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        builder.setStatusLevel(org.apache.logging.log4j.Level.ERROR);
//        LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout").addAttribute("pattern",
//                "%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n%throwable");

        LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%d{yyyy-MM-dd HH:mm:ss} [%t]%-5level%msg%n%throwable");

        // 添加文件 appender
        AppenderComponentBuilder fileAppenderBuilder = builder.newAppender("LogFile", "File")
                .addAttribute("fileName", fileName).add(layoutBuilder);
        builder.add(fileAppenderBuilder);

        // 添加 Stdout appender
        AppenderComponentBuilder stdoutAppenderBuilder = builder.newAppender("Stdout", "Console")
                .add(layoutBuilder);
        builder.add(stdoutAppenderBuilder);

        // 配置 root logger
        builder.add(builder.newRootLogger(org.apache.logging.log4j.Level.INFO)
                .add(builder.newAppenderRef("LogFile"))
                .add(builder.newAppenderRef("Stdout")));

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        var config = builder.build();
        ctx.start(config);
    }


    @Override
    public void error(Throwable ex) {
        LOG4J.error(ex.getMessage(), ex);  // 不能直接FileLogger.Instance.error(ex)，不会打印追踪信息
    }

    @Override
    public void trace(DTObject content) {
        var message = content.getCode();
        LOG4J.info(message);
    }

    @Override
    public void trace(String message) {
        LOG4J.info(message);
    }

    @Override
    public void trace(String formatMessage, Object... args) {
        LOG4J.info(String.format(formatMessage, args));
    }
}