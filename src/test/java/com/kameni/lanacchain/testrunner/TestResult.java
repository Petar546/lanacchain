package com.kameni.lanacchain.testrunner;

import java.util.ArrayList;
import java.util.List;

public class TestResult {
    // Helper class to store specific details for each method
    public static class TestMethodData {
        public String methodName;
        public long durationMs;
        public boolean passed;
        public Throwable error;

        public TestMethodData(String methodName, long durationMs, boolean passed, Throwable error) {
            this.methodName = methodName;
            this.durationMs = durationMs;
            this.passed = passed;
            this.error = error;
        }
    }

    final List<TestMethodData> results = new ArrayList<>();

    void add(TestResult other) {
        this.results.addAll(other.results);
    }

    public void addResult(String name, long time, boolean passed, Throwable error) {
        results.add(new TestMethodData(name, time, passed, error));
    }

    public List<TestMethodData> getPassed() {
        return results.stream().filter(r -> r.passed).toList();
    }

    public List<TestMethodData> getFailed() {
        return results.stream().filter(r -> !r.passed).toList();
    }
}

