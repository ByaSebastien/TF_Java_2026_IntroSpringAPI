package be.bstorm.tf_java_2026_introspringapi.bll.exceptions;

import org.springframework.http.HttpStatus;

public class RateLimitException extends IntroSpringApiException {

    private final int remainingTokens;
    private final long retryAfterSeconds;

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
