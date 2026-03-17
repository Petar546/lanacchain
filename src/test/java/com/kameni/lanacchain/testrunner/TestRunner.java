package com.kameni.lanacchain.testrunner;

import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.display.Color;
import com.kameni.lanacchain.testrunner.display.TestPrint;
import com.kameni.lanacchain.testrunner.exceptions.TestPassedSignal;

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
        String testClassName = testInstance.getClass().getSimpleName();
        String fileName = testClassName + ".java";
        Method[] methods = testInstance.getClass().getDeclaredMethods();
        TestResult testResult = new TestResult();

        for (Method m : methods) {
            //skip non test methods
            if (!m.isAnnotationPresent(Test.class)) continue;

            String currentMethodName = testClassName + "." + getTestMethodName(m);

            IO.print("------ Running ");
            TestPrint.printColored(currentMethodName, Color.MAGENTA);
            IO.println("... ------");

            long timerStartTime = System.nanoTime();

            try {
                m.invoke(testInstance);
                throw new TestPassedSignal();
            } catch (Exception e) {

                long testDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timerStartTime);
                Throwable cause = (e instanceof InvocationTargetException) ? e.getCause() : e;

                int lineNumber = 1;
                for (StackTraceElement element : cause.getStackTrace()) {
                    if (element.getClassName().contains(testClassName) && element.getMethodName().equals(m.getName())) {
                        lineNumber = element.getLineNumber();
                        break;
                    }
                }

                boolean isPassed = (cause instanceof TestPassedSignal);

                IO.print("------ Finished ");
                if (isPassed) {
                    TestPrint.printColored(currentMethodName + " PASSED", Color.GREEN);
                    testResult.addResult(currentMethodName, fileName, lineNumber, testDuration, true, null);
                } else {
                    TestPrint.printColored(currentMethodName + " FAILED", Color.RED);
                    cause.printStackTrace();
                    testResult.addResult(currentMethodName, fileName, lineNumber, testDuration, false, cause);
                }
                IO.println(" ------");
            }
        }
        return testResult;
    }

    private static String getTestMethodName(Method m) {
        String methodName;
        if (!Objects.equals(m.getAnnotation(Test.class).name(), "")){
            methodName = m.getAnnotation(Test.class).name();
        }else{
            methodName = m.getName().replace("test__", "");
        }
        return methodName;
    }

}
