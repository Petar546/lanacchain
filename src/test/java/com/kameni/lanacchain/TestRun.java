package com.kameni.lanacchain;

import com.kameni.lanacchain.testrunner.TestRunner;

public class TestRun {

    void main(String[] args) {
        String packageName = "com.kameni.lanacchain";
        TestRunner.runTests(packageName);
    }

}
