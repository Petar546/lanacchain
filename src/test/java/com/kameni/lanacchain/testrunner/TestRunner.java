package com.kameni.lanacchain.testrunner;

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

    void main(String[] args) {
        IO.println("------------ Starting Tests ------------");

        String packageName = "com.kameni.lanacchain";
        List<Object> testInstances = TestHelpers.findTests(packageName);

        TestResultGroup overallResultGroup = new TestResultGroup();
        for (Object testInstance : testInstances){
            TestPrint.printColoredln("--- Test Class: " + testInstance.getClass().getSimpleName() + " ---", Color.PINK);

            for (Method m : testInstance.getClass().getDeclaredMethods()) {
                //skip non test methods
                if (m.isAnnotationPresent(Test.class)) {
                    TestMethodData testMethodData = new TestMethodData(testInstance.getClass(), m, testInstance.getClass().getSimpleName(), 1);

                    printMethodStart(testMethodData);

                    TestMethodResult testMethodResult = runTestMethodOfInstance(testMethodData, testInstance);
                    overallResultGroup.addResult(testMethodResult);

                    printMethodFinish(testMethodResult);

                }
            }
        }


        printResultTable(overallResultGroup);

        if (!overallResultGroup.getFailed().isEmpty()){
            System.exit(1);
        }else {
            System.exit(0);
        }
    }




    private static void printResultTable(TestResultGroup overallResult) {
        int maxLinkLength = overallResult.getResults().stream()
                .mapToInt(r -> (r.testMethodData().methodName() + "(" + r.testMethodData().instanceClassName() + ":" + r.testMethodData().lineNumber() + ")").length())
                .max()
                .orElse(40);
        maxLinkLength = Math.max(maxLinkLength, 40);

        String headerFormat = "%-" + maxLinkLength + "s | %-10s | %-8s";
        String rowFormat = "%-" + maxLinkLength + "s | %-4d ms    | ";
        String separator = "-".repeat(maxLinkLength + 25);

        IO.println("\n================ TEST SUMMARY ================");
        IO.println(String.format(headerFormat, "Method Reference", "Time", "Status"));
        IO.println(separator);

        for (TestMethodResult methodResult : overallResult.getResults()) {
            String status = methodResult.passed() ? "PASSED" : "FAILED";
            Color statusColor = methodResult.passed() ? Color.GREEN : Color.RED;

            String clickableLink = String.format("%s(%s:%d)", methodResult.testMethodData().methodName(), methodResult.testMethodData().instanceClassName(), methodResult.lineNumber());

            TestPrint.printColored(String.format(rowFormat, clickableLink, methodResult.durationMs()), statusColor);
            TestPrint.printColoredln(status, statusColor);
        }

        IO.println(separator);

        long totalPassed = overallResult.getResults().stream().filter(TestMethodResult::passed).count();
        long totalFailed = overallResult.getResults().stream().filter(r -> !r.passed()).count();
        long totalTime = overallResult.getResults().stream().mapToLong(TestMethodResult::durationMs).sum();

        IO.print("Final Results: " + totalPassed + " Passed, ");
        if (totalFailed > 0) {
            TestPrint.printColored(totalFailed + " Failed", Color.RED);
        } else {
            IO.print("0 Failed");
        }
        IO.println(" (Total Time: " + totalTime + " ms)");
    }

    public TestMethodResult runTestMethodOfInstance(TestMethodData testMethodData, Object methodsInstance) {
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

    private static void printMethodStart(TestMethodData testMethodData) {
        IO.print("------ Running ");
        TestPrint.printColored(testMethodData.fullRealMethodName(), Color.MAGENTA);
        IO.println("... ------");
    }

    private void printMethodFinish(TestMethodResult testMethodResult) {
        IO.print("------ Finished ");
        Color statusColor = testMethodResult.passed() ? Color.GREEN : Color.RED;
        String statusText = testMethodResult.passed() ? " PASSED" : " FAILED";

        TestPrint.printColored(testMethodResult.testMethodData().fullRealMethodName() + statusText, statusColor);
        IO.print(" (" + testMethodResult.durationMs() + "ms) ------");

        if (!testMethodResult.passed()) {
            IO.println(" with stacktrace");
            if (testMethodResult.error() != null) testMethodResult.error().printStackTrace(System.out);
        } else {
            IO.println("");
        }
        IO.println("\n");
    }

}
