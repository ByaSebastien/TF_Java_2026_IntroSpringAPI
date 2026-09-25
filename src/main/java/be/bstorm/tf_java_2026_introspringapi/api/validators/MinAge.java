package be.bstorm.tf_java_2026_introspringapi.api.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation personnalisée pour valider un âge minimum sur une LocalDate.
 * Utilisée pour vérifier que l'utilisateur a au moins X ans révolus.
 * Paramétrable: @MinAge(min = 18)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = MinAgeValidator.class)
public @interface MinAge {

    /**
     * Âge minimum requis en années.
     * @return âge min
     */
    int min() default 12;

    /**
     * Message d'erreur si la validation échoue.
     * @return message
     */
    String message() default "Age must be at least {min} years old";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
