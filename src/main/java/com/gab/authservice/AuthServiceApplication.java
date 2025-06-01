package com.gab.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class AuthServiceApplication {
	
	public static void main(String[] args) {
		// Load .env file for local development BEFORE Spring Boot starts
		// This ensures environment variables are available during Spring initialization
		boolean isProd = isProductionMode(args);
		
		if (!isProd) {
			// Local development mode - load .env file
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME", ""));
			System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD", ""));
			System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET", ""));
			System.setProperty("PAT_TOKEN", dotenv.get("PAT_TOKEN", ""));
			System.out.println("Local development mode: .env file loaded");
		} else {
			System.out.println("Production mode: Using AWS Secrets Manager");
		}
		
		SpringApplication.run(AuthServiceApplication.class, args);
	}
	
	private static boolean isProductionMode(String[] args) {
		// Check if production profile is explicitly set
		for (String arg : args) {
			if (arg.contains("spring.profiles.active=prod")) {
				return true;
			}
		}
		// Check system property
		String activeProfiles = System.getProperty("spring.profiles.active");
		return activeProfiles != null && activeProfiles.contains("prod");
	}

}
