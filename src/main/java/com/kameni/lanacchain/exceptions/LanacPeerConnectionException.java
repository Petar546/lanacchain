package com.kameni.lanacchain.exceptions;

public class LanacPeerConnectionException extends Exception {
    public LanacPeerConnectionException(String message, Throwable cause) {
        super("LanacPeerConnectionException: " + message, cause);
    }

    public LanacPeerConnectionException(Exception e) {
        super("LanacPeerConnectionException: " + e.getMessage(), e.getCause());
    }
}
