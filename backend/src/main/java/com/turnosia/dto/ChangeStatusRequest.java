package com.turnosia.dto;

import com.turnosia.domain.AppointmentStatus;

import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(@NotNull AppointmentStatus status) {
}
