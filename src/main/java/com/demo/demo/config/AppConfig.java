package com.demo.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import java.util.Collections;

/**
 * Configuration beans globaux.
 * RestTemplate : Pour fetch données Yahoo Finance (temps réel, remplace yfinance).
 * FIX : Interceptor pour headers par défaut (User-Agent anti-blocage Yahoo, évite fallback).
 */
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());  // Avec factory pour timeout

        // FIX : Interceptor pour ajouter headers par défaut à chaque requête (pas de setDefaultHeaders)
        ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");  // Anti-blocage Yahoo
            request.getHeaders().addAll(headers);
            return execution.execute(request, body);
        };
        restTemplate.getInterceptors().add(interceptor);  // Ajoute interceptor

        return restTemplate;
    }

    // Factory pour timeout (évite hang sur fetch lent)
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 5s
        factory.setReadTimeout(10000);    // 10s
        return factory;
    }
}