package com.template.backtemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.template.backtemplate.auth.JwtFilter;
import com.template.backtemplate.repository.UserRepository;
import com.template.backtemplate.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final JwtFilter jwtFilter;
        private final UserRepository userRepository;

        @Autowired
        public SecurityConfig(JwtFilter jwtFilter, UserRepository userRepository) {
                this.jwtFilter = jwtFilter;
                this.userRepository = userRepository;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .authorizeHttpRequests(requests -> requests
                                                .requestMatchers("api/public/**").permitAll()
                                                .anyRequest().authenticated());
                http.csrf(csrf -> csrf.disable());
                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        CustomUserDetailsService customUserDetailsService,
                        PasswordEncoder passwordEncoder) {
                DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
                authenticationProvider.setUserDetailsService(customUserDetailsService);
                authenticationProvider.setPasswordEncoder(passwordEncoder);

                return new ProviderManager(authenticationProvider);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService() {
                return new CustomUserDetailsService(userRepository);
        }
}