package com.turnosia.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final LoginRateLimiter rateLimiter;

	public AuthService(AuthenticationManager authenticationManager, LoginRateLimiter rateLimiter) {
		this.authenticationManager = authenticationManager;
		this.rateLimiter = rateLimiter;
	}

	public Authentication authenticate(String username, String password, String clientIp) {
		rateLimiter.checkAllowed(username, clientIp);
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(username, password));
			rateLimiter.reset(username, clientIp);
			return authentication;
		} catch (AuthenticationException e) {
			rateLimiter.recordFailure(username, clientIp);
			throw new BusinessRuleException("INVALID_CREDENTIALS", "Usuario o contraseña incorrectos");
		}
	}
}
