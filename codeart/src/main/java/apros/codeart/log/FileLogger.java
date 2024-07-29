package apros.codeart.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import apros.codeart.AppConfig;

class FileLogger {


    public static final Logger Instance;

    static {
        setupLogConfig();
        Instance = LogManager.getLogger(FileLogger.class);
    }

    private static void setupLogConfig() {

        var logConfig = AppConfig.section("log");

        var fileName = logConfig == null ? "logs/app.log" :
                logConfig.getString("fileName", "logs/app.log"); // 默认是当前目录下的logs里的app.log文件

        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        builder.setStatusLevel(org.apache.logging.log4j.Level.ERROR);
        LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout").addAttribute("pattern",
                "%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n");

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


}