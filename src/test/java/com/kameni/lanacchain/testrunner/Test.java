package com.kameni.lanacchain.testrunner;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

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

    void main(String[] args) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        IO.println("------------ Starting Tests ------------");

        String packageName = "com.kameni.lanacchain.testrun";
        List<Object> testInstances = findFilesInPackage(packageName, "Test");

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

    private static List<Object> findFilesInPackage(String packageName, String containingString) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String path = packageName.replace('.', '/');
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL resource = loader.getResource(path);

        assert resource != null;
        File directory = new File(resource.getFile());
        List<Object> testInstances = new ArrayList<>();

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.getName().endsWith(".class") && file.getName().contains(containingString)) {
                IO.println(file.getName());
                String className = packageName + "." + file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(className);
                try {
                    testInstances.add(clazz.getConstructor().newInstance());

                }catch (NoSuchMethodException e){
                    IO.println("NoSuchMethodException: no contructor method for class " + file.getName());
                }

            }
        }
        return testInstances;
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

}
