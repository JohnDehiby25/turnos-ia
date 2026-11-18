package com.turnosia.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.turnosia.service.BusinessRuleException;
import com.turnosia.service.DaySchedule;
import com.turnosia.service.ScheduleService;

@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

	private static final LocalDate SUNDAY = LocalDate.of(2026, 11, 22);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ScheduleService scheduleService;

	@Test
	void day_returnsEffectiveSchedule() throws Exception {
		when(scheduleService.scheduleFor(SUNDAY)).thenReturn(DaySchedule.closed(SUNDAY));

		mockMvc.perform(get("/api/schedule/days/2026-11-22"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.date").value("2026-11-22"))
				.andExpect(jsonPath("$.open").value(false));
	}

	@Test
	void saveOverride_openSunday_returns200() throws Exception {
		when(scheduleService.saveOverride(any(), any(Boolean.class), any(), any(), any(), any(), any()))
				.thenReturn(new DaySchedule(SUNDAY, true, LocalTime.of(9, 0), LocalTime.of(14, 0), null, null));

		mockMvc.perform(put("/api/schedule/days/2026-11-22").contentType(APPLICATION_JSON).content("""
				{"open":true,"openTime":"09:00","closeTime":"14:00","note":"Domingo confirmado"}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.open").value(true))
				.andExpect(jsonPath("$.closeTime").value("14:00:00"));
	}

	@Test
	void saveOverride_openWithoutTimes_returns400FromRule() throws Exception {
		when(scheduleService.saveOverride(any(), any(Boolean.class), any(), any(), any(), any(), any()))
				.thenThrow(new BusinessRuleException("SCHEDULE_TIMES_REQUIRED",
						"Un día abierto necesita hora de apertura y de cierre"));

		mockMvc.perform(put("/api/schedule/days/2026-11-22").contentType(APPLICATION_JSON)
				.content("{\"open\":true}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("SCHEDULE_TIMES_REQUIRED"));
	}

	@Test
	void saveOverride_missingOpenFlag_returns400Validation() throws Exception {
		mockMvc.perform(put("/api/schedule/days/2026-11-22").contentType(APPLICATION_JSON)
				.content("{\"note\":\"sin estado\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}
}
