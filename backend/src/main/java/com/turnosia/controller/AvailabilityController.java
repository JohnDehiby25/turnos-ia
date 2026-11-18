package com.turnosia.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turnosia.dto.FreeSlotResponse;
import com.turnosia.service.AvailabilityService;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

	private final AvailabilityService availabilityService;

	public AvailabilityController(AvailabilityService availabilityService) {
		this.availabilityService = availabilityService;
	}

	@GetMapping
	public List<FreeSlotResponse> freeSlots(@RequestParam LocalDate from, @RequestParam LocalDate to,
			@RequestParam Long serviceId) {
		return availabilityService.freeSlotsForService(from, to, serviceId).stream()
				.map(FreeSlotResponse::from)
				.toList();
	}
}
