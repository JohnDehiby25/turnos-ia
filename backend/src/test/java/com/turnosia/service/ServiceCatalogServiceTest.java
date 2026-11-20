package com.turnosia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.turnosia.domain.ServiceType;
import com.turnosia.repository.ServiceTypeRepository;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogServiceTest {

	private static final Long SERVICE_ID = 1L;

	@Mock
	private ServiceTypeRepository serviceTypeRepository;

	@InjectMocks
	private ServiceCatalogService serviceCatalogService;

	@Test
	void listActive_returnsActiveServicesFromRepository() {
		ServiceType cepillado = new ServiceType("Cepillado", 45, new BigDecimal("35000.00"));
		when(serviceTypeRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(cepillado));

		assertThat(serviceCatalogService.listActive()).containsExactly(cepillado);
	}

	@Test
	void updatePrice_setsNewPrice() {
		ServiceType cepillado = new ServiceType("Cepillado", 45, null);
		when(serviceTypeRepository.findById(SERVICE_ID)).thenReturn(Optional.of(cepillado));

		ServiceType updated = serviceCatalogService.updatePrice(SERVICE_ID, new BigDecimal("40000.00"));

		assertThat(updated.getPrice()).isEqualByComparingTo("40000.00");
	}

	@Test
	void updatePrice_nullMeansPendingConfirmation() {
		ServiceType cepillado = new ServiceType("Cepillado", 45, new BigDecimal("35000.00"));
		when(serviceTypeRepository.findById(SERVICE_ID)).thenReturn(Optional.of(cepillado));

		ServiceType updated = serviceCatalogService.updatePrice(SERVICE_ID, null);

		assertThat(updated.getPrice()).isNull();
	}

	@Test
	void updatePrice_unknownService_throwsServiceNotFound() {
		when(serviceTypeRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> serviceCatalogService.updatePrice(99L, BigDecimal.TEN))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("SERVICE_NOT_FOUND"));
	}
}
