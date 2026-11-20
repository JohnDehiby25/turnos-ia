package com.turnosia.controller;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turnosia.service.BusinessRuleException;
import com.turnosia.service.StatsPeriod;
import com.turnosia.service.StatsService;
import com.turnosia.service.StatsSummary;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

	private final StatsService statsService;

	public StatsController(StatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping("/summary")
	public StatsSummary summary(@RequestParam(required = false) StatsPeriod period,
			@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to) {
		if (from != null && to != null) {
			return statsService.summary(from, to);
		}
		if (period != null && date != null) {
			return statsService.summaryFor(period, date);
		}
		throw new BusinessRuleException("STATS_RANGE_REQUIRED", "Indica period y date, o from y to");
	}
}
