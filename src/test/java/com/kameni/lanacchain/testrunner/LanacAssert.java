package com.kameni.lanacchain.testrunner;

import com.kameni.lanacchain.testrunner.exceptions.TestPassedSignal;

public class LanacAssert {

    public static void assertTrue(boolean condition, String failedMessage) {
        if (!condition) {
            throw new RuntimeException("Assertion Failed: " + failedMessage);
        }
        throw new TestPassedSignal();
    }

    public static void assertFalse(boolean condition, String failedMessage) {
        assertTrue(!condition, failedMessage);
    }


    public static void assertEquals(Object expected, Object actual, String failedMessage) {
        if (expected == null && actual == null) {
            throw new TestPassedSignal();
        }
        if (expected != null && expected.equals(actual)){
            throw new TestPassedSignal();
        }

        throw new RuntimeException("Assertion Failed: " + failedMessage
                + " [Expected: " + expected + ", Actual: " + actual + "]");
    }

    public static void assertThrows(Class<? extends Throwable> expectedException, ThrowingRunnable runnable, String failedMessage) {
        try {
            runnable.run();
        } catch (Throwable caught) {
            if (expectedException.isInstance(caught)) {
                throw new TestPassedSignal();
            }

            throw new RuntimeException("Assertion Failed: " + failedMessage
                + " [Expected: " + expectedException.getSimpleName()
                + ", but caught: " + caught.getClass().getSimpleName() + "]");
        }
        throw new RuntimeException("Assertion Failed: " + failedMessage
            + " [No exception was thrown, but expected: " + expectedException.getSimpleName() + "]");
    }
}