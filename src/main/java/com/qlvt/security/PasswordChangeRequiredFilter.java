package com.qlvt.security;

import com.qlvt.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {
    private final AppUserRepository userRepository;

    public PasswordChangeRequiredFilter(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || isAllowedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean mustChange = userRepository.findByUsername(authentication.getName())
                .map(user -> user.isMustChangePassword() && user.isEnabled() && !user.isDeleted())
                .orElse(false);
        if (!mustChange) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().startsWith("/api/")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Password change required");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/profile?passwordRequired");
    }

    private boolean isAllowedPath(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.equals("/login")
                || path.equals("/logout")
                || path.equals("/profile")
                || path.equals("/profile/password")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/error");
    }
}
