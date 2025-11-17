package org.finsible.backend.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class AtLeastOneFieldNotNullValidator
        implements ConstraintValidator<AtLeastOneFieldNotNull, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        Field[] fields = value.getClass().getDeclaredFields();

        for (Field field : fields) {
            // Skip validation group marker interfaces
            if (field.getType().isInterface() ||
                    field.getName().contains("$")) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object fieldValue = field.get(value);
                if (fieldValue != null) {
                    return true; // At least one field is non-null
                }
            } catch (IllegalAccessException e) {
                // Skip this field
            }
        }

        return false; // All fields are null
    }
}