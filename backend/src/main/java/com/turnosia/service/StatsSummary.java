package com.turnosia.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.turnosia.domain.AppointmentStatus;
import com.turnosia.repository.ServiceCount;

/**
 * Resumen de un rango de fechas. earned = ATTENDED; projected = SCHEDULED;
 * notCollected = CANCELLED + NO_SHOW (informativo); tips = propinas de ATTENDED.
 */
public record StatsSummary(
		LocalDate from,
		LocalDate to,
		Map<AppointmentStatus, Long> countByStatus,
		BigDecimal earned,
		BigDecimal projected,
		BigDecimal notCollected,
		BigDecimal tips,
		List<ServiceCount> topServices) {
}
