package com.turnosia.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turnosia.domain.AppointmentStatus;
import com.turnosia.repository.AppointmentRepository;
import com.turnosia.repository.ServiceCount;
import com.turnosia.repository.StatusTotal;

@Service
public class StatsService {

	private final AppointmentRepository appointmentRepository;

	public StatsService(AppointmentRepository appointmentRepository) {
		this.appointmentRepository = appointmentRepository;
	}

	/** Día, semana (lunes a domingo) o mes que contiene la fecha dada. */
	@Transactional(readOnly = true)
	public StatsSummary summaryFor(StatsPeriod period, LocalDate date) {
		return switch (period) {
			case DAY -> summary(date, date);
			case WEEK -> {
				LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
				yield summary(monday, monday.plusDays(6));
			}
			case MONTH -> summary(date.withDayOfMonth(1), date.with(TemporalAdjusters.lastDayOfMonth()));
		};
	}

	@Transactional(readOnly = true)
	public StatsSummary summary(LocalDate from, LocalDate to) {
		if (to.isBefore(from)) {
			throw new BusinessRuleException("STATS_INVALID_RANGE", "La fecha final no puede ser anterior a la inicial");
		}
		Instant start = from.atStartOfDay(BusinessTime.ZONE).toInstant();
		Instant end = to.plusDays(1).atStartOfDay(BusinessTime.ZONE).toInstant();

		Map<AppointmentStatus, StatusTotal> totals = new EnumMap<>(AppointmentStatus.class);
		for (StatusTotal row : appointmentRepository.totalsByStatus(start, end)) {
			totals.put(row.status(), row);
		}
		Map<AppointmentStatus, Long> counts = new EnumMap<>(AppointmentStatus.class);
		for (AppointmentStatus status : AppointmentStatus.values()) {
			counts.put(status, totals.containsKey(status) ? totals.get(status).count() : 0L);
		}
		List<ServiceCount> topServices = appointmentRepository.topServices(start, end);

		return new StatsSummary(from, to, counts,
				amountOf(totals, AppointmentStatus.ATTENDED),
				amountOf(totals, AppointmentStatus.SCHEDULED),
				amountOf(totals, AppointmentStatus.CANCELLED).add(amountOf(totals, AppointmentStatus.NO_SHOW)),
				tipsOf(totals, AppointmentStatus.ATTENDED),
				topServices);
	}

	private static BigDecimal amountOf(Map<AppointmentStatus, StatusTotal> totals, AppointmentStatus status) {
		StatusTotal total = totals.get(status);
		return total == null || total.amount() == null ? BigDecimal.ZERO : total.amount();
	}

	private static BigDecimal tipsOf(Map<AppointmentStatus, StatusTotal> totals, AppointmentStatus status) {
		StatusTotal total = totals.get(status);
		return total == null || total.tips() == null ? BigDecimal.ZERO : total.tips();
	}
}
