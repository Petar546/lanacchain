package com.kameni.lanacchain.testrunner;


import com.kameni.lanacchain.KeyConverterTest;
import com.kameni.lanacchain.PeerIdentityTest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class Test {
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

        Object[] testInstances = {
                new KeyConverterTest(),
                new PeerIdentityTest()
        };

        TestResult overallResult = new TestResult();
        for (Object testInstance : testInstances){
            IO.println("--- Test Class: " + testInstance.getClass().getSimpleName() + " ---");

            TestResult currentTestTestResult = runMethodsOfInstance(testInstance);
            overallResult.add(currentTestTestResult);
        }


        printResult(overallResult);

        // Exit with error code if any test failed (useful for CI/CD)
        if (!overallResult.getFailed().isEmpty()) System.exit(1);
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
            if (m.getName().startsWith("test__") && !Modifier.isStatic(m.getModifiers())) {
                try {
                    IO.print("------ Running ");
                    TestPrint.printColored(currentMethodName, Color.GREEN);
                    IO.println("... ------");

                    m.invoke(testInstance); // dynamic call
                    TestPrint.printColored(currentMethodName + " PASSED", Color.GREEN);
                    testResult.addPassed(currentMethodName);
                } catch (Exception e) {
                    TestPrint.printColored(currentMethodName + " FAILED", Color.RED);

                    // unwrapping exception to see actual error
                    Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                    cause.printStackTrace();
                    testResult.addFailed(currentMethodName);
                }
            }
        }

        return testResult;
    }
}
