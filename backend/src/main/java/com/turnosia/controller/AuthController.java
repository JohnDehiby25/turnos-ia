package com.turnosia.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.turnosia.dto.LoginRequest;
import com.turnosia.dto.UserResponse;
import com.turnosia.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Autenticación por sesión con cookie. El front guarda la cookie de sesión y
 * envía el token CSRF en la cabecera X-XSRF-TOKEN.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final SecurityContextRepository securityContextRepository;

	public AuthController(AuthService authService, SecurityContextRepository securityContextRepository) {
		this.authService = authService;
		this.securityContextRepository = securityContextRepository;
	}

	@PostMapping("/login")
	public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		Authentication authentication = authService.authenticate(request.username(), request.password(),
				httpRequest.getRemoteAddr());
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		securityContextRepository.saveContext(context, httpRequest, httpResponse);
		return UserResponse.from(authentication);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		new SecurityContextLogoutHandler().logout(request, response, authentication);
	}

	@GetMapping("/me")
	public UserResponse me(Authentication authentication) {
		return UserResponse.from(authentication);
	}

	/** Permite al front obtener el token CSRF antes del primer POST. */
	@GetMapping("/csrf")
	public Map<String, String> csrf(CsrfToken token) {
		return Map.of("token", token.getToken());
	}
}
