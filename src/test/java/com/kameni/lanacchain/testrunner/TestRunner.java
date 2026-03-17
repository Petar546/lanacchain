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

            for (Method m : testInstance.getClass().getDeclaredMethods()) {
                //skip non test methods
                if (m.isAnnotationPresent(Test.class)) {
                    TestMethodData testMethodData = runTestMethodOfInstance(m, testInstance);
                    overallResult.addResult(testMethodData);
                }
            }
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
                .mapToInt(r -> (r.methodName() + "(" + r.instanceClassName() + ":" + r.lineNumber() + ")").length())
                .max()
                .orElse(40);
        maxLinkLength = Math.max(maxLinkLength, 40);

        String headerFormat = "%-" + maxLinkLength + "s | %-10s | %-8s";
        String rowFormat = "%-" + maxLinkLength + "s | %-4d ms    | ";
        String separator = "-".repeat(maxLinkLength + 25);

        IO.println("\n================ TEST SUMMARY ================");
        IO.println(String.format(headerFormat, "Method Reference", "Time", "Status"));
        IO.println(separator);

        for (TestMethodData data : overallResult.results) {
            String status = data.passed() ? "PASSED" : "FAILED";
            Color statusColor = data.passed() ? Color.GREEN : Color.RED;

            String clickableLink = String.format("%s(%s:%d)", data.methodName(), data.instanceClassName(), data.lineNumber());

            TestPrint.printColored(String.format(rowFormat, clickableLink, data.durationMs()), statusColor);
            TestPrint.printColoredln(status, statusColor);
        }

        IO.println(separator);

        long totalPassed = overallResult.results.stream().filter(TestMethodData::passed).count();
        long totalFailed = overallResult.results.stream().filter(r -> !r.passed()).count();
        long totalTime = overallResult.results.stream().mapToLong(TestMethodData::durationMs).sum();

        IO.print("Final Results: " + totalPassed + " Passed, ");
        if (totalFailed > 0) {
            TestPrint.printColored(totalFailed + " Failed", Color.RED);
        } else {
            IO.print("0 Failed");
        }
        IO.println(" (Total Time: " + totalTime + " ms)");
    }

    public TestMethodData runTestMethodOfInstance(Method testMethod, Object methodsInstance) {
        String instanceClassName = methodsInstance.getClass().getSimpleName();

        String fullMethodName = instanceClassName + "." + getTestMethodName(testMethod);

        printMethodStart(fullMethodName);

        long timerStartTime = System.nanoTime();
        Throwable testError = null;
        boolean isPassed = false;

        try {
            testMethod.invoke(methodsInstance);
            isPassed = true;

        } catch (InvocationTargetException e) {
            testError = e.getCause();
        } catch (Exception e) {
            testError = e;
        }

        long testDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timerStartTime);
        int lineNumber = findLineNumber(testError, testMethod);

        printMethodFinish(fullMethodName, isPassed, testDuration, testError);

        return new TestMethodData(fullMethodName, instanceClassName, lineNumber, testDuration, isPassed, testError);
    }

    private static void printMethodStart(String currentMethodName) {
        IO.print("------ Running ");
        TestPrint.printColored(currentMethodName, Color.MAGENTA);
        IO.println("... ------");
    }


    private void printMethodFinish(String methodName, boolean passed, long duration, Throwable error) {
        IO.print("------ Finished ");
        Color statusColor = passed ? Color.GREEN : Color.RED;
        String statusText = passed ? " PASSED" : " FAILED";

        TestPrint.printColored(methodName + statusText, statusColor);
        IO.print(" (" + duration + "ms) ------");

        if (!passed) {
            IO.println(" with stacktrace");
            if (error != null) error.printStackTrace(System.out);
        } else {
            IO.println("");
        }
        IO.println("\n");
    }

    private int findLineNumber(Throwable t, Method m) {
        if (t == null) return 1;
        for (StackTraceElement element : t.getStackTrace()) {
            if (element.getClassName().contains(m.getDeclaringClass().getSimpleName()) && element.getMethodName().equals(m.getName())) {
                return element.getLineNumber();
            }
        }
        return 1;
    }

    private static String getTestMethodName(Method m) {
        Test annotation = m.getAnnotation(Test.class);

        String methodName;
        if (!Objects.equals(annotation.name(), "")){
            methodName = annotation.name();
        }else{
            methodName = m.getName().replace("test__", "");
        }
        return methodName;
    }

}
