package com.turnosia.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.turnosia.domain.ServiceType;
import com.turnosia.service.BusinessRuleException;
import com.turnosia.service.ServiceCatalogService;

@WebMvcTest(ServiceCatalogController.class)
class ServiceCatalogControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ServiceCatalogService serviceCatalogService;

	@Test
	void list_returnsServicesWithPrice() throws Exception {
		when(serviceCatalogService.listActive())
				.thenReturn(List.of(new ServiceType("Cepillado", 45, new BigDecimal("35000.00"))));

		mockMvc.perform(get("/api/services"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Cepillado"))
				.andExpect(jsonPath("$[0].durationMin").value(45))
				.andExpect(jsonPath("$[0].price").value(35000.00));
	}

	@Test
	void updatePrice_validRequest_returns200WithNewPrice() throws Exception {
		when(serviceCatalogService.updatePrice(eq(1L), eq(new BigDecimal("40000.00"))))
				.thenReturn(new ServiceType("Cepillado", 45, new BigDecimal("40000.00")));

		mockMvc.perform(put("/api/services/1/price").contentType(APPLICATION_JSON)
				.content("{\"price\":40000.00}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price").value(40000.00));
	}

	@Test
	void updatePrice_negativePrice_returns400Validation() throws Exception {
		mockMvc.perform(put("/api/services/1/price").contentType(APPLICATION_JSON)
				.content("{\"price\":-1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void updatePrice_unknownService_returns404() throws Exception {
		when(serviceCatalogService.updatePrice(eq(99L), eq(BigDecimal.TEN)))
				.thenThrow(new BusinessRuleException("SERVICE_NOT_FOUND", "El servicio no existe"));

		mockMvc.perform(put("/api/services/99/price").contentType(APPLICATION_JSON)
				.content("{\"price\":10}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("SERVICE_NOT_FOUND"));
	}
}
