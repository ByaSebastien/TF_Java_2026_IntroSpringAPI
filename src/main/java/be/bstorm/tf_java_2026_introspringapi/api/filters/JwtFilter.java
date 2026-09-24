package be.bstorm.tf_java_2026_introspringapi.api.filters;

import be.bstorm.tf_java_2026_introspringapi.api.model.user.UserContext;
import be.bstorm.tf_java_2026_introspringapi.api.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            UserContext user = jwtUtils.getUser(token);

            UsernamePasswordAuthenticationToken upt = new UsernamePasswordAuthenticationToken(
                    user,
                    token,
                    user.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(upt);
        }

        filterChain.doFilter(request, response);
    }
}
