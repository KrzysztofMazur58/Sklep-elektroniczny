package com.example.sklepElektroniczny.security;

import com.example.sklepElektroniczny.entity.AppRole;
import com.example.sklepElektroniczny.entity.Role;
import com.example.sklepElektroniczny.entity.User;
import com.example.sklepElektroniczny.repository.RoleRepository;
import com.example.sklepElektroniczny.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthEntryPoint jwtAuthEntryPoint) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

        authenticationProvider.setUserDetailsService(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint)) // Poprawione
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                //.requestMatchers("/api/admin/**").permitAll()
                                //.requestMatchers("/api/public/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/api/test/**").permitAll()
                                .requestMatchers("/images/**").permitAll()
                                .anyRequest().authenticated()
                );

        http.authenticationProvider(daoAuthenticationProvider());

        http.addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().requestMatchers("/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**"));
    }

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                // Pobranie lub stworzenie ról i natychmiastowe zapisanie ich w bazie
                Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                        .orElseGet(() -> roleRepository.saveAndFlush(new Role(AppRole.ROLE_USER)));

                Role workerRole = roleRepository.findByRoleName(AppRole.ROLE_WORKER)
                        .orElseGet(() -> roleRepository.saveAndFlush(new Role(AppRole.ROLE_WORKER)));

                Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                        .orElseGet(() -> roleRepository.saveAndFlush(new Role(AppRole.ROLE_ADMIN)));

                Set<Role> userRoles = Set.of(userRole);
                Set<Role> workerRoles = Set.of(workerRole);
                Set<Role> adminRoles = Set.of(userRole, workerRole, adminRole);

                if (!userRepository.existsByUserName("user1")) {
                    User user1 = new User("user1", "user1@example.com", passwordEncoder.encode("password1"));
                    user1 = userRepository.saveAndFlush(user1); // Zapisz użytkownika przed przypisaniem roli
                    user1.setRoles(userRoles);
                    userRepository.saveAndFlush(user1);
                }

                if (!userRepository.existsByUserName("seller1")) {
                    User seller1 = new User("seller1", "seller1@example.com", passwordEncoder.encode("password2"));
                    seller1 = userRepository.saveAndFlush(seller1);
                    seller1.setRoles(workerRoles);
                    userRepository.saveAndFlush(seller1);
                }

                if (!userRepository.existsByUserName("admin")) {
                    User admin = new User("admin", "admin@example.com", passwordEncoder.encode("adminPass"));
                    admin = userRepository.saveAndFlush(admin);
                    admin.setRoles(adminRoles);
                    userRepository.saveAndFlush(admin);
                }

            } catch (Exception e) {
                System.err.println("Błąd inicjalizacji danych: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }


}


