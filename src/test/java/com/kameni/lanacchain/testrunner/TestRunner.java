package com.kameni.lanacchain.testrunner;

import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.display.Color;
import com.kameni.lanacchain.testrunner.display.TestPrint;
import com.kameni.lanacchain.testrunner.exceptions.TestPassedSignal;

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
        }
    }



    private static void printResult(TestResult overallResult) {
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

            IO.print(String.format(rowFormat, clickableLink, data.durationMs()));
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
        String testClassName = testInstance.getClass().getSimpleName();
        String fileName = testClassName + ".java";
        Method[] methods = testInstance.getClass().getDeclaredMethods();
        TestResult testResult = new TestResult();

        for (Method m : methods) {
            if (m.isAnnotationPresent(Test.class)) {
                String methodName;
                if (!Objects.equals(m.getAnnotation(Test.class).name(), "")){
                    methodName = m.getAnnotation(Test.class).name();
                }else{
                    methodName = m.getName();
                }
                String currentMethodName = testClassName + "." + methodName;

                IO.print("------ Running ");
                TestPrint.printColored(currentMethodName, Color.GREEN);
                IO.println("... ------");

                long timerStartTime = System.nanoTime();

                try {
                    m.invoke(testInstance);

                    long testDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timerStartTime);

                    TestPrint.printColoredln(currentMethodName + " PASSED", Color.GREEN);

                    testResult.addResult(currentMethodName, fileName, 1, testDuration, true, null);

                } catch (Exception e) {
                    long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timerStartTime);
                    Throwable cause = (e instanceof java.lang.reflect.InvocationTargetException) ? e.getCause() : e;

                    int lineNumber = 1;
                    for (StackTraceElement element : cause.getStackTrace()) {
                        if (element.getClassName().contains(testClassName) && element.getMethodName().equals(m.getName())) {
                            lineNumber = element.getLineNumber();
                            break;
                        }
                    }

                    boolean isPassed = (cause instanceof TestPassedSignal);

                    if (isPassed) {
                        TestPrint.printColoredln(currentMethodName + " PASSED", Color.GREEN);
                        testResult.addResult(currentMethodName, fileName, lineNumber, duration, true, null);
                    } else {
                        TestPrint.printColoredln(currentMethodName + " FAILED", Color.RED);
                        cause.printStackTrace();
                        testResult.addResult(currentMethodName, fileName, lineNumber, duration, false, cause);
                    }
                }
            }
        }
        return testResult;
    }

}
