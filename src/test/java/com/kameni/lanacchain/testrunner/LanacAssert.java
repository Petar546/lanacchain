package com.kameni.lanacchain.testrunner;

public class LanacAssert {

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException("Assertion Failed: " + message);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;

        throw new RuntimeException("Assertion Failed: " + message
                + " [Expected: " + expected + ", Actual: " + actual + "]");
    }
}