package com.turnosia.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.turnosia.domain.AppointmentStatus;
import com.turnosia.repository.ServiceCount;
import com.turnosia.service.StatsPeriod;
import com.turnosia.service.StatsService;
import com.turnosia.service.StatsSummary;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private StatsService statsService;

	@Test
	void summary_byWeek_returnsMoneyAndCounts() throws Exception {
		when(statsService.summaryFor(eq(StatsPeriod.WEEK), eq(LocalDate.of(2026, 11, 19))))
				.thenReturn(summary());

		mockMvc.perform(get("/api/stats/summary").param("period", "WEEK").param("date", "2026-11-19"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.earned").value(70000))
				.andExpect(jsonPath("$.countByStatus.ATTENDED").value(2))
				.andExpect(jsonPath("$.topServices[0].serviceName").value("Cepillado"));
	}

	@Test
	void summary_byFreeRange_usesFromAndTo() throws Exception {
		when(statsService.summary(eq(LocalDate.of(2026, 11, 1)), eq(LocalDate.of(2026, 11, 15))))
				.thenReturn(summary());

		mockMvc.perform(get("/api/stats/summary").param("from", "2026-11-01").param("to", "2026-11-15"))
				.andExpect(status().isOk());
	}

	@Test
	void summary_withoutPeriodOrRange_returns400() throws Exception {
		mockMvc.perform(get("/api/stats/summary"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("STATS_RANGE_REQUIRED"));
	}

	private static StatsSummary summary() {
		return new StatsSummary(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22),
				Map.of(AppointmentStatus.ATTENDED, 2L, AppointmentStatus.SCHEDULED, 0L,
						AppointmentStatus.CANCELLED, 0L, AppointmentStatus.NO_SHOW, 0L),
				new BigDecimal("70000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				List.of(new ServiceCount("Cepillado", 2L)));
	}
}
