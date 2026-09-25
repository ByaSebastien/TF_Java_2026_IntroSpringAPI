package be.bstorm.tf_java_2026_introspringapi.bll.services.impls;

import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.RateLimitException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Gère la limitation de débit (rate limiting) via Redis.
 * Implémente un bucket à jetons avec refill périodique.
 * Chaque utilisateur/IP a une limite de requêtes par fenêtre de temps.
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Vérifie et applique la limite de débit.
     * Lance une exception si la limite est dépassée.
     * @param identifier clé unique (username ou IP)
     * @param endpoint route/méthode contrôlée
     * @param maxRequests nombre max de requêtes
     * @param windowSeconds fenêtre de temps en secondes
     * @throws RateLimitException si la limite est dépassée
     */
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

    /**
     * Retourne le nombre de jetons restants.
     * @param identifier clé unique
     * @param endpoint route/méthode
     * @param maxRequests nombre max de requêtes
     * @param windowSeconds fenêtre de temps
     * @return nombre de jetons disponibles
     */
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
