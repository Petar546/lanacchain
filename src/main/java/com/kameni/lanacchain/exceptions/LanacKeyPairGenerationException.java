package com.kameni.lanacchain.exceptions;

public class LanacKeyPairGenerationException extends Exception {
    public LanacKeyPairGenerationException(String message, Throwable cause) {
        super("KeyPairGenerationException: " + message, cause);
    }

    public LanacKeyPairGenerationException(Exception e) {
        super("KeyPairGenerationException: " + e.getMessage(), e.getCause());
    }
}
