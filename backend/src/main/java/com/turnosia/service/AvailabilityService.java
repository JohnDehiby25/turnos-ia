package com.turnosia.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turnosia.domain.Appointment;
import com.turnosia.domain.ServiceType;
import com.turnosia.repository.AppointmentRepository;
import com.turnosia.repository.ServiceTypeRepository;

@Service
public class AvailabilityService {

	private static final ZoneId ZONE = BusinessTime.ZONE;
	private static final Duration STEP = Duration.ofMinutes(15);

	private final ScheduleService scheduleService;
	private final AppointmentRepository appointmentRepository;
	private final ServiceTypeRepository serviceTypeRepository;
	private final Clock clock;

	public AvailabilityService(ScheduleService scheduleService, AppointmentRepository appointmentRepository,
			ServiceTypeRepository serviceTypeRepository, Clock clock) {
		this.scheduleService = scheduleService;
		this.appointmentRepository = appointmentRepository;
		this.serviceTypeRepository = serviceTypeRepository;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<FreeSlot> freeSlotsForService(LocalDate from, LocalDate to, Long serviceTypeId) {
		ServiceType service = serviceTypeRepository.findById(serviceTypeId)
				.filter(ServiceType::isActive)
				.orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_FOUND",
						"El servicio no existe o está inactivo"));
		return freeSlots(from, to, Duration.ofMinutes(service.getDurationMin()));
	}

	@Transactional(readOnly = true)
	public List<FreeSlot> freeSlots(LocalDate from, LocalDate to, Duration duration) {
		if (!duration.isPositive()) {
			throw new IllegalArgumentException("La duración debe ser mayor a cero");
		}
		List<FreeSlot> slots = new ArrayList<>();
		for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
			slots.addAll(freeSlotsOn(day, duration));
		}
		return slots;
	}

	/**
	 * Indica si [start, end) cabe dentro del horario del día, fuera del almuerzo y sin
	 * solapar citas no canceladas. {@code ignoredAppointmentId} excluye la propia cita al reprogramarla.
	 */
	@Transactional(readOnly = true)
	public boolean isAvailable(Instant start, Instant end, Long ignoredAppointmentId) {
		LocalDate day = start.atZone(ZONE).toLocalDate();
		DaySchedule schedule = scheduleService.scheduleFor(day);
		if (!schedule.open()) {
			return false;
		}
		Instant open = toInstant(day, schedule.openTime());
		Instant close = toInstant(day, schedule.closeTime());
		if (start.isBefore(open) || end.isAfter(close)) {
			return false;
		}
		return isFree(start, end, busyPeriods(day, schedule, open, close, ignoredAppointmentId));
	}

	private List<FreeSlot> freeSlotsOn(LocalDate day, Duration duration) {
		DaySchedule schedule = scheduleService.scheduleFor(day);
		if (!schedule.open()) {
			return List.of();
		}
		Instant open = toInstant(day, schedule.openTime());
		Instant close = toInstant(day, schedule.closeTime());
		List<BusyPeriod> busy = busyPeriods(day, schedule, open, close, null);
		Instant now = clock.instant();

		List<FreeSlot> slots = new ArrayList<>();
		for (Instant start = open; !start.plus(duration).isAfter(close); start = start.plus(STEP)) {
			Instant end = start.plus(duration);
			if (!start.isBefore(now) && isFree(start, end, busy)) {
				slots.add(new FreeSlot(start.atZone(ZONE), end.atZone(ZONE)));
			}
		}
		return slots;
	}

	private List<BusyPeriod> busyPeriods(LocalDate day, DaySchedule schedule, Instant open, Instant close,
			Long ignoredAppointmentId) {
		List<BusyPeriod> busy = new ArrayList<>();
		if (schedule.lunchStart() != null && schedule.lunchEnd() != null) {
			busy.add(new BusyPeriod(toInstant(day, schedule.lunchStart()), toInstant(day, schedule.lunchEnd())));
		}
		for (Appointment appointment : appointmentRepository.findNotCancelledOverlapping(open, close)) {
			if (ignoredAppointmentId == null || !ignoredAppointmentId.equals(appointment.getId())) {
				busy.add(new BusyPeriod(appointment.getStartAt(), appointment.getEndAt()));
			}
		}
		return busy;
	}

	private static boolean isFree(Instant start, Instant end, List<BusyPeriod> busy) {
		return busy.stream().noneMatch(period -> start.isBefore(period.end()) && period.start().isBefore(end));
	}

	private static Instant toInstant(LocalDate day, LocalTime time) {
		return day.atTime(time).atZone(ZONE).toInstant();
	}

	private record BusyPeriod(Instant start, Instant end) {
	}
}
