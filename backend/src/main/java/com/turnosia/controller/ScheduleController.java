package com.turnosia.controller;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turnosia.dto.DayScheduleRequest;
import com.turnosia.dto.DayScheduleResponse;
import com.turnosia.service.ScheduleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schedule/days")
public class ScheduleController {

	private final ScheduleService scheduleService;

	public ScheduleController(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@GetMapping("/{date}")
	public DayScheduleResponse day(@PathVariable LocalDate date) {
		return DayScheduleResponse.from(scheduleService.scheduleFor(date));
	}

	@PutMapping("/{date}")
	public DayScheduleResponse saveOverride(@PathVariable LocalDate date,
			@Valid @RequestBody DayScheduleRequest request) {
		return DayScheduleResponse.from(scheduleService.saveOverride(date, request.open(), request.openTime(),
				request.closeTime(), request.lunchStart(), request.lunchEnd(), request.note()));
	}
}
