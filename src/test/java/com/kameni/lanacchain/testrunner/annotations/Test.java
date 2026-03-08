package com.kameni.lanacchain.testrunner.annotations;

import com.kameni.lanacchain.testrunner.exceptions.TestPassedSignal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {

}
