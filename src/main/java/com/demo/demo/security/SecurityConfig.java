
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
  private JWTAuthenticationFilter jwtAuthenticationFilter; // Injection auto

  public SecurityConfig(CustoUserDetailsService userDetailsService, JwtAuthEntryPoint authEntryPoint) {
    this.userDetailsService = userDetailsService;
    this.authEntryPoint = authEntryPoint;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Intégration du 2ème : CORS explicite
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(authEntryPoint))
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // Public endpoints (gardés du 1er avec fixes)
                    .requestMatchers("/api/auth/**").permitAll()  // Login public
                    .requestMatchers("/api/user/add", "/api/user/addwithconfpassword", "/api/user/all", "/api/user/findbyid/**", "/api/user/findbyusername/**").permitAll()
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/configuration/ui", "/configuration/security").permitAll()
                    .requestMatchers("/test/**").permitAll()
                    // Simulations, CRUD, etc. (revert sécurisé du 1er + intégration 2ème)
                    .requestMatchers(HttpMethod.POST, "/api/simulations").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/simulations").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/simulations/status/**").permitAll()
                   // .requestMatchers("/api/simulations/**").hasAnyAuthority("ADMIN", "TRADER")  // Revert : Sécurisé comme 2ème
                    .requestMatchers("/api/yahoo/**").permitAll()
                    .requestMatchers("/api/yahoo/forex/**").permitAll()
                    .requestMatchers( "/api/yahoo/forex/candles/**").permitAll()

                    .requestMatchers("/api/alpha/**").permitAll() // NOUVEAU : /live-tech, /candles-tech/{symbol}
                    .requestMatchers("/api/finhub/**").permitAll() // NOUVEAU : /live-energy, /candles-energy/{symbol}
                    .requestMatchers("/api/twelvedata/**").permitAll() // NOUVEAU : /live-commodities, /candles-commodities/{symbol}
                    .requestMatchers("/testt/**").permitAll()
                    .requestMatchers("/api/simulations/**").permitAll() // ← LE FIX : Autorise tout (y compris PUT update)
// ==================== SIMULATIONS : TOUT AUTORISÉ (c’est ÇA qui débloque le start) ====================
                            .requestMatchers(HttpMethod.GET, "/api/simulations/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/simulations/**").permitAll()   // ← crée + start + ia-move
                            .requestMatchers(HttpMethod.PUT, "/api/simulations/**").permitAll()    // ← update
                            .requestMatchers(HttpMethod.DELETE, "/api/simulations/**").permitAll()

// OPTIONS CORS (indispensable pour le preflight du front)
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/yahoo/**").permitAll()
                    // Intégration du 2ème : Tests publics
                    .requestMatchers("/test/questions", "/test/submit").permitAll()
                    .requestMatchers("/test/latest/**").hasAnyAuthority("ADMIN", "TRADER")
                    // Intégration du 2ème : Admin only
                    .requestMatchers("/admin/**").hasAuthority("ADMIN")
                    // CRUD + IA: ADMIN ou TRADER (harmonisé avec /api/ du 1er)
                    .requestMatchers("/api/transactions/**").hasAnyAuthority("ADMIN", "TRADER")
                    .requestMatchers("/api/actifs/**").hasAnyAuthority("ADMIN", "TRADER")
                    .requestMatchers("/api/carnets-ordre/**").hasAnyAuthority("ADMIN", "TRADER")
                    .requestMatchers("/api/cours-modules/**").hasAnyAuthority("ADMIN", "TRADER")
                    .requestMatchers("/api/user/updateuser/**").authenticated()
                    .requestMatchers("/api/user/delete/**").authenticated()
                    .requestMatchers("/api/user/saveall", "/api/user/addwithconfpassword", "/api/user/addWTUN").authenticated()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated()  // Intégration du 2ème : Plus strict
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



/*
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
  private JWTAuthenticationFilter jwtAuthenticationFilter; // Injection auto

  public SecurityConfig(CustoUserDetailsService userDetailsService, JwtAuthEntryPoint authEntryPoint) {
    this.userDetailsService = userDetailsService;
    this.authEntryPoint = authEntryPoint;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS explicite
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPoint))
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // Public endpoints (login, users read/add, images, swagger, tests)
                    .requestMatchers("/api/auth/**").permitAll() // Login public
                    .requestMatchers("/api/user/add", "/api/user/addwithconfpassword", "/api/user/all",
                            "/api/user/findbyid/**", "/api/user/findbyusername/**").permitAll()
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                            "/swagger-resources/**", "/webjars/**", "/configuration/ui",
                            "/configuration/security").permitAll()
                    .requestMatchers("/test/**").permitAll() // Tests publics

                    // Simulations : FIX - Full CRUD public pour dev (POST/GET/PUT/DELETE sans auth)
                    .requestMatchers("/api/simulations/**").permitAll() // ← LE FIX : Autorise tout (y compris PUT update)
                    .requestMatchers(HttpMethod.GET, "/api/simulations/status/**").permitAll() // Redondant mais OK

                    .requestMatchers("/api/yahoo/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/yahoo/**").permitAll()

                    // Admin/Trader only (harmonisé)
                    .requestMatchers("/test/questions", "/test/submit").hasAnyAuthority("ADMIN", "TRADER")
                    .requestMatchers("/test/latest/**").hasAnyAuthority("ADMIN", "TRADER")
                    .requestMatchers("/admin/**").hasAuthority("ADMIN")
                    .requestMatchers("/api/transactions/**").hasAnyAuthority("ADMIN", "TRADER")
                    .requestMatchers("/api/actifs/**").hasAnyAuthority("ADMIN", "TRADER")
                    .requestMatchers("/api/carnets-ordre/**").hasAnyAuthority("ADMIN", "TRADER")
                    .requestMatchers("/api/cours-modules/**").hasAnyAuthority("ADMIN", "TRADER")

                    // Users updates (authenticated)
                    .requestMatchers("/api/user/updateuser/**").authenticated()
                    .requestMatchers("/api/user/delete/**").authenticated()
                    .requestMatchers("/api/user/saveall", "/api/user/addwithconfpassword", "/api/user/addWTUN").authenticated()

                    // OPTIONS CORS
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // Tout le reste : Auth required
                    .anyRequest().authenticated()
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
}*/