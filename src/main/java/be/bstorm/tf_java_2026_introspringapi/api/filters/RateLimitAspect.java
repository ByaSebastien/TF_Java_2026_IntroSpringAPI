package be.bstorm.tf_java_2026_introspringapi.api.filters;

import be.bstorm.tf_java_2026_introspringapi.api.model.user.UserContext;
import be.bstorm.tf_java_2026_introspringapi.api.utils.RateLimit;
import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.RateLimitException;
import be.bstorm.tf_java_2026_introspringapi.bll.services.impls.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspect AOP pour appliquer le rate limiting.
 * Intercepte les méthodes annotées @RateLimit et valide la limite de débit.
 * Identifie les utilisateurs par ID ou par IP pour les non-authentifiés.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitService rateLimitService;

    /**
     * Intercept les appels de méthode annotée @RateLimit AVANT exécution.
     * Lève RateLimitException si la limite est dépassée.
     * @param joinPoint contexte AOP
     * @param rateLimit annotation @RateLimit
     * @throws RateLimitException si limite dépassée
     */
    @Before("@annotation(rateLimit)")
    public void enforceRateLimit(JoinPoint joinPoint, RateLimit rateLimit) throws RateLimitException {
        String identifier = extractIdentifier();
        String endpoint = extractEndpoint(joinPoint);

        log.debug("Rate limit check for {} on {}", identifier, endpoint);

        try {
            rateLimitService.checkRateLimit(
                    identifier,
                    endpoint,
                    rateLimit.maxRequests(),
                    rateLimit.windowSeconds()
            );
        } catch (RateLimitException e) {
            log.warn("Rate limit exceeded for {} on {}", identifier, endpoint);
            throw e;
        }
    }

    /**
     * Extrait un identifiant unique pour le rate limiting.
     * Utilisateurs authentifiés: user_<id>
     * Non authentifiés: ip_<address>
     * @return identifiant unique
     */
    private String extractIdentifier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserContext userContext) {
                return "user_" + userContext.id();
            }
        }

        return "ip_" + getClientIp();
    }

    /**
     * Récupère l'adresse IP du client.
     * Vérifieet X-Forwarded-For (proxy), X-Real-IP (nginx), puis RemoteAddr.
     * @return adresse IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String remoteAddr = request.getHeader("X-Real-IP");
            if (remoteAddr != null && !remoteAddr.isEmpty()) {
                return remoteAddr;
            }

            return request.getRemoteAddr();
        }

        return "unknown";
    }

    /**
     * Extrait le nom de l'endpoint depuis le JoinPoint.
     * Format: com.example.Controller.methodName
     * @param joinPoint contexte AOP
     * @return nom de l'endpoint
     */
    private String extractEndpoint(JoinPoint joinPoint) {
        return joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
    }
}
