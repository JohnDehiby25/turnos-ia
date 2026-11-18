package com.turnosia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.turnosia.domain.DayOverride;
import com.turnosia.domain.WorkingHours;
import com.turnosia.repository.DayOverrideRepository;
import com.turnosia.repository.WorkingHoursRepository;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

	private static final LocalDate WEDNESDAY = LocalDate.of(2026, 11, 18);
	private static final LocalDate SUNDAY = LocalDate.of(2026, 11, 22);

	@Mock
	private WorkingHoursRepository workingHoursRepository;

	@Mock
	private DayOverrideRepository dayOverrideRepository;

	private ScheduleService scheduleService;

	@BeforeEach
	void setUp() {
		scheduleService = new ScheduleService(workingHoursRepository, dayOverrideRepository);
	}

	@Test
	void scheduleFor_weekdayWithoutOverride_usesWorkingHoursWithLunch() {
		when(dayOverrideRepository.findById(WEDNESDAY)).thenReturn(Optional.empty());
		when(workingHoursRepository.findByWeekday(DayOfWeek.WEDNESDAY))
				.thenReturn(Optional.of(openWorkingHours(DayOfWeek.WEDNESDAY)));

		DaySchedule schedule = scheduleService.scheduleFor(WEDNESDAY);

		assertThat(schedule.open()).isTrue();
		assertThat(schedule.openTime()).isEqualTo(LocalTime.of(8, 0));
		assertThat(schedule.closeTime()).isEqualTo(LocalTime.of(18, 0));
		assertThat(schedule.lunchStart()).isEqualTo(LocalTime.of(12, 0));
		assertThat(schedule.lunchEnd()).isEqualTo(LocalTime.of(13, 0));
	}

	@Test
	void scheduleFor_closedWorkingHoursOnWeekday_isClosed() {
		when(dayOverrideRepository.findById(WEDNESDAY)).thenReturn(Optional.empty());
		when(workingHoursRepository.findByWeekday(DayOfWeek.WEDNESDAY))
				.thenReturn(Optional.of(new WorkingHours(DayOfWeek.WEDNESDAY, false, null, null, null, null)));

		DaySchedule schedule = scheduleService.scheduleFor(WEDNESDAY);

		assertThat(schedule.open()).isFalse();
		assertThat(schedule.openTime()).isNull();
	}

	@Test
	void scheduleFor_noWorkingHoursRow_isClosed() {
		when(dayOverrideRepository.findById(WEDNESDAY)).thenReturn(Optional.empty());
		when(workingHoursRepository.findByWeekday(DayOfWeek.WEDNESDAY)).thenReturn(Optional.empty());

		DaySchedule schedule = scheduleService.scheduleFor(WEDNESDAY);

		assertThat(schedule.open()).isFalse();
	}

	@Test
	void scheduleFor_sundayWithoutOverride_isClosedEvenIfWorkingHoursSaysOpen() {
		when(dayOverrideRepository.findById(SUNDAY)).thenReturn(Optional.empty());

		DaySchedule schedule = scheduleService.scheduleFor(SUNDAY);

		assertThat(schedule.open()).isFalse();
	}

	@Test
	void scheduleFor_sundayWithOpenOverride_usesOverrideTimes() {
		when(dayOverrideRepository.findById(SUNDAY)).thenReturn(Optional.of(
				new DayOverride(SUNDAY, true, LocalTime.of(9, 0), LocalTime.of(14, 0), null, null, "Domingo confirmado")));

		DaySchedule schedule = scheduleService.scheduleFor(SUNDAY);

		assertThat(schedule.open()).isTrue();
		assertThat(schedule.openTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(schedule.closeTime()).isEqualTo(LocalTime.of(14, 0));
	}

	@Test
	void scheduleFor_closedOverrideOnOpenWeekday_isClosed() {
		when(dayOverrideRepository.findById(WEDNESDAY)).thenReturn(Optional.of(
				new DayOverride(WEDNESDAY, false, null, null, null, null, "Festivo")));

		DaySchedule schedule = scheduleService.scheduleFor(WEDNESDAY);

		assertThat(schedule.open()).isFalse();
	}

	@Test
	void scheduleFor_overrideWithoutLunch_hasNoLunchInsteadOfBaseLunch() {
		when(dayOverrideRepository.findById(WEDNESDAY)).thenReturn(Optional.of(
				new DayOverride(WEDNESDAY, true, LocalTime.of(8, 0), LocalTime.of(18, 0), null, null, "Temporada alta")));

		DaySchedule schedule = scheduleService.scheduleFor(WEDNESDAY);

		assertThat(schedule.open()).isTrue();
		assertThat(schedule.lunchStart()).isNull();
		assertThat(schedule.lunchEnd()).isNull();
	}

	private static WorkingHours openWorkingHours(DayOfWeek weekday) {
		return new WorkingHours(weekday, true, LocalTime.of(8, 0), LocalTime.of(18, 0),
				LocalTime.of(12, 0), LocalTime.of(13, 0));
	}
}
