package com.turnosia.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.turnosia.TestDataFactory;
import com.turnosia.TestcontainersConfiguration;
import com.turnosia.domain.ServiceType;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ServiceTypeRepositoryTest {

	@Autowired
	private ServiceTypeRepository serviceTypeRepository;

	@Test
	void save_serviceWithNullPrice_persistsAndKeepsNullPrice() {
		ServiceType saved = serviceTypeRepository.save(TestDataFactory.serviceType("Mechas", 180));

		Optional<ServiceType> found = serviceTypeRepository.findById(saved.getId());

		assertThat(found).isPresent();
		assertThat(found.get().getName()).isEqualTo("Mechas");
		assertThat(found.get().getDurationMin()).isEqualTo(180);
		assertThat(found.get().getPrice()).isNull();
	}

	@Test
	void save_serviceWithPrice_persistsExactPrice() {
		ServiceType saved = serviceTypeRepository.save(
				new ServiceType("Cepillado", 45, new BigDecimal("35000.00")));

		assertThat(serviceTypeRepository.findById(saved.getId()))
				.get()
				.extracting("price")
				.isEqualTo(new BigDecimal("35000.00"));
	}

	@Test
	void seed_flywayV1_hasThreeInitialServices() {
		assertThat(serviceTypeRepository.count()).isEqualTo(3);
	}
}
