package com.kameni.lanacchain.testrunner;

import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.display.Color;
import com.kameni.lanacchain.testrunner.display.TestPrint;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TestRunner {

    void main(String[] args) {
        IO.println("------------ Starting Tests ------------");

        String packageName = "com.kameni.lanacchain";
        List<Object> testInstances = TestHelpers.findTests(packageName);

        TestResult overallResult = new TestResult();
        for (Object testInstance : testInstances){
            IO.println("--- Test Class: " + testInstance.getClass().getSimpleName() + " ---");

            TestResult currentTestTestResult = runMethodsOfInstance(testInstance);
            overallResult.add(currentTestTestResult);
        }


        printResult(overallResult);

        if (!overallResult.getFailed().isEmpty()){
            System.exit(1);
        }
        else {
            System.exit(0);
        };
    }




    private static void printResult(TestResult overallResult) {
        int maxNameLength = overallResult.results.stream()
                .mapToInt(r -> r.methodName.length())
                .max()
                .orElse(40);
        maxNameLength = Math.max(maxNameLength, 40);

        String headerFormat = "%-" + maxNameLength + "s | %-10s | %-8s";
        String rowFormat = "%-" + maxNameLength + "s | %-4d ms    | ";

        IO.println("\n================ TEST SUMMARY ================");
        IO.println(String.format(headerFormat, "Method Name", "Time", "Status"));

        // adjust line length based on dynamic width
        String separator = "-".repeat(maxNameLength + 25);
        IO.println(separator);

        for (TestResult.TestMethodData data : overallResult.results) {
            String status = data.passed ? "PASSED" : "FAILED";
            Color statusColor = data.passed ? Color.GREEN : Color.RED;

            IO.print(String.format(rowFormat, data.methodName, data.durationMs));
            TestPrint.printColoredln(status, statusColor);
        }

        IO.println(separator);

        int totalPassed = overallResult.getPassed().size();
        int totalFailed = overallResult.getFailed().size();
        long totalTime = overallResult.results.stream().mapToLong(r -> r.durationMs).sum();

        IO.print("Final Results: " + totalPassed + " Passed, ");
        if (totalFailed > 0) {
            TestPrint.printColored(totalFailed + " Failed", Color.RED);
        } else {
            IO.print("0 Failed");
        }
        IO.println(" (Total Time: " + totalTime + " ms)");
    }


    public TestResult runMethodsOfInstance(Object testInstance) {
        String testClassName = testInstance.getClass().getSimpleName();
        Method[] methods = testInstance.getClass().getDeclaredMethods();
        TestResult testResult = new TestResult();

        for (Method m : methods) {
            if (m.isAnnotationPresent(Test.class)) {
                String currentMethodName = testClassName + "." + m.getName();
                long startTime = System.nanoTime();
                try {
                    IO.print("------ Running ");
                    TestPrint.printColored(currentMethodName, Color.GREEN);
                    IO.println("... ------");

                    m.invoke(testInstance);

                    long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
                    TestPrint.printColored(currentMethodName + " PASSED", Color.GREEN);
                    TestPrint.printColoredln(" in " + duration + "ms", Color.BOLD_WHITE);
                    testResult.addResult(currentMethodName, duration, true, null);

                } catch (Exception e) {
                    long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
                    Throwable cause = (e.getCause() != null) ? e.getCause() : e;

                    TestPrint.printColoredln(currentMethodName + " FAILED", Color.RED);
                    TestPrint.printColoredln(" after " + duration + "ms", Color.BOLD_WHITE);
                    cause.printStackTrace();
                    testResult.addResult(currentMethodName, duration, false, cause);
                }
            }
        }
        return testResult;
    }

}
