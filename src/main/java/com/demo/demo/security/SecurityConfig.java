package com.demo.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthEntryPoint authEntryPoint;
  private final CustoUserDetailsService userDetailsService;

  @Autowired
  private JWTAuthenticationFilter jwtAuthenticationFilter;  // Injection auto

  public SecurityConfig(CustoUserDetailsService userDetailsService, JwtAuthEntryPoint authEntryPoint) {
    this.userDetailsService = userDetailsService;
    this.authEntryPoint = authEntryPoint;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(authEntryPoint))
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // FIX : Login public (full /api/auth/** pour matcher /api/auth/login)
                    .requestMatchers("/api/auth/**").permitAll()  // ← LE FIX EN 1 LIGNE
                    // Autres public (add user, etc. – ajoute /api si pas déjà)
                    .requestMatchers("/api/user/add", "/api/user/addwithconfpassword", "/api/user/all", "/api/user/findbyid/**", "/api/user/findbyusername/**").permitAll()
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/configuration/ui", "/configuration/security").permitAll()
                    .requestMatchers("/test/**").permitAll()
                    // Simulations, CRUD, etc. (inchangé)
                    .requestMatchers(HttpMethod.POST, "/api/simulations").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/simulations").permitAll()
                    .requestMatchers("/api/simulations/**").hasAnyAuthority("TRADER", "ADMIN")
                    .requestMatchers("/api/transactions/**").hasAnyAuthority("TRADER", "ADMIN")
                    .requestMatchers("/api/actifs/**").hasAnyAuthority("TRADER", "ADMIN")
                    .requestMatchers("/api/carnets-ordre/**").hasAnyAuthority("TRADER", "ADMIN")
                    .requestMatchers("/api/cours-modules/**").hasAnyAuthority("TRADER", "ADMIN")
                    .requestMatchers("/api/user/updateuser/**").authenticated()
                    .requestMatchers("/api/user/delete/**").authenticated()
                    .requestMatchers("/api/user/saveall", "/api/user/addwithconfpassword", "/api/user/addWTUN").authenticated()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().permitAll()
            );
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
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
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}