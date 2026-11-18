package com.turnosia.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DayScheduleRequest(
		@NotNull Boolean open,
		LocalTime openTime,
		LocalTime closeTime,
		LocalTime lunchStart,
		LocalTime lunchEnd,
		@Size(max = 255) String note) {
}
