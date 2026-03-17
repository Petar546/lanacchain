package com.kameni.lanacchain.testrunner;

public record TestMethodData(
        String methodName,
        String instanceClassName,
        int lineNumber,
        long durationMs,
        boolean passed,
        Throwable error
){
    public String instanceFileName() {
        return instanceClassName + ".java";
    }
}