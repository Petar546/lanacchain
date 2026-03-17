package com.kameni.lanacchain.testrunner.data;

import java.util.ArrayList;
import java.util.List;

public class TestResultGroup {

    final List<TestMethodResult> results = new ArrayList<>();

    public List<TestMethodResult> getResults() {
        return results;
    }

    void add(TestResultGroup other) {
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

