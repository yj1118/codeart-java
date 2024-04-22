package apros.codeart.dto.serialization.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DTOClass {
	DTOSerializableMode mode() default DTOSerializableMode.General;
}
