package com.turnosia.dto;

import java.time.Instant;

import com.turnosia.domain.Appointment;
import com.turnosia.domain.AppointmentStatus;

public record AppointmentResponse(
		Long id,
		String clientFirstName,
		String clientLastName,
		String clientPhone,
		Long serviceTypeId,
		String serviceName,
		Instant start,
		Instant end,
		AppointmentStatus status,
		Instant createdAt,
		Instant updatedAt) {

	public static AppointmentResponse from(Appointment appointment) {
		return new AppointmentResponse(
				appointment.getId(),
				appointment.getClient().getFirstName(),
				appointment.getClient().getLastName(),
				appointment.getClient().getPhone(),
				appointment.getServiceType().getId(),
				appointment.getServiceType().getName(),
				appointment.getStartAt(),
				appointment.getEndAt(),
				appointment.getStatus(),
				appointment.getCreatedAt(),
				appointment.getUpdatedAt());
	}
}
