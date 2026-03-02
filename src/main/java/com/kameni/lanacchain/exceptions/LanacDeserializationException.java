package com.kameni.lanacchain.exceptions;

public class LanacDeserializationException extends Exception {
    public LanacDeserializationException(String message, Throwable cause) {
        super("LanacDeserializationException: " + message, cause);
    }
    public LanacDeserializationException(String message) {
        super("LanacDeserializationException: " + message);
    }

    public LanacDeserializationException(Exception e) {
        super("LanacDeserializationException: " + e.getMessage(), e.getCause());
    }
}
