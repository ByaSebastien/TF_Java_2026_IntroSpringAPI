package be.bstorm.tf_java_2026_introspringapi.api.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class MinAgeValidator implements ConstraintValidator<MinAge, LocalDate> {

    private int min;

    @Override
    public void initialize(MinAge constraintAnnotation) {
        this.min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        LocalDate now = LocalDate.now();
        LocalDate minDate = now.minusYears(min);

        return value.isBefore(minDate);
    }
}
