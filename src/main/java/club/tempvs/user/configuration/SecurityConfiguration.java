package club.tempvs.user.configuration;

import club.tempvs.user.filter.AuthFilter;
import club.tempvs.user.filter.OAuthUserFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthFilter authFilter,
            OAuthUserFilter oAuthUserFilter,
            AuthenticationSuccessHandler oauthSuccessHandler
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(authFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(oAuthUserFilter, BasicAuthenticationFilter.class)
                .sessionManagement(
                        sm -> sm.sessionCreationPolicy((SessionCreationPolicy.IF_REQUIRED))
                )
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/", "/register", "/verify/**", "/login", "/swagger-ui/**", "/v3/api-docs/**",
                                "/oauth2/**", "/login/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        );

        if (StringUtils.hasText(googleClientId)) {
            http.oauth2Login(oauth2 -> oauth2.successHandler(oauthSuccessHandler));
        }

        return http.build();
    }
}
