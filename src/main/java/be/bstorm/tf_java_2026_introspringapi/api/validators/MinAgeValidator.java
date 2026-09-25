package be.bstorm.tf_java_2026_introspringapi.api.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * Implémentation du validateur personnalisé @MinAge.
 * Calcule l'âge en années révolues basé sur la date actuelle.
 * Échoue si l'utilisateur n'a pas atteint l'âge minimum.
 */
public class MinAgeValidator implements ConstraintValidator<MinAge, LocalDate> {

    private int min;

    /**
     * Initialisation du validateur depuis l'annotation.
     * @param constraintAnnotation annotation @MinAge
     */
    @Override
    public void initialize(MinAge constraintAnnotation) {
        this.min = constraintAnnotation.min();
    }

    /**
     * Valide que la date de naissance correspond à au moins X ans révolus.
     * @param value date de naissance
     * @param context contexte de validation
     * @return true si âge >= min
     */
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        LocalDate now = LocalDate.now();
        LocalDate minDate = now.minusYears(min);

        return value.isBefore(minDate);
    }
}
