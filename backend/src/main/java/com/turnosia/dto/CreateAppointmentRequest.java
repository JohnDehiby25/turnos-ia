package com.turnosia.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAppointmentRequest(
		@NotBlank @Size(max = 120) String firstName,
		@NotBlank @Size(max = 120) String lastName,
		@Size(max = 20) String phone,
		@NotNull Long serviceTypeId,
		@NotNull OffsetDateTime start,
		OffsetDateTime end,
		@NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price) {
}
