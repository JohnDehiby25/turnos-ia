package com.turnosia.repository;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.turnosia.TestDataFactory;
import com.turnosia.TestcontainersConfiguration;
import com.turnosia.domain.Client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClientRepositoryTest {

	@Autowired
	private ClientRepository clientRepository;

	@Test
	void save_newClient_persistsWithIdAndTimestamp() {
		Client saved = clientRepository.save(TestDataFactory.client("Ana", "Gómez", "3001234567"));

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();
	}

	@Test
	void existsByPhone_existingAndUnknownPhone_returnsTrueAndFalse() {
		clientRepository.save(TestDataFactory.client("Ana", "Gómez", "3001234567"));

		assertThat(clientRepository.existsByPhone("3001234567")).isTrue();
		assertThat(clientRepository.existsByPhone("3110000000")).isFalse();
	}

	@Test
	void findByPhone_unknownPhone_returnsEmpty() {
		clientRepository.save(TestDataFactory.client("Ana", "Gómez", "3001234567"));

		assertThat(clientRepository.findByPhone("3110000000")).isEmpty();
	}

	@Test
	void save_duplicatePhone_throwsDataIntegrityViolation() {
		clientRepository.saveAndFlush(TestDataFactory.client("Ana", "Gómez", "3001234567"));

		assertThatThrownBy(() ->
				clientRepository.saveAndFlush(TestDataFactory.client("Luis", "Pérez", "3001234567")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void save_clientsWithoutPhone_areAllowed() {
		clientRepository.saveAndFlush(TestDataFactory.client("Ana", "Gómez", null));
		clientRepository.saveAndFlush(TestDataFactory.client("Luis", "Pérez", null));

		assertThat(clientRepository.count()).isEqualTo(2);
	}

	@Test
	void findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase_matchesRegardlessOfCase() {
		clientRepository.save(TestDataFactory.client("Ana", "Gomez", "3001234567"));
		clientRepository.save(TestDataFactory.client("Ana", "Perez", null));

		List<Client> result = clientRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase("ANA", "gomez");

		assertThat(result).extracting("phone").containsExactly("3001234567");
	}
}
