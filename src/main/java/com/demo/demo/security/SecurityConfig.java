package com.demo.demo.security;


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

import static org.springframework.security.config.Customizer.withDefaults;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthEntryPoint authEntryPoint;
  private final CustoUserDetailsService userDetailsService;

  public SecurityConfig(CustoUserDetailsService userDetailsService, JwtAuthEntryPoint authEntryPoint) {
    this.userDetailsService = userDetailsService;
    this.authEntryPoint = authEntryPoint;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http.cors(withDefaults())
            // Disable Cross-Site Request Forgery (CSRF) protection
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(authEntryPoint))
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                    // Swagger/OpenAPI - Accès public
                    .requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/swagger-resources/**",
                            "/webjars/**",
                            "/configuration/ui",
                            "/configuration/security"
                    ).permitAll()
                    // API Auth - Accès public
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/cours-modules/**").permitAll()  // ← Ajouté ici pour autoriser /api/cours-modules sans auth
                    ////////////////hibaaa//////////////
                    .requestMatchers("/user/**").permitAll()  // ← TOUS LES ENDPOINTS USER
                    .requestMatchers("/test/**").permitAll()  // ← ENDPOINTS TEST
                    .requestMatchers("/user/updateuser/**").authenticated()            // Modif besoin auth
                    .requestMatchers("/user/delete/**").authenticated()                // Suppression besoin auth
                    .requestMatchers("/user/saveall", "/user/addwithconfpassword", "/user/addWTUN").authenticated()
                    .requestMatchers("/api/cours-modules").authenticated()
                    .requestMatchers("/api/cours-modules/").authenticated()
                    // Ajout des endpoints pour Simulation - Nécessite authentification
// Autoriser toutes les méthodes HTTP pour les endpoints de simulation
                    .requestMatchers(HttpMethod.POST, "/api/simulations/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/simulations/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/simulations/**").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/api/simulations/**").permitAll()
                    // Ajout des endpoints pour CarnetOrdre - Nécessite authentification
                    .requestMatchers("/api/carnets-ordre/**").permitAll()  // ← Ajouté pour tous les endpoints de CarnetOrdre
                    .requestMatchers(HttpMethod.POST, "/api/carnet-ordre/simulation/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/carnet-ordre/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/carnet-ordre/**").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/api/carnet-ordre/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/carnet-ordre/*/execute").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/carnet-ordre/simulation/*/pending").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/carnet-ordre/simulation/*/match").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/carnet-ordre/*/execute").permitAll()
                    .requestMatchers("/api/transactions/**").permitAll()  // FIX : Tout public pour tests
                    .requestMatchers(HttpMethod.POST,"/api/transactions/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/transactions").permitAll()  // Create
                    .requestMatchers(HttpMethod.GET, "/api/transactions/**").permitAll()  // Get all et by ID
                    .requestMatchers(HttpMethod.PUT, "/api/transactions/**").permitAll()  // Update
                    .requestMatchers(HttpMethod.DELETE, "/api/transactions/**").permitAll()  // Delete
                    .requestMatchers("/api/actifs/**").permitAll()  // FIX : Tout public pour tests
                    .requestMatchers(HttpMethod.POST, "/api/actifs").permitAll()  // Create
                    .requestMatchers(HttpMethod.GET, "/api/actifs/**").permitAll()  // Get all et by ID
                    .requestMatchers(HttpMethod.PUT, "/api/actifs/**").permitAll()  // Update
                    .requestMatchers(HttpMethod.DELETE, "/api/actifs/**").permitAll()  // Delete
                    // FIX : Explicit pour /analyse (GET /api/simulations/{id}/analyse) - Public sans auth
                    .requestMatchers(HttpMethod.GET, "/api/simulations/*/analyse").permitAll()






                    // .requestMatchers("/post/**").hasAuthority(UserRoleName.ADMIN.name())
                    // .requestMatchers("/image/**").hasAnyAuthority(UserRoleName.USER.name(), UserRoleName.ADMIN.name())
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated())
    ;

   http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

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
  public JWTAuthenticationFilter jwtAuthenticationFilter() {
    return new JWTAuthenticationFilter();
  }
}
