package com.kameni.lanacchain.testrunner;

import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.data.TestMethodData;
import com.kameni.lanacchain.testrunner.data.TestMethodResult;
import com.kameni.lanacchain.testrunner.data.TestResultGroup;
import com.kameni.lanacchain.testrunner.display.Color;
import com.kameni.lanacchain.testrunner.display.TestPrint;
import com.kameni.lanacchain.testrunner.helpers.TestDisplayer;
import com.kameni.lanacchain.testrunner.helpers.TestHelpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestRun {

    void main(String[] args) {
        String packageName = "com.kameni.lanacchain";
        TestRunner.runTests(packageName);
    }

}
