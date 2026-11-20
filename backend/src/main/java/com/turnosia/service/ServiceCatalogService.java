package com.turnosia.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turnosia.domain.ServiceType;
import com.turnosia.repository.ServiceTypeRepository;

@Service
public class ServiceCatalogService {

	private final ServiceTypeRepository serviceTypeRepository;

	public ServiceCatalogService(ServiceTypeRepository serviceTypeRepository) {
		this.serviceTypeRepository = serviceTypeRepository;
	}

	@Transactional(readOnly = true)
	public List<ServiceType> listActive() {
		return serviceTypeRepository.findAllByActiveTrueOrderByNameAsc();
	}

	/** Cambia el precio vigente del servicio. Las citas ya creadas conservan su precio_charged. */
	@Transactional
	public ServiceType updatePrice(Long serviceTypeId, BigDecimal price) {
		ServiceType service = serviceTypeRepository.findById(serviceTypeId)
				.orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_FOUND", "El servicio no existe"));
		service.changePrice(price);
		return service;
	}
}
