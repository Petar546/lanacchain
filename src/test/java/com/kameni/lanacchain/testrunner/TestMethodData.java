package com.kameni.lanacchain.testrunner;

import com.kameni.lanacchain.testrunner.annotations.Test;

import java.lang.reflect.Method;
import java.util.Objects;

public record TestMethodData(
        Class<?> testClazz,
        Method method,
        String instanceClassName,
        int lineNumber
){

    public String methodName() {
        return method.getName();
    }

    public String instanceFileName() {
        return instanceClassName + ".java";
    }

    /**
     *
     * @return methodName from annotation and without test__ prefix
     */
    public String realTestMethodName() {
        Test annotation = method.getAnnotation(Test.class);

        String realMethodName;
        if (!Objects.equals(annotation.name(), "")){
            realMethodName = annotation.name();
        }else{
            realMethodName = method.getName().replace("test__", "");
        }
        return realMethodName;
    }

    /**
     * requires object instance because cant get class in any other way
     * (e.g. Class.test__foo -> Class.foo )
     * @return the full real method name
     */
    public String fullRealMethodName() {
        String instanceClassName = testClazz.getSimpleName();
        return instanceClassName + "." + realTestMethodName();
    }
}