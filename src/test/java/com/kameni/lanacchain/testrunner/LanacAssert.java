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

    public static void assertThrows(Class<? extends Throwable> expectedException, ThrowingRunnable runnable, String message) {
        try {
            runnable.run();
        } catch (Throwable caught) {
            if (expectedException.isInstance(caught)) {
                return; // correct exception thrown
            }
            throw new RuntimeException("Assertion Failed: " + message
                + " [Expected: " + expectedException.getSimpleName()
                + ", but caught: " + caught.getClass().getSimpleName() + "]");
        }
        throw new RuntimeException("Assertion Failed: " + message
            + " [No exception was thrown, but expected: " + expectedException.getSimpleName() + "]");
    }
}