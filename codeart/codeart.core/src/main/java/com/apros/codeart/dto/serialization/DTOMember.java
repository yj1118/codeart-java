package com.apros.codeart.dto.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DTOMember {
	String name() default "";

	DTOMemberType type() default DTOMemberType.General;

	boolean blob() default false;
}
