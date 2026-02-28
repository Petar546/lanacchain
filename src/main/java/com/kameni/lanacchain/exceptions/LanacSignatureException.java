package com.kameni.lanacchain.exceptions;

public class LanacSignatureException extends Exception {
    public LanacSignatureException(String message, Throwable cause) {
        super("LanacSignatureException: " + message, cause);
    }

    public LanacSignatureException(Exception e) {
        super("LanacSignatureException: " + e.getMessage(), e.getCause());
    }
}
