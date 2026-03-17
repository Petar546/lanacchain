package com.kameni.lanacchain.testrunner;

import com.kameni.lanacchain.testrunner.annotations.Test;
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

        TestResult overallResult = new TestResult();
        for (Object testInstance : testInstances){
            TestPrint.printColoredln("--- Test Class: " + testInstance.getClass().getSimpleName() + " ---", Color.PINK);

            TestResult currentTestTestResult = runMethodsOfInstance(testInstance);
            overallResult.add(currentTestTestResult);
        }


        printResultTable(overallResult);

        if (!overallResult.getFailed().isEmpty()){
            System.exit(1);
        }else {
            System.exit(0);
        }
    }



    private static void printResultTable(TestResult overallResult) {
        int maxLinkLength = overallResult.results.stream()
                .mapToInt(r -> (r.methodName() + "(" + r.fileName() + ":" + r.lineNumber() + ")").length())
                .max()
                .orElse(40);
        maxLinkLength = Math.max(maxLinkLength, 40);

        String headerFormat = "%-" + maxLinkLength + "s | %-10s | %-8s";
        String rowFormat = "%-" + maxLinkLength + "s | %-4d ms    | ";
        String separator = "-".repeat(maxLinkLength + 25);

        IO.println("\n================ TEST SUMMARY ================");
        IO.println(String.format(headerFormat, "Method Reference", "Time", "Status"));
        IO.println(separator);

        for (TestResult.TestMethodData data : overallResult.results) {
            String status = data.passed() ? "PASSED" : "FAILED";
            Color statusColor = data.passed() ? Color.GREEN : Color.RED;

            String clickableLink = String.format("%s(%s:%d)", data.methodName(), data.fileName(), data.lineNumber());

            TestPrint.printColored(String.format(rowFormat, clickableLink, data.durationMs()), statusColor);
            TestPrint.printColoredln(status, statusColor);
        }

        IO.println(separator);

        int totalPassed = (int) overallResult.results.stream().filter(r -> r.passed()).count();
        int totalFailed = (int) overallResult.results.stream().filter(r -> !r.passed()).count();
        long totalTime = overallResult.results.stream().mapToLong(r -> r.durationMs()).sum();

        IO.print("Final Results: " + totalPassed + " Passed, ");
        if (totalFailed > 0) {
            TestPrint.printColored(totalFailed + " Failed", Color.RED);
        } else {
            IO.print("0 Failed");
        }
        IO.println(" (Total Time: " + totalTime + " ms)");
    }

    public TestResult runMethodsOfInstance(Object testInstance) {
        TestResult testResult = new TestResult();

        String testClassName = testInstance.getClass().getSimpleName();
        for (Method m : testInstance.getClass().getDeclaredMethods()) {
            //skip non test methods
            if (!m.isAnnotationPresent(Test.class)) continue;

            String currentMethodName = testClassName + "." + getTestMethodName(m);

            IO.print("------ Running ");
            TestPrint.printColored(currentMethodName, Color.MAGENTA);
            IO.println("... ------");

            long timerStartTime = System.nanoTime();
            Throwable testError = null;
            boolean isPassed = false;

            try {
                m.invoke(testInstance);
                isPassed = true;

            } catch (InvocationTargetException e) {
                testError = e.getCause();
            } catch (Exception e) {
                testError = e;
            }

            long testDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timerStartTime);
            int lineNumber = findLineNumber(testError, m);

            IO.print("------ Finished ");
            if (isPassed) {
                TestPrint.printColored(currentMethodName + " PASSED", Color.GREEN);
                IO.println(" (" + testDuration + "ms) ------");

            } else {
                TestPrint.printColored(currentMethodName + " FAILED", Color.RED);
                IO.print(" (" + testDuration + "ms) ------");
                IO.println(" with stacktrace");

                if (testError != null) {
                    testError.printStackTrace(System.out);
                }
            }
            IO.println("\n");

            testResult.addResult(currentMethodName, testClassName + ".java", lineNumber, testDuration, isPassed, testError);
        }
        return testResult;
    }

    private int findLineNumber(Throwable t, Method m) {
        if (t == null) return 1;
        for (StackTraceElement element : t.getStackTrace()) {
            if (element.getClassName().contains(m.getClass().getSimpleName()) && element.getMethodName().equals(m.getName())) {
                return element.getLineNumber();
            }
        }
        return 1;
    }


    private static String getTestMethodName(Method m) {
        String methodName;
        m.getClass().getSimpleName();
        if (!Objects.equals(m.getAnnotation(Test.class).name(), "")){
            methodName = m.getAnnotation(Test.class).name();
        }else{
            methodName = m.getName().replace("test__", "");
        }
        return methodName;
    }

}
