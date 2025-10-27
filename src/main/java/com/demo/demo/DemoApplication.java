package com.demo.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Pour temps réel 5min
public class DemoApplication {

	public static void main(String[] args) {

		SpringApplication.run(DemoApplication.class, args);
// AJOUT CORRECTION : Prints startup comme Python
		System.out.println("🚀 SYSTÈME DE TRADING MULTI-DEVISES - Spring Boot");
		System.out.println("💡 6 paires majeures | 1h + 4h | Mise à jour 5min");
		System.out.println("🎯 Pour salle de trading - Aide à la décision");
		System.out.println("⏸️ Ctrl+C pour arrêter");
	}	}



