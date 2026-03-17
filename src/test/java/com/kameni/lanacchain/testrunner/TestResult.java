package com.kameni.lanacchain.testrunner;

import java.util.ArrayList;
import java.util.List;

public class TestResult {
    // Helper class to store specific details for each method


    final List<TestMethodResult> results = new ArrayList<>();

    void add(TestResult other) {
        this.results.addAll(other.results);
    }

    public void addResult(TestMethodResult TestMethodResult) {
        results.add(TestMethodResult);
    }

    public List<TestMethodResult> getPassed() {
        return results.stream().filter(r -> r.passed()).toList();
    }

    public List<TestMethodResult> getFailed() {
        return results.stream().filter(r -> !r.passed()).toList();
    }
}

