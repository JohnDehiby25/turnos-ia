package com.turnosia.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.turnosia.config.SecurityConfig;
import com.turnosia.repository.AppUserRepository;
import com.turnosia.service.AuthService;
import com.turnosia.service.BusinessRuleException;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private AppUserRepository appUserRepository;

	@Test
	void login_validCredentials_returnsUserAndRole() throws Exception {
		when(authService.authenticate(any(), any(), any())).thenReturn(adminAuthentication());

		mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"secreta123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	void login_wrongPassword_returns401WithCode() throws Exception {
		when(authService.authenticate(any(), any(), any()))
				.thenThrow(new BusinessRuleException("INVALID_CREDENTIALS", "Usuario o contraseña incorrectos"));

		mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"mala\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void login_tooManyAttempts_returns429WithCode() throws Exception {
		when(authService.authenticate(any(), any(), any()))
				.thenThrow(new BusinessRuleException("TOO_MANY_ATTEMPTS", "Demasiados intentos fallidos"));

		mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"mala\"}"))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.code").value("TOO_MANY_ATTEMPTS"));
	}

	@Test
	void login_missingPassword_returns400Validation() throws Exception {
		mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(APPLICATION_JSON)
				.content("{\"username\":\"admin\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void login_withoutCsrfToken_returns403() throws Exception {
		mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"secreta123\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void me_withoutSession_returns401WithCode() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

	@Test
	void csrf_returnsTokenForTheClient() throws Exception {
		mockMvc.perform(get("/api/auth/csrf"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	private static Authentication adminAuthentication() {
		return new UsernamePasswordAuthenticationToken("admin", null,
				List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
	}
}
