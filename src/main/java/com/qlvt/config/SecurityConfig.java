package com.qlvt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/qr/public/material/**").permitAll()
                        .requestMatchers("/qr/internal/material/**").hasAnyRole("ADMIN", "WAREHOUSE_STAFF", "MANAGER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/departments/**").hasRole("ADMIN")
                        .requestMatchers("/warehouses/**", "/locations/**").hasAnyRole("ADMIN", "WAREHOUSE_STAFF")
                        .requestMatchers("/materials/new", "/materials/*/edit", "/materials/*/delete").hasAnyRole("ADMIN", "WAREHOUSE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/materials/**").hasAnyRole("ADMIN", "WAREHOUSE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/materials", "/materials/**")
                                .hasAnyRole("ADMIN", "WAREHOUSE_STAFF", "MANAGER", "DEPARTMENT_STAFF", "DEPARTMENT_HEAD")
                        .requestMatchers("/stock/**", "/receipts/**", "/batches/**", "/issues/**", "/storage-monitoring/**",
                                "/inventory-counts/**", "/stock-adjustments/**", "/stock-transfers/**",
                                "/recalls/**", "/destructions/**").hasAnyRole("ADMIN", "WAREHOUSE_STAFF", "MANAGER")
                        .requestMatchers("/purchases/**").hasAnyRole("ADMIN", "WAREHOUSE_STAFF", "MANAGER", "PROCUREMENT", "ACCOUNTANT")
                        .requestMatchers("/department-stocks/my-department", "/department-stocks/use", "/department-stocks/report-issue")
                                .hasAnyRole("ADMIN", "DEPARTMENT_STAFF", "DEPARTMENT_HEAD", "WAREHOUSE_STAFF", "MANAGER")
                        .requestMatchers("/department-stocks/**", "/department-returns/**")
                                .hasAnyRole("ADMIN", "DEPARTMENT_STAFF", "DEPARTMENT_HEAD", "WAREHOUSE_STAFF", "MANAGER")
                        .requestMatchers("/price-histories/**", "/price-alerts/**")
                                .hasAnyRole("ADMIN", "ACCOUNTANT", "PROCUREMENT", "MANAGER")
                        .requestMatchers("/attachments/**").authenticated()
                        .requestMatchers("/notifications/**").authenticated()
                        .requestMatchers("/requests/*/approve-department").hasAnyRole("ADMIN", "DEPARTMENT_HEAD")
                        .requestMatchers("/requests/*/approve-warehouse").hasAnyRole("ADMIN", "WAREHOUSE_STAFF")
                        .requestMatchers("/requests/new", "/requests/my/**").hasAnyRole("ADMIN", "DEPARTMENT_STAFF", "DEPARTMENT_HEAD")
                        .requestMatchers("/requests/**").authenticated()
                        .requestMatchers("/reports/**").hasAnyRole("ADMIN", "ACCOUNTANT", "MANAGER", "WAREHOUSE_STAFF")
                        .requestMatchers("/api/chatbot/**", "/chatbot/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());
        http.csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/api/chatbot/**")));
        http.exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));
        return http.build();
    }
}
