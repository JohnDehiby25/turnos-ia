package com.turnosia.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turnosia.domain.DayOverride;
import com.turnosia.domain.WorkingHours;
import com.turnosia.repository.DayOverrideRepository;
import com.turnosia.repository.WorkingHoursRepository;

@Service
public class ScheduleService {

	private final WorkingHoursRepository workingHoursRepository;
	private final DayOverrideRepository dayOverrideRepository;

	public ScheduleService(WorkingHoursRepository workingHoursRepository,
			DayOverrideRepository dayOverrideRepository) {
		this.workingHoursRepository = workingHoursRepository;
		this.dayOverrideRepository = dayOverrideRepository;
	}

	@Transactional(readOnly = true)
	public DaySchedule scheduleFor(LocalDate date) {
		Optional<DayOverride> override = dayOverrideRepository.findById(date);
		if (override.isPresent()) {
			return fromOverride(date, override.get());
		}
		if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
			return DaySchedule.closed(date);
		}
		return workingHoursRepository.findByWeekday(date.getDayOfWeek())
				.map(workingHours -> fromWorkingHours(date, workingHours))
				.orElseGet(() -> DaySchedule.closed(date));
	}

	@Transactional
	public DaySchedule saveOverride(LocalDate date, boolean open, LocalTime openTime, LocalTime closeTime,
			LocalTime lunchStart, LocalTime lunchEnd, String note) {
		if (!open) {
			dayOverrideRepository.save(new DayOverride(date, false, null, null, null, null, note));
			return DaySchedule.closed(date);
		}
		requireValidWindow(openTime, closeTime);
		requireValidLunch(openTime, closeTime, lunchStart, lunchEnd);
		DayOverride override = new DayOverride(date, true, openTime, closeTime, lunchStart, lunchEnd, note);
		dayOverrideRepository.save(override);
		return fromOverride(date, override);
	}

	private static void requireValidWindow(LocalTime openTime, LocalTime closeTime) {
		if (openTime == null || closeTime == null) {
			throw new BusinessRuleException("SCHEDULE_TIMES_REQUIRED",
					"Un día abierto necesita hora de apertura y de cierre");
		}
		if (!openTime.isBefore(closeTime)) {
			throw new BusinessRuleException("SCHEDULE_INVALID_RANGE", "La apertura debe ser anterior al cierre");
		}
	}

	private static void requireValidLunch(LocalTime openTime, LocalTime closeTime, LocalTime lunchStart,
			LocalTime lunchEnd) {
		if (lunchStart == null && lunchEnd == null) {
			return;
		}
		if (lunchStart == null || lunchEnd == null || !lunchStart.isBefore(lunchEnd)
				|| lunchStart.isBefore(openTime) || lunchEnd.isAfter(closeTime)) {
			throw new BusinessRuleException("SCHEDULE_LUNCH_INVALID",
					"El almuerzo necesita inicio y fin válidos, dentro del horario del día");
		}
	}

	private static DaySchedule fromOverride(LocalDate date, DayOverride override) {
		if (!override.isOpen()) {
			return DaySchedule.closed(date);
		}
		return new DaySchedule(date, true, override.getOpenTime(), override.getCloseTime(),
				override.getLunchStart(), override.getLunchEnd());
	}

	private static DaySchedule fromWorkingHours(LocalDate date, WorkingHours workingHours) {
		if (!workingHours.isOpen()) {
			return DaySchedule.closed(date);
		}
		return new DaySchedule(date, true, workingHours.getOpenTime(), workingHours.getCloseTime(),
				workingHours.getLunchStart(), workingHours.getLunchEnd());
	}
}
