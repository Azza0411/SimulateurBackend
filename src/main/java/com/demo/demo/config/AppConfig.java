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

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());

        // INTERCEPTOR : Ajoute headers + LOG L'URL
        ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Content-Type", "application/json");

            request.getHeaders().addAll(headers);

            // LOGGING : Affiche l'URL appelée
            System.out.println("REST CALL → " + request.getMethod() + " " + request.getURI());

            return execution.execute(request, body);
        };

        restTemplate.getInterceptors().add(interceptor);
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(45000);  // 45s
        factory.setReadTimeout(45000);     // 45s
        return factory;
    }
}