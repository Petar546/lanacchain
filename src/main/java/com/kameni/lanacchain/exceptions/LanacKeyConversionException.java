package com.kameni.lanacchain.exceptions;

public class LanacKeyConversionException extends Exception {
    public LanacKeyConversionException(String message, Throwable cause) {
        super("LanacKeyConversionException: " + message, cause);
    }

    public LanacKeyConversionException(Exception e) {
        super("LanacKeyConversionException: " + e.getMessage(), e.getCause());
    }
}
