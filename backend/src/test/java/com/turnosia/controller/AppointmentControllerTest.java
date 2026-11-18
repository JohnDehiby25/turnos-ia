package com.turnosia.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.turnosia.domain.Appointment;
import com.turnosia.domain.AppointmentStatus;
import com.turnosia.domain.Client;
import com.turnosia.domain.ServiceType;
import com.turnosia.service.AppointmentService;
import com.turnosia.service.BusinessRuleException;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

	private static final Instant START = Instant.parse("2026-11-19T14:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AppointmentService appointmentService;

	@Test
	void create_validRequest_returns201WithAppointment() throws Exception {
		when(appointmentService.create(eq("Ana"), eq("Gómez"), eq("3001234567"), eq(1L), eq(START), isNull()))
				.thenReturn(appointment());

		mockMvc.perform(post("/api/appointments").contentType(APPLICATION_JSON).content("""
				{"firstName":"Ana","lastName":"Gómez","phone":"3001234567","serviceTypeId":1,
				 "start":"2026-11-19T09:00:00-05:00"}
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.clientFirstName").value("Ana"))
				.andExpect(jsonPath("$.serviceName").value("Cepillado"))
				.andExpect(jsonPath("$.status").value("SCHEDULED"));
	}

	@Test
	void create_missingLastName_returns400Validation() throws Exception {
		mockMvc.perform(post("/api/appointments").contentType(APPLICATION_JSON).content("""
				{"firstName":"Ana","serviceTypeId":1,"start":"2026-11-19T09:00:00-05:00"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void create_slotNotAvailable_returns409WithCode() throws Exception {
		when(appointmentService.create(any(), any(), any(), any(), any(), any()))
				.thenThrow(new BusinessRuleException("APPOINTMENT_NOT_AVAILABLE", "Ese horario no está disponible"));

		mockMvc.perform(post("/api/appointments").contentType(APPLICATION_JSON).content("""
				{"firstName":"Ana","lastName":"Gómez","serviceTypeId":1,"start":"2026-11-19T09:00:00-05:00"}
				"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("APPOINTMENT_NOT_AVAILABLE"));
	}

	@Test
	void list_byDate_returnsAppointmentsForThatDay() throws Exception {
		when(appointmentService.listBetween(LocalDate.of(2026, 11, 19), LocalDate.of(2026, 11, 19), null))
				.thenReturn(List.of(appointment()));

		mockMvc.perform(get("/api/appointments").param("date", "2026-11-19"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].start").value("2026-11-19T14:00:00Z"));
	}

	@Test
	void list_withoutDateOrRange_returns400() throws Exception {
		mockMvc.perform(get("/api/appointments"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("QUERY_RANGE_REQUIRED"));
	}

	@Test
	void list_invalidStatusParam_returns400Bad() throws Exception {
		mockMvc.perform(get("/api/appointments").param("date", "2026-11-19").param("status", "INVENTADO"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	@Test
	void reschedule_returns200WithNewTimes() throws Exception {
		when(appointmentService.reschedule(eq(5L), eq(START), isNull())).thenReturn(appointment());

		mockMvc.perform(put("/api/appointments/5").contentType(APPLICATION_JSON).content("""
				{"start":"2026-11-19T09:00:00-05:00"}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SCHEDULED"));
	}

	@Test
	void changeStatus_cancel_returns200() throws Exception {
		when(appointmentService.changeStatus(5L, AppointmentStatus.CANCELLED)).thenReturn(appointment());

		mockMvc.perform(patch("/api/appointments/5/status").contentType(APPLICATION_JSON)
				.content("{\"status\":\"CANCELLED\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void changeStatus_unknownAppointment_returns404() throws Exception {
		when(appointmentService.changeStatus(99L, AppointmentStatus.ATTENDED))
				.thenThrow(new BusinessRuleException("APPOINTMENT_NOT_FOUND", "La cita no existe"));

		mockMvc.perform(patch("/api/appointments/99/status").contentType(APPLICATION_JSON)
				.content("{\"status\":\"ATTENDED\"}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("APPOINTMENT_NOT_FOUND"));
	}

	@Test
	void changeStatus_unknownStatusValue_returns400() throws Exception {
		mockMvc.perform(patch("/api/appointments/5/status").contentType(APPLICATION_JSON)
				.content("{\"status\":\"ESTADO_INVENTADO\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	private static Appointment appointment() {
		Client client = new Client("Ana", "Gómez", "3001234567");
		ServiceType service = new ServiceType("Cepillado", 45, null);
		return new Appointment(client, service, START, START.plusSeconds(2700));
	}
}
