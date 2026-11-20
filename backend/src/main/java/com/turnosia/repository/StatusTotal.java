package com.turnosia.repository;

import java.math.BigDecimal;

import com.turnosia.domain.AppointmentStatus;

/** Totales de citas de un estado: cantidad, suma de precios y suma de propinas. */
public record StatusTotal(AppointmentStatus status, Long count, BigDecimal amount, BigDecimal tips) {
}
