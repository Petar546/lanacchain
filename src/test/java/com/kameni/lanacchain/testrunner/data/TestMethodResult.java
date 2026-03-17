package com.kameni.lanacchain.testrunner.data;

public record TestMethodResult(
        TestMethodData testMethodData,
        long durationMs,
        boolean passed,
        Throwable error
){
    /**
     * returns the line Number of the TestMethod from TestMethodData or the stacktrace line number if it can find it
     */
    public int lineNumber(){

        if (error != null){
            for (StackTraceElement element : error.getStackTrace()) {
                if (element.getClassName().contains(testMethodData.method().getDeclaringClass().getSimpleName()) && element.getMethodName().equals(testMethodData.method().getName())) {
                    return element.getLineNumber();
                }
            }
        }

        return testMethodData.lineNumber();
    }
}