package com.turnosia.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

import com.turnosia.TestcontainersConfiguration;
import com.turnosia.domain.AppUser;
import com.turnosia.repository.AppUserRepository;
import com.turnosia.repository.ServiceTypeRepository;

/**
 * Flujo de autenticación completo: sesión real, acceso protegido, cierre de sesión
 * y aplicación del CSRF.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SecurityIntegrationTest {

	private static final String PASSWORD = "secreta123";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ServiceTypeRepository serviceTypeRepository;

	@BeforeEach
	void setUp() {
		appUserRepository.findByUsername("admin").ifPresent(appUserRepository::delete);
		appUserRepository.save(new AppUser("admin", passwordEncoder.encode(PASSWORD), "ADMIN"));
	}

	@Test
	void protectedEndpoint_withoutSession_returns401() throws Exception {
		mockMvc.perform(get("/api/appointments").param("date", "2026-11-19"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

	@Test
	void login_wrongPassword_returns401() throws Exception {
		mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void login_correctPassword_returnsSessionThatOpensProtectedEndpoints() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"" + PASSWORD + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ADMIN"))
				.andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

		mockMvc.perform(get("/api/auth/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("admin"));
		mockMvc.perform(get("/api/appointments").param("date", "2026-11-19").session(session))
				.andExpect(status().isOk());
	}

	@Test
	void logout_closesTheSession() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"" + PASSWORD + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

		mockMvc.perform(post("/api/auth/logout").with(csrf()).session(session))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/auth/me").session(session))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void login_withoutCsrfToken_isRejected() throws Exception {
		mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"" + PASSWORD + "\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void unknownRoute_returnsUniformNotFoundError() throws Exception {
		MockHttpSession session = login();

		mockMvc.perform(get("/api/ruta-inexistente").session(session))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}

	@Test
	void unimplementedMethod_returnsUniform405() throws Exception {
		MockHttpSession session = login();

		mockMvc.perform(get("/api/appointments/5").session(session))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
	}

	@Test
	void updateServicePrice_withoutSession_returns401() throws Exception {
		mockMvc.perform(put("/api/services/1/price").with(csrf()).contentType(APPLICATION_JSON)
				.content("{\"price\":40000}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void updateServicePrice_nonAdminRole_returns403() throws Exception {
		mockMvc.perform(put("/api/services/1/price").with(csrf()).with(user("staff").roles("STAFF"))
				.contentType(APPLICATION_JSON).content("{\"price\":40000}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void statsSummary_withoutSession_returns401() throws Exception {
		mockMvc.perform(get("/api/stats/summary").param("period", "DAY").param("date", "2026-11-19"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void statsSummary_nonAdminRole_returns403() throws Exception {
		mockMvc.perform(get("/api/stats/summary").with(user("staff").roles("STAFF"))
				.param("period", "DAY").param("date", "2026-11-19"))
				.andExpect(status().isForbidden());
	}

	@Test
	void statsSummary_admin_returns200() throws Exception {
		MockHttpSession session = login();

		mockMvc.perform(get("/api/stats/summary").session(session).param("period", "DAY").param("date", "2026-11-19"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.earned").exists());
	}

	@Test
	void updateServicePrice_admin_returns200() throws Exception {
		Long serviceId = serviceTypeRepository.findAll().getFirst().getId();
		MockHttpSession session = login();

		mockMvc.perform(put("/api/services/" + serviceId + "/price").with(csrf()).session(session)
				.contentType(APPLICATION_JSON).content("{\"price\":40000}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price").value(40000));
	}

	private MockHttpSession login() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"" + PASSWORD + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}
}
