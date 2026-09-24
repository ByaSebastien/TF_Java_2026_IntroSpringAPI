package be.bstorm.tf_java_2026_introspringapi.api.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = MinAgeValidator.class)
public @interface MinAge {

    int min() default 12;

    String message() default "Age must be at least {min} years old";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
