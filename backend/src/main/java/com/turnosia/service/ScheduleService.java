package com.turnosia.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
