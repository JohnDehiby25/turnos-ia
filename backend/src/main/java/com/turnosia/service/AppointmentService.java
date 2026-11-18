package com.turnosia.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turnosia.domain.Appointment;
import com.turnosia.domain.AppointmentStatus;
import com.turnosia.domain.Client;
import com.turnosia.domain.ServiceType;
import com.turnosia.repository.AppointmentRepository;
import com.turnosia.repository.ServiceTypeRepository;

@Service
public class AppointmentService {

	private final ClientService clientService;
	private final ServiceTypeRepository serviceTypeRepository;
	private final AppointmentRepository appointmentRepository;
	private final AvailabilityService availabilityService;
	private final Clock clock;

	public AppointmentService(ClientService clientService, ServiceTypeRepository serviceTypeRepository,
			AppointmentRepository appointmentRepository, AvailabilityService availabilityService, Clock clock) {
		this.clientService = clientService;
		this.serviceTypeRepository = serviceTypeRepository;
		this.appointmentRepository = appointmentRepository;
		this.availabilityService = availabilityService;
		this.clock = clock;
	}

	@Transactional
	public Appointment create(String firstName, String lastName, String phone, Long serviceTypeId,
			Instant start, Instant end) {
		requireFuture(start);
		ServiceType service = activeService(serviceTypeId);
		Instant effectiveEnd = end != null ? end : start.plus(durationOf(service));
		requireValidRange(start, effectiveEnd);
		requireAvailable(start, effectiveEnd, null);
		Client client = clientService.resolveClient(firstName, lastName, phone);
		return saveWithoutOverlap(new Appointment(client, service, start, effectiveEnd));
	}

	@Transactional
	public Appointment reschedule(Long appointmentId, Instant start, Instant end) {
		Appointment appointment = findAppointment(appointmentId);
		if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
			throw new BusinessRuleException("APPOINTMENT_NOT_RESCHEDULABLE",
					"Solo se pueden reprogramar citas por atender");
		}
		requireFuture(start);
		Instant effectiveEnd = end != null ? end : start.plus(durationOf(appointment.getServiceType()));
		requireValidRange(start, effectiveEnd);
		requireAvailable(start, effectiveEnd, appointmentId);
		appointment.reschedule(start, effectiveEnd, clock.instant());
		return saveWithoutOverlap(appointment);
	}

	@Transactional(readOnly = true)
	public List<Appointment> listBetween(LocalDate from, LocalDate to, AppointmentStatus status) {
		if (to.isBefore(from)) {
			throw new BusinessRuleException("APPOINTMENT_INVALID_RANGE", "La fecha final no puede ser anterior a la inicial");
		}
		Instant start = from.atStartOfDay(BusinessTime.ZONE).toInstant();
		Instant end = to.plusDays(1).atStartOfDay(BusinessTime.ZONE).toInstant();
		return appointmentRepository.findStartingBetween(start, end, status);
	}

	@Transactional
	public Appointment changeStatus(Long appointmentId, AppointmentStatus target) {
		return transition(appointmentId, target);
	}

	@Transactional
	public Appointment cancel(Long appointmentId) {
		return transition(appointmentId, AppointmentStatus.CANCELLED);
	}

	@Transactional
	public Appointment markAttended(Long appointmentId) {
		return transition(appointmentId, AppointmentStatus.ATTENDED);
	}

	@Transactional
	public Appointment markNoShow(Long appointmentId) {
		return transition(appointmentId, AppointmentStatus.NO_SHOW);
	}

	private Appointment transition(Long appointmentId, AppointmentStatus target) {
		Appointment appointment = findAppointment(appointmentId);
		if (!appointment.getStatus().canTransitionTo(target)) {
			throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
					"La cita está en estado " + appointment.getStatus() + "; no se puede cambiar a " + target);
		}
		appointment.changeStatus(target, clock.instant());
		return appointment;
	}

	private Appointment findAppointment(Long appointmentId) {
		return appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new BusinessRuleException("APPOINTMENT_NOT_FOUND", "La cita no existe"));
	}

	private ServiceType activeService(Long serviceTypeId) {
		return serviceTypeRepository.findById(serviceTypeId)
				.filter(ServiceType::isActive)
				.orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_FOUND",
						"El servicio no existe o está inactivo"));
	}

	private static Duration durationOf(ServiceType service) {
		return Duration.ofMinutes(service.getDurationMin());
	}

	private void requireFuture(Instant start) {
		if (start == null) {
			throw new BusinessRuleException("APPOINTMENT_START_REQUIRED", "La hora de inicio es obligatoria");
		}
		if (!start.isAfter(clock.instant())) {
			throw new BusinessRuleException("APPOINTMENT_IN_PAST", "No se puede agendar en el pasado");
		}
	}

	private static void requireValidRange(Instant start, Instant end) {
		if (!end.isAfter(start)) {
			throw new BusinessRuleException("APPOINTMENT_INVALID_RANGE",
					"La hora de fin debe ser posterior a la de inicio");
		}
	}

	private void requireAvailable(Instant start, Instant end, Long ignoredAppointmentId) {
		if (!availabilityService.isAvailable(start, end, ignoredAppointmentId)) {
			throw new BusinessRuleException("APPOINTMENT_NOT_AVAILABLE", "Ese horario no está disponible");
		}
	}

	/** Si dos transacciones pasan la validación a la vez, la restricción de BD es la última barrera. */
	private Appointment saveWithoutOverlap(Appointment appointment) {
		try {
			return appointmentRepository.saveAndFlush(appointment);
		} catch (DataIntegrityViolationException e) {
			if (e.getMostSpecificCause().getMessage() != null
					&& e.getMostSpecificCause().getMessage().contains("appointment_no_overlap")) {
				throw new BusinessRuleException("APPOINTMENT_NOT_AVAILABLE", "Ese horario acaba de ocuparse");
			}
			throw e;
		}
	}
}
