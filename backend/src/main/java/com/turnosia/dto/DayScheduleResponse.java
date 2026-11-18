package com.turnosia.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.turnosia.service.DaySchedule;

public record DayScheduleResponse(
		LocalDate date,
		boolean open,
		LocalTime openTime,
		LocalTime closeTime,
		LocalTime lunchStart,
		LocalTime lunchEnd) {

	public static DayScheduleResponse from(DaySchedule schedule) {
		return new DayScheduleResponse(schedule.date(), schedule.open(), schedule.openTime(),
				schedule.closeTime(), schedule.lunchStart(), schedule.lunchEnd());
	}
}
