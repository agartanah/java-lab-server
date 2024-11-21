package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loggers {
    private static final Logger loggerUser = LoggerFactory.getLogger("user.messages");
    private static final Logger loggerErrors = LoggerFactory.getLogger("server.errors");

    public static Logger getLoggerUser() {
        return loggerUser;
    }

    public static Logger getLoggerErrors() {
        return loggerErrors;
    }
}
