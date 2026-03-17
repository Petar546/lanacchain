package com.kameni.lanacchain.testrunner.helpers;

import com.kameni.lanacchain.testrunner.data.TestMethodData;
import com.kameni.lanacchain.testrunner.data.TestMethodResult;
import com.kameni.lanacchain.testrunner.data.TestResultGroup;
import com.kameni.lanacchain.testrunner.display.Color;
import com.kameni.lanacchain.testrunner.display.TestPrint;

public class TestDisplayer {

    public static void printResultTable(TestResultGroup overallResult) {
        int maxLinkLength = overallResult.getResults().stream()
                .mapToInt(r -> (r.testMethodData().fullRealMethodName() + "(" + r.testMethodData().instanceClassName() + ":" + r.testMethodData().lineNumber() + ")").length())
                .max()
                .orElse(40);
        maxLinkLength = Math.max(maxLinkLength, 40);

        String headerFormat = "%-" + maxLinkLength + "s | %-10s | %-8s";
        String rowFormat = "%-" + maxLinkLength + "s | %-4d ms    | ";
        String separator = "-".repeat(maxLinkLength + 25);

        IO.println("\n================ TEST SUMMARY ================");
        IO.println(String.format(headerFormat, "Method Reference", "Time", "Status"));
        IO.println(separator);

        for (TestMethodResult methodResult : overallResult.getResults()) {
            String status = methodResult.passed() ? "PASSED" : "FAILED";
            Color statusColor = methodResult.passed() ? Color.GREEN : Color.RED;

            String clickableLink = String.format("%s(%s:%d)", methodResult.testMethodData().fullRealMethodName(), methodResult.testMethodData().instanceClassName(), methodResult.lineNumber());

            TestPrint.printColored(String.format(rowFormat, clickableLink, methodResult.durationMs()), statusColor);
            TestPrint.printColoredln(status, statusColor);
        }

        IO.println(separator);

        long totalPassed = overallResult.getResults().stream().filter(TestMethodResult::passed).count();
        long totalFailed = overallResult.getResults().stream().filter(r -> !r.passed()).count();
        long totalTime = overallResult.getResults().stream().mapToLong(TestMethodResult::durationMs).sum();

        IO.print("Final Results: " + totalPassed + " Passed, ");
        if (totalFailed > 0) {
            TestPrint.printColored(totalFailed + " Failed", Color.RED);
        } else {
            IO.print("0 Failed");
        }
        IO.println(" (Total Time: " + totalTime + " ms)");
    }

    public static void printMethodStart(TestMethodData testMethodData) {
        IO.print("------ Running ");
        TestPrint.printColored(testMethodData.fullRealMethodName(), Color.MAGENTA);
        IO.println("... ------");
    }

    public static void printMethodFinish(TestMethodResult testMethodResult) {
        IO.print("------ Finished ");
        Color statusColor = testMethodResult.passed() ? Color.GREEN : Color.RED;
        String statusText = testMethodResult.passed() ? " PASSED" : " FAILED";

        TestPrint.printColored(testMethodResult.testMethodData().fullRealMethodName() + statusText, statusColor);
        IO.print(" (" + testMethodResult.durationMs() + "ms) ------");

        if (!testMethodResult.passed()) {
            IO.println(" with stacktrace");
            if (testMethodResult.error() != null) testMethodResult.error().printStackTrace(System.out);
        } else {
            IO.println("");
        }
        IO.println("\n");
    }

}
