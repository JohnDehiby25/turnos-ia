package com.turnosia.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

public record RescheduleAppointmentRequest(@NotNull OffsetDateTime start, OffsetDateTime end) {
}
