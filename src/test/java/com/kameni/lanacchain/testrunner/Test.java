package com.kameni.lanacchain.testrunner;


import com.kameni.lanacchain.KeyConverterTest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Test {
    public class ResultCount {
        Integer passed;
        Integer failed;

        ResultCount(int passed, int failed) {
            this.passed = passed;
            this.failed = failed;
        }

        void add(ResultCount otherResultCount){
            this.passed = this.passed + otherResultCount.passed;
            this.failed = this.failed + otherResultCount.failed;
        }
    }

    void main(String[] args) {
        IO.println("------------ Starting Tests ------------");

        Object[] testInstances = {
                new KeyConverterTest()
        };

        ResultCount overallResult = new ResultCount(0,0);
        for (Object testInstance : testInstances){
            IO.println("--- Test Class: " + testInstance.getClass().getSimpleName() + " ---");

            ResultCount currentTestResultCount = runMethodsOfInstance(testInstance);
            overallResult.add(currentTestResultCount);
        }

        System.out.println("\n---------------------------------");

        System.out.printf("Results: %d Passed, %d Failed%n", overallResult.passed, overallResult.failed);

        // Exit with error code if any test failed (useful for CI/CD)
        if (overallResult.failed > 0) System.exit(1);
    }

    public ResultCount runMethodsOfInstance(Object testInstance) {
        String testClassName = testInstance.getClass().getSimpleName();
        Method[] methods = testInstance.getClass().getDeclaredMethods();
        ResultCount resultCount = new ResultCount(0, 0);
        for (Method m : methods) {
            String currentMethodName = testClassName + "."  + m.getName();
            if (m.getName().startsWith("test") && !Modifier.isStatic(m.getModifiers())) {
                try {
                    IO.print("------ Running ");
                    TestPrint.printColored(currentMethodName, Color.GREEN);
                    IO.println("... ------");

                    m.invoke(testInstance); // dynamic call
                    TestPrint.printColored(currentMethodName + " PASSED", Color.GREEN);
                    resultCount.passed++;
                } catch (Exception e) {
                    TestPrint.printColored(currentMethodName + " FAILED", Color.RED);

                    // unwrapping exception to see actual error
                    Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                    cause.printStackTrace();
                    resultCount.failed++;
                }
            }
        }

        return resultCount;
    }
}
