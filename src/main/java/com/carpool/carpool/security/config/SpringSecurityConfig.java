package com.carpool.carpool.security.config;

import java.util.Arrays;

import com.carpool.carpool.security.filter.RecaptchaFilter;
import com.carpool.carpool.security.handler.JwtAuthenticationEntryPoint;
import com.carpool.carpool.service.auth.blacklist.IAuthBlacklistService;
import com.carpool.carpool.service.auth.recaptcha.IAuthRecaptchaService;
import com.carpool.carpool.service.user.account.IUserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.security.filter.JwtAuthenticationFilter;
import com.carpool.carpool.security.filter.JwtValidationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled=true)
public class SpringSecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final IAuthBlacklistService authBlacklistService;
    private final IAuthRecaptchaService authRecaptchaService;
    private final UserRepository userRepository;
    private final IUserAccountService userAccountService;

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) throws Exception{
        return http.authorizeHttpRequests((authz)-> authz
        .requestMatchers(HttpMethod.POST, "/users/complete-registration").authenticated()
        .requestMatchers("/users", "/users/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/users/activate-account").permitAll()
        .requestMatchers(HttpMethod.POST, "/users/resend-activation").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth-google/**").permitAll()
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/drivers/become_driver").authenticated()
        .requestMatchers(HttpMethod.POST, "/drivers").authenticated()
        .requestMatchers(HttpMethod.GET, "/vehicle-types").hasAnyRole("DRIVER", "ADMIN")
        .requestMatchers("/vehicles", "/vehicles/**").hasRole("DRIVER")
        .anyRequest().authenticated())
        .exceptionHandling(config -> config
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        )
        //.addFilterBefore(new RecaptchaFilter(authRecaptchaService), UsernamePasswordAuthenticationFilter.class)
        .addFilter(new JwtAuthenticationFilter(authenticationManager(),userRepository, userAccountService))
        .addFilter(new JwtValidationFilter(authenticationManager(), authBlacklistService, userRepository))
        .csrf(config-> config.disable())
        .cors(cors-> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(managment->managment.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type","recaptcha"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> corsBean = new FilterRegistrationBean<>(
                new CorsFilter(corsConfigurationSource()));
        corsBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return corsBean;
    }

}
