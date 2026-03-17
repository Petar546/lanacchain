package com.kameni.lanacchain.testrunner.helpers;

import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.annotations.TestClass;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TestHelpers {

    public static List<Object> findTests(String packageName) {
        List<Object> instances = new ArrayList<>();
        try {
            String path = packageName.replace('.', '/');
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL resource = loader.getResource(path);

            if (resource != null) {
                File directory = new File(resource.getFile());
                scanDirectory(directory, packageName, instances);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instances;
    }

    private static void scanDirectory(File directory, String packageName, List<Object> instances) throws Exception {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), instances);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(className);

                if (hasTestMethods(clazz)) {
                    try {
                        instances.add(clazz.getDeclaredConstructor().newInstance());
                    } catch (NoSuchMethodException e) {
                        //skip
                    }
                }
            }
        }
    }

    private static boolean hasTestMethods(Class<?> clazz) {
        if (clazz.isAnnotationPresent(TestClass.class)) return true;

        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class)) return true;
        }
        return false;
    }
}
