package com.kameni.lanacchain.exceptions;

import jdk.jfr.StackTrace;

public class KeyPairGenerationException extends Exception {
    public KeyPairGenerationException(String message, Throwable cause) {
        super("KeyPairGenerationException: " + message, cause);
    }
}
