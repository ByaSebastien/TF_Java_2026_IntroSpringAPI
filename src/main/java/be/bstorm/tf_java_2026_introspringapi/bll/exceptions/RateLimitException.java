package be.bstorm.tf_java_2026_introspringapi.bll.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Levée quand un utilisateur/IP a dépassé son quota de requêtes.
 * Retourne 429 TOO_MANY_REQUESTS avec info de retry.
 * Les headers X-RateLimit-Remaining et Retry-After informent le client.
 */
public class RateLimitException extends IntroSpringApiException {

    private final int remainingTokens;
    private final long retryAfterSeconds;

    /**
     * @param message message d'erreur
     * @param remainingTokens nombre de jetons restants
     * @param retryAfterSeconds délai avant nouvelle tentative
     */
    public RateLimitException(String message, int remainingTokens, long retryAfterSeconds) {
        super(HttpStatus.TOO_MANY_REQUESTS, message, "Rate limit exceeded");
        this.remainingTokens = remainingTokens;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRemainingTokens() {
        return remainingTokens;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
