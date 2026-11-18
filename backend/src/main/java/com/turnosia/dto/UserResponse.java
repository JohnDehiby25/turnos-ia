package com.turnosia.dto;

import org.springframework.security.core.Authentication;

public record UserResponse(String username, String role) {

	public static UserResponse from(Authentication authentication) {
		String role = authentication.getAuthorities().stream()
				.map(authority -> authority.getAuthority())
				.filter(authority -> authority.startsWith("ROLE_"))
				.map(authority -> authority.substring("ROLE_".length()))
				.findFirst()
				.orElse("USER");
		return new UserResponse(authentication.getName(), role);
	}
}
