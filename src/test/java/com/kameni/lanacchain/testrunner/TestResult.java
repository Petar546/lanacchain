package com.kameni.lanacchain.testrunner;

import java.util.ArrayList;
import java.util.List;

public class TestResult {
    // Helper class to store specific details for each method
        public record TestMethodData(
                String methodName,
                String fileName,
                int lineNumber,
                long durationMs,
                boolean passed,
                Throwable error
        ){}

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

