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
                    ////////////////hibaaa//////////////
                    .requestMatchers("/user/**").permitAll()  // ← TOUS LES ENDPOINTS USER
                    .requestMatchers("/test/**").permitAll()  // ← ENDPOINTS TEST
                    .requestMatchers("/user/updateuser/**").authenticated()            // Modif besoin auth
                    .requestMatchers("/user/delete/**").authenticated()                // Suppression besoin auth
                    .requestMatchers("/user/saveall", "/user/addwithconfpassword", "/user/addWTUN").authenticated()




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
