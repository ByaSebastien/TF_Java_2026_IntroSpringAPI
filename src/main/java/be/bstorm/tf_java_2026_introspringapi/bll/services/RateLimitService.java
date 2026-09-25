package be.bstorm.tf_java_2026_introspringapi.bll.services;

import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.RateLimitException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    public void checkRateLimit(String identifier, String endpoint, int maxRequests, int windowSeconds) {
        String key = generateKey(identifier, endpoint);

        long now = System.currentTimeMillis();

        String tokenData = redisTemplate.opsForValue().get(key);

        int currentTokens = maxRequests;
        long lastRefillTime = now;

        if (tokenData != null) {
            String[] parts = tokenData.split(":");
            currentTokens = Integer.parseInt(parts[0]);
            lastRefillTime = Long.parseLong(parts[1]);
        }

        // Refill tokens based on time elapsed
        long timeSinceLastRefill = now - lastRefillTime;
        long tokensToAdd = (timeSinceLastRefill / (windowSeconds * 1000L)) * maxRequests;

        if (tokensToAdd > 0) {
            currentTokens = Math.min(maxRequests, (int) (currentTokens + tokensToAdd));
            lastRefillTime = now;
        }

        if (currentTokens <= 0) {
            long retryAfter = Math.max(1, (windowSeconds * 1000L - (now - lastRefillTime)) / 1000L);
            throw new RateLimitException(
                    "Rate limit exceeded for " + identifier + " on " + endpoint,
                    0,
                    retryAfter
            );
        }

        // Consume one token
        currentTokens--;

        // Store updated state
        String newTokenData = currentTokens + ":" + lastRefillTime;
        redisTemplate.opsForValue().set(key, newTokenData);
        redisTemplate.expire(key, java.time.Duration.ofSeconds(windowSeconds));
    }

    public int getRemainingTokens(String identifier, String endpoint, int maxRequests, int windowSeconds) {
        String key = generateKey(identifier, endpoint);

        long now = System.currentTimeMillis();

        String tokenData = redisTemplate.opsForValue().get(key);

        int currentTokens = maxRequests;
        long lastRefillTime = now;

        if (tokenData != null) {
            String[] parts = tokenData.split(":");
            currentTokens = Integer.parseInt(parts[0]);
            lastRefillTime = Long.parseLong(parts[1]);
        }

        // Refill tokens based on time elapsed
        long timeSinceLastRefill = now - lastRefillTime;
        long tokensToAdd = (timeSinceLastRefill / (windowSeconds * 1000L)) * maxRequests;

        if (tokensToAdd > 0) {
            currentTokens = Math.min(maxRequests, (int) (currentTokens + tokensToAdd));
        }

        return Math.max(0, currentTokens);
    }

    private String generateKey(String identifier, String endpoint) {
        return "rate_limit:" + identifier + ":" + endpoint;
    }
}
