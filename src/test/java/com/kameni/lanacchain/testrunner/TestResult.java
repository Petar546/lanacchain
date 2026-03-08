package com.kameni.lanacchain.testrunner;

import java.util.ArrayList;
import java.util.List;

public class TestResult {
    // Helper class to store specific details for each method
    public static class TestMethodData {
        public final String methodName;
        public final String fileName;
        public final int lineNumber;
        public final long durationMs;
        public final boolean passed;
        public final Throwable error;

        public TestMethodData(String methodName, String fileName, int lineNumber, long durationMs, boolean passed, Throwable error) {
            this.methodName = methodName;
            this.fileName = fileName;
            this.lineNumber = lineNumber;
            this.durationMs = durationMs;
            this.passed = passed;
            this.error = error;
        }
    }

    final List<TestMethodData> results = new ArrayList<>();

    void add(TestResult other) {
        this.results.addAll(other.results);
    }

    public void addResult(String name, String fileName, int lineNumber, long time, boolean passed, Throwable error) {
        results.add(new TestMethodData(name, fileName, lineNumber, time, passed, error));
    }

    public List<TestMethodData> getPassed() {
        return results.stream().filter(r -> r.passed).toList();
    }

    public List<TestMethodData> getFailed() {
        return results.stream().filter(r -> !r.passed).toList();
    }
}

