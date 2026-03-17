package com.kameni.lanacchain.testrunner;

import com.kameni.lanacchain.testrunner.helpers.TestDisplayer;
import com.kameni.lanacchain.testrunner.helpers.TestHelpers;
import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.data.TestMethodData;
import com.kameni.lanacchain.testrunner.data.TestMethodResult;
import com.kameni.lanacchain.testrunner.data.TestResultGroup;
import com.kameni.lanacchain.testrunner.display.Color;
import com.kameni.lanacchain.testrunner.display.TestPrint;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TestRunner {

    public static void runTests(String packageName) {
        IO.println("------------ Starting Tests ------------");

        List<Object> testInstances = TestHelpers.findTests(packageName);

        TestResultGroup overallResultGroup = new TestResultGroup();
        for (Object testInstance : testInstances){
            TestPrint.printColoredln("--- Test Class: " + testInstance.getClass().getSimpleName() + " ---", Color.PINK);

            for (Method m : testInstance.getClass().getDeclaredMethods()) {
                //skip non test methods
                if (m.isAnnotationPresent(Test.class)) {
                    TestMethodData testMethodData = new TestMethodData(testInstance.getClass(), m, testInstance.getClass().getSimpleName(), 1);

                    TestDisplayer.printMethodStart(testMethodData);

                    TestMethodResult testMethodResult = runTestMethodOfInstance(testMethodData, testInstance);
                    overallResultGroup.addResult(testMethodResult);

                    TestDisplayer.printMethodFinish(testMethodResult);

                }
            }
        }


        TestDisplayer.printResultTable(overallResultGroup);

        if (!overallResultGroup.getFailed().isEmpty()){
            System.exit(1);
        }else {
            System.exit(0);
        }
    }

    public static TestMethodResult runTestMethodOfInstance(TestMethodData testMethodData, Object methodsInstance) {
        long timerStartTime = System.nanoTime();
        Throwable testError = null;
        boolean isPassed = false;

        try {
            testMethodData.method().invoke(methodsInstance);
            isPassed = true;

        } catch (InvocationTargetException e) {
            testError = e.getCause();
        } catch (Exception e) {
            testError = e;
        }

        long testDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timerStartTime);

        return new TestMethodResult(testMethodData, testDuration, isPassed, testError);
    }


}
