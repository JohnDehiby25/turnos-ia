package com.turnosia.dto;

import java.math.BigDecimal;

import com.turnosia.domain.ServiceType;

public record ServiceResponse(
		Long id,
		String name,
		Integer durationMin,
		BigDecimal price) {

	public static ServiceResponse from(ServiceType service) {
		return new ServiceResponse(service.getId(), service.getName(), service.getDurationMin(), service.getPrice());
	}
}
