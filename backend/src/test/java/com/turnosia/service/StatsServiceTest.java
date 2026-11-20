package com.turnosia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.turnosia.domain.AppointmentStatus;
import com.turnosia.repository.AppointmentRepository;
import com.turnosia.repository.ServiceCount;
import com.turnosia.repository.StatusTotal;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

	@Mock
	private AppointmentRepository appointmentRepository;

	@InjectMocks
	private StatsService statsService;

	@Test
	void summaryFor_week_usesMondayToSundayInBogota() {
		when(appointmentRepository.totalsByStatus(any(), any())).thenReturn(List.of());
		when(appointmentRepository.topServices(any(), any())).thenReturn(List.of());

		StatsSummary summary = statsService.summaryFor(StatsPeriod.WEEK, LocalDate.of(2026, 11, 22));

		assertThat(summary.from()).isEqualTo(LocalDate.of(2026, 11, 16));
		assertThat(summary.to()).isEqualTo(LocalDate.of(2026, 11, 22));
	}

	@Test
	void summaryFor_sundayLateNightInBogota_countsInThatWeek() {
		when(appointmentRepository.totalsByStatus(any(), any())).thenReturn(List.of());
		when(appointmentRepository.topServices(any(), any())).thenReturn(List.of());

		statsService.summaryFor(StatsPeriod.WEEK, LocalDate.of(2026, 11, 22));

		// Domingo 11:30 p. m. en Bogotá = lunes 04:30 UTC: el rango se calcula en hora de Bogotá.
		org.mockito.ArgumentCaptor<Instant> from = org.mockito.ArgumentCaptor.forClass(Instant.class);
		org.mockito.ArgumentCaptor<Instant> to = org.mockito.ArgumentCaptor.forClass(Instant.class);
		org.mockito.Mockito.verify(appointmentRepository).totalsByStatus(from.capture(), to.capture());
		assertThat(from.getValue()).isEqualTo(Instant.parse("2026-11-16T05:00:00Z"));
		assertThat(to.getValue()).isEqualTo(Instant.parse("2026-11-23T05:00:00Z"));
	}

	@Test
	void summaryFor_month_coversAllDaysOfFebruaryLeapYear() {
		when(appointmentRepository.totalsByStatus(any(), any())).thenReturn(List.of());
		when(appointmentRepository.topServices(any(), any())).thenReturn(List.of());

		StatsSummary summary = statsService.summaryFor(StatsPeriod.MONTH, LocalDate.of(2028, 2, 10));

		assertThat(summary.from()).isEqualTo(LocalDate.of(2028, 2, 1));
		assertThat(summary.to()).isEqualTo(LocalDate.of(2028, 2, 29));
	}

	@Test
	void summary_sumsMoneyByStatusAndTipsOfAttended() {
		when(appointmentRepository.totalsByStatus(any(), any())).thenReturn(List.of(
				new StatusTotal(AppointmentStatus.ATTENDED, 2L, new BigDecimal("70000.00"), new BigDecimal("5000.00")),
				new StatusTotal(AppointmentStatus.SCHEDULED, 1L, new BigDecimal("35000.00"), null),
				new StatusTotal(AppointmentStatus.CANCELLED, 1L, new BigDecimal("20000.00"), null),
				new StatusTotal(AppointmentStatus.NO_SHOW, 1L, new BigDecimal("15000.00"), null)));
		when(appointmentRepository.topServices(any(), any())).thenReturn(List.of(new ServiceCount("Mechas", 2L)));

		StatsSummary summary = statsService.summary(LocalDate.of(2026, 11, 19), LocalDate.of(2026, 11, 19));

		assertThat(summary.earned()).isEqualByComparingTo("70000.00");
		assertThat(summary.projected()).isEqualByComparingTo("35000.00");
		assertThat(summary.notCollected()).isEqualByComparingTo("35000.00");
		assertThat(summary.tips()).isEqualByComparingTo("5000.00");
		assertThat(summary.countByStatus().get(AppointmentStatus.ATTENDED)).isEqualTo(2L);
		assertThat(summary.topServices()).containsExactly(new ServiceCount("Mechas", 2L));
	}

	@Test
	void summary_emptyRange_returnsZerosNotErrors() {
		when(appointmentRepository.totalsByStatus(any(), any())).thenReturn(List.of());
		when(appointmentRepository.topServices(any(), any())).thenReturn(List.of());

		StatsSummary summary = statsService.summary(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

		assertThat(summary.earned()).isEqualByComparingTo("0");
		assertThat(summary.projected()).isEqualByComparingTo("0");
		assertThat(summary.notCollected()).isEqualByComparingTo("0");
		assertThat(summary.tips()).isEqualByComparingTo("0");
		assertThat(summary.countByStatus().values()).containsOnly(0L);
	}

	@Test
	void summary_toBeforeFrom_throwsInvalidRange() {
		assertThatThrownBy(() -> statsService.summary(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 19)))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("STATS_INVALID_RANGE"));
	}
}
