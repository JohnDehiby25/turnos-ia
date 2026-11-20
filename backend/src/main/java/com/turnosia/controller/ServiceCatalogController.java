package com.turnosia.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turnosia.dto.ServiceResponse;
import com.turnosia.dto.UpdateServicePriceRequest;
import com.turnosia.service.ServiceCatalogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/services")
public class ServiceCatalogController {

	private final ServiceCatalogService serviceCatalogService;

	public ServiceCatalogController(ServiceCatalogService serviceCatalogService) {
		this.serviceCatalogService = serviceCatalogService;
	}

	@GetMapping
	public List<ServiceResponse> list() {
		return serviceCatalogService.listActive().stream().map(ServiceResponse::from).toList();
	}

	@PutMapping("/{id}/price")
	public ServiceResponse updatePrice(@PathVariable Long id, @Valid @RequestBody UpdateServicePriceRequest request) {
		return ServiceResponse.from(serviceCatalogService.updatePrice(id, request.price()));
	}
}
