package be.bstorm.tf_java_2026_introspringapi.api.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour appliquer le rate limiting sur une méthode HTTP.
 * Utilisée avec RateLimitAspect pour limiter les appels par utilisateur/IP.
 * Exemple: @RateLimit(maxRequests = 5, windowSeconds = 60)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * Nombre maximal de requêtes autorisées.
     * @return nombre de requêtes
     */
    int maxRequests() default 100;
    
    /**
     * Fenêtre de temps en secondes.
     * @return nombre de secondes
     */
    int windowSeconds() default 60;
}
