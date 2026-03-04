package com.kameni.lanacchain.testrunner.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // Must be RUNTIME to be seen by the runner
@Target(ElementType.METHOD)        // Restricts use to methods
public @interface Test {

}
