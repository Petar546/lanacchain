package com.kameni.lanacchain.testrunner;

public class LanacAssertionException extends RuntimeException {
    public LanacAssertionException(String message) {
        super(message);
    }
    public LanacAssertionException(Exception e) {
        super(e);
    }


}
