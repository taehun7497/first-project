package com.example.personal.project.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers("/**").permitAll() // 공개 경로
                        .anyRequest().authenticated() // 그 외의 경로는 인증 필요
                )
                .formLogin((formLogin) -> formLogin
                        .loginPage("/user/login") // 사용자 정의 로그인 페이지
                        .defaultSuccessUrl("/", true) // 로그인 성공 후 리디렉션 페이지
                        .permitAll() // 모든 사용자에게 허용
                )
                .logout((logout) -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout")) // 로그아웃 경로
                        .logoutSuccessUrl("/") // 로그아웃 후 리디렉션
                        .invalidateHttpSession(true) // 세션 무효화
                        .permitAll() // 모든 사용자에게 허용
                )
        ;
        return http.build();
    }
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager(); // 인증 관리자
    }
}