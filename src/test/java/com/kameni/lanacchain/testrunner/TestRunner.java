package com.kameni.lanacchain.testrunner;

import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.display.Color;
import com.kameni.lanacchain.testrunner.display.TestPrint;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TestRunner {
    public class TestResult {
        private Set<String> passed;
        private Set<String> failed;

        TestResult(Set<String> passed, Set<String> failed) {
            this.passed = passed;
            this.failed = failed;
        }
        TestResult() {
            this.passed = new HashSet<>();
            this.failed = new HashSet<>();
        }

        void add(TestResult otherTestResult){
            this.passed.addAll(otherTestResult.passed);
            this.failed.addAll(otherTestResult.failed);
        }

        public Set<String> getFailed() {
            return failed;
        }

        public Set<String> getPassed() {
            return passed;
        }

        public void addPassed(String passedTestName) {
            passed.add(passedTestName);
        }

        public void addFailed(String failedTestName) {
            failed.add(failedTestName);
        }

    }

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
        IO.println("\n---------------------------------");
        IO.print("Results: ");
        //if any test has failed red, else make passed green :D
        if (!overallResult.getFailed().isEmpty()){
            IO.print(overallResult.getPassed().size() + " Passed");
            IO.print(", ");
            TestPrint.printColoredln(overallResult.getFailed().size() + " Failed", Color.RED);
        }else{
            TestPrint.printColored(overallResult.getPassed().size() + " Passed", Color.GREEN);
            IO.print(", ");
            IO.println(overallResult.getFailed().size() + " Failed");
        }
    }

    public TestResult runMethodsOfInstance(Object testInstance) {
        String testClassName = testInstance.getClass().getSimpleName();
        Method[] methods = testInstance.getClass().getDeclaredMethods();
        TestResult testResult = new TestResult();
        for (Method m : methods) {
            String currentMethodName = testClassName + "."  + m.getName();
            if (m.isAnnotationPresent(Test.class)) {
                try {
                    IO.print("------ Running ");
                    TestPrint.printColored(currentMethodName, Color.GREEN);
                    IO.println("... ------");

                    timeMethod(m, testInstance, currentMethodName);

                    TestPrint.printColoredln(currentMethodName + " PASSED", Color.GREEN);
                    testResult.addPassed(currentMethodName);
                } catch (Exception e) {
                    TestPrint.printColoredln(currentMethodName + " FAILED", Color.RED);

                    // unwrapping exception to see actual error
                    Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                    cause.printStackTrace();
                    testResult.addFailed(currentMethodName);
                }
            }
        }

        return testResult;
    }


    private void timeMethod(Method method, Object object, String currentMethodName) throws  Exception{
        long startTime = System.nanoTime();

        method.invoke(object); // dynamic call

        long endTime = System.nanoTime();
        long durationInNanos = endTime - startTime;

        long durationInMs = durationInNanos / 1_000_000;
        TestPrint.printColoredln(currentMethodName  + " took " + durationInMs + " ms", Color.BOLD_WHITE);

    }
}
