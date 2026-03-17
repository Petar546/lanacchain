package com.kameni.lanacchain.testrunner;

import java.util.ArrayList;
import java.util.List;

public class TestResult {
    // Helper class to store specific details for each method


    final List<TestMethodData> results = new ArrayList<>();

    void add(TestResult other) {
        this.results.addAll(other.results);
    }

    public void addResult(TestMethodData testMethodData) {
        results.add(testMethodData);
    }

    public List<TestMethodData> getPassed() {
        return results.stream().filter(r -> r.passed()).toList();
    }

    public List<TestMethodData> getFailed() {
        return results.stream().filter(r -> !r.passed()).toList();
    }
}

