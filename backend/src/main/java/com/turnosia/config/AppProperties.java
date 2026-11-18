package com.turnosia.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Security security, Admin admin) {

	public record Security(Cors cors, Login login) {

		public record Cors(List<String> allowedOrigins) {
		}

		public record Login(int maxAttempts, long windowMinutes, long lockoutMinutes) {
		}
	}

	public record Admin(String username, String password) {
	}
}
