package org.finsible.backend.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldNotNullValidator.class)
@Documented
public @interface AtLeastOneFieldNotNull {
    String message() default "At least one field must be provided for update";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}