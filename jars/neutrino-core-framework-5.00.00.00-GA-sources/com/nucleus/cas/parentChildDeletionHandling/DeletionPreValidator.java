package com.nucleus.cas.parentChildDeletionHandling;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DeletionPreValidator {

    String DEFAULT_FIELD = "code";

    String fieldName() default DEFAULT_FIELD;

}
