package com.kameni.lanacchain.testrunner;

public class LanacAssert {

    public static void assertTrue(boolean condition, String failedMessage) {
        if (!condition) {
            throw new RuntimeException("Assertion Failed: " + failedMessage);
        }
        // Do nothing on success. This allows the next line of the test to run.
    }

    public static void assertFalse(boolean condition, String failedMessage) {
        assertTrue(!condition, failedMessage);
    }


    public static void assertEquals(Object expected, Object actual, String failedMessage) {
        boolean match = (expected == null && actual == null) || (expected != null && expected.equals(actual));
        if (!match) {
            throw new RuntimeException("Assertion Failed: " + failedMessage
                    + " [Expected: " + expected + ", Actual: " + actual + "]");
        }
    }

    public static void assertThrows(Class<? extends Throwable> expectedException, ThrowingRunnable runnable, String failedMessage) {
        try {
            runnable.run();
        } catch (Throwable caught) {
            if (expectedException.isInstance(caught)) {
                return; // Success
            }

            throw new RuntimeException("Assertion Failed: " + failedMessage
                + " [Expected: " + expectedException.getSimpleName()
                + ", but caught: " + caught.getClass().getSimpleName() + "]");
        }
        throw new RuntimeException("Assertion Failed: " + failedMessage
            + " [No exception was thrown, but expected: " + expectedException.getSimpleName() + "]");
    }
}
