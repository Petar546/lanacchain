package com.kameni.lanacchain.testrunner;

public class TestPrint {

    public static void printColoredln(Object obj, Color color){
        printColored(obj + "\n", color);
    }

    public static void printColored(Object obj, Color color){
        IO.print(color.toString() + obj + Color.RESET);
    }

}
