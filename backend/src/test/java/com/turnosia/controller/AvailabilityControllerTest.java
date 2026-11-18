package com.turnosia.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.turnosia.service.AvailabilityService;
import com.turnosia.service.FreeSlot;

@WebMvcTest(AvailabilityController.class)
class AvailabilityControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AvailabilityService availabilityService;

	@Test
	void freeSlots_returnsSlotsInUtc() throws Exception {
		when(availabilityService.freeSlotsForService(LocalDate.of(2026, 11, 19), LocalDate.of(2026, 11, 19), 1L))
				.thenReturn(List.of(new FreeSlot(
						ZonedDateTime.parse("2026-11-19T09:00-05:00[America/Bogota]"),
						ZonedDateTime.parse("2026-11-19T09:45-05:00[America/Bogota]"))));

		mockMvc.perform(get("/api/availability").param("from", "2026-11-19").param("to", "2026-11-19")
				.param("serviceId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].start").value("2026-11-19T14:00:00Z"))
				.andExpect(jsonPath("$[0].end").value("2026-11-19T14:45:00Z"));
	}

	@Test
	void freeSlots_missingServiceId_returns400() throws Exception {
		mockMvc.perform(get("/api/availability").param("from", "2026-11-19").param("to", "2026-11-19"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}
}
