package com.gab.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class AuthServiceApplication {
	
	public static void main(String[] args) {
		// When running the application with -Dspring.profiles.active=local flag, Spring Boot will:
		// 1. First load application.properties as the base configuration
		// 2. Then load application-local.properties, which overrides the base configuration
		// 3. The active profile is stored in Environment and can be accessed via env.matchesProfiles()
		ConfigurableApplicationContext context = SpringApplication.run(AuthServiceApplication.class, args);
		Environment env = context.getEnvironment();
		
		// Load .env file only for local development profile
		if (env.matchesProfiles("local")) {
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME", ""));
			System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD", ""));
			System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET", ""));
			System.out.println("Local development mode: .env file loaded");
		} else {
			System.out.println("Production mode: Using AWS Secrets Manager");
		}
	}

}
