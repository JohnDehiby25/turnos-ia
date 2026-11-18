package com.turnosia.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.turnosia.domain.Appointment;
import com.turnosia.domain.AppointmentStatus;
import com.turnosia.dto.AppointmentResponse;
import com.turnosia.dto.ChangeStatusRequest;
import com.turnosia.dto.CreateAppointmentRequest;
import com.turnosia.dto.RescheduleAppointmentRequest;
import com.turnosia.service.AppointmentService;
import com.turnosia.service.BusinessRuleException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AppointmentResponse create(@Valid @RequestBody CreateAppointmentRequest request) {
		Appointment appointment = appointmentService.create(request.firstName(), request.lastName(),
				request.phone(), request.serviceTypeId(), toInstant(request.start()), toInstant(request.end()));
		return AppointmentResponse.from(appointment);
	}

	@GetMapping
	public List<AppointmentResponse> list(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to,
			@RequestParam(required = false) AppointmentStatus status) {
		List<Appointment> appointments;
		if (date != null) {
			appointments = appointmentService.listBetween(date, date, status);
		} else if (from != null && to != null) {
			appointments = appointmentService.listBetween(from, to, status);
		} else {
			throw new BusinessRuleException("QUERY_RANGE_REQUIRED", "Indica date, o from y to");
		}
		return appointments.stream().map(AppointmentResponse::from).toList();
	}

	@PutMapping("/{id}")
	public AppointmentResponse reschedule(@PathVariable Long id,
			@Valid @RequestBody RescheduleAppointmentRequest request) {
		Appointment appointment = appointmentService.reschedule(id, toInstant(request.start()),
				toInstant(request.end()));
		return AppointmentResponse.from(appointment);
	}

	@PatchMapping("/{id}/status")
	public AppointmentResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request) {
		return AppointmentResponse.from(appointmentService.changeStatus(id, request.status()));
	}

	private static Instant toInstant(OffsetDateTime value) {
		return value == null ? null : value.toInstant();
	}
}
