package com.shtrih.tinyjavapostester.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;

public class LogbackConfig {
    public static final String MainFileName = "/tinyJavaPosTester.txt";

    public static void configure(String logDir) {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
        lc.reset();

        // Логирование в файл
        PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
        encoder1.setContext(lc);
        encoder1.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder1.start();

        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(lc);
        fileAppender.setFile(logDir + MainFileName);

        SizeBasedTriggeringPolicy<ILoggingEvent> trigPolicy = new SizeBasedTriggeringPolicy<>();
        trigPolicy.setMaxFileSize("10MB"); // задаем максимальный размер фрагмента
        trigPolicy.setContext(lc);
        trigPolicy.start();

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(lc);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(logDir + "/tinyJavaPosTester.%i.txt");
        rollingPolicy.setMinIndex(1); // храним 9 файлов предыдущих логов
        rollingPolicy.setMaxIndex(9);
        rollingPolicy.start();

        // Итого 10 файлов по 10Мб = 100Мб
        fileAppender.setTriggeringPolicy(trigPolicy);
        fileAppender.setRollingPolicy(rollingPolicy);

        fileAppender.setEncoder(encoder1);
        fileAppender.start();

        // Настраиваем логирование в logcat
        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
        encoder2.setContext(lc);
        encoder2.setPattern("[%thread] %msg%n");
        encoder2.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setEncoder(encoder2);
        logcatAppender.start();

        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(lc);
        asyncAppender.setName("ASYNC");

        // UNCOMMENT TO TWEAK OPTIONAL SETTINGS
//    // excluding caller data (used for stack traces) improves appender's performance
//    asyncAppender.setIncludeCallerData(false);
//    // set threshold to 0 to disable discarding and keep all events
//    asyncAppender.setDiscardingThreshold(0);
//    asyncAppender.setQueueSize(256);

        // NOTE: asyncAppender takes only one child appender
        asyncAppender.addAppender(fileAppender);
        asyncAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(asyncAppender);
        root.addAppender(logcatAppender);
    }
}