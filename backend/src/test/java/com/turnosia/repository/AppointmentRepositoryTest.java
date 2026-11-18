package com.turnosia.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.turnosia.TestDataFactory;
import com.turnosia.TestcontainersConfiguration;
import com.turnosia.domain.Appointment;
import com.turnosia.domain.Client;
import com.turnosia.domain.ServiceType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AppointmentRepositoryTest {

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private ServiceTypeRepository serviceTypeRepository;

	@Test
	void findAllByStartAtBetween_returnsAppointmentsOrderedByStartAt() {
		Client client = clientRepository.save(TestDataFactory.client("Ana", "3001234567"));
		ServiceType mechas = serviceTypeRepository.save(TestDataFactory.serviceType("Mechas", 180));

		appointmentRepository.save(TestDataFactory.appointment(client, mechas,
				OffsetDateTime.parse("2026-11-18T13:00:00Z"), OffsetDateTime.parse("2026-11-18T16:00:00Z")));
		appointmentRepository.save(TestDataFactory.appointment(client, mechas,
				OffsetDateTime.parse("2026-11-18T09:00:00Z"), OffsetDateTime.parse("2026-11-18T10:00:00Z")));
		appointmentRepository.save(TestDataFactory.appointment(client, mechas,
				OffsetDateTime.parse("2026-11-18T11:00:00Z"), OffsetDateTime.parse("2026-11-18T12:00:00Z")));

		List<Appointment> result = appointmentRepository.findAllByStartAtBetweenOrderByStartAtAsc(
				OffsetDateTime.parse("2026-11-18T08:00:00Z").toInstant(),
				OffsetDateTime.parse("2026-11-18T13:30:00Z").toInstant());

		assertThat(result).extracting(a -> a.getStartAt().toString())
				.containsExactly("2026-11-18T09:00:00Z", "2026-11-18T11:00:00Z", "2026-11-18T13:00:00Z");
	}

	@Test
	void save_overlappingAppointment_throwsDataIntegrityViolation() {
		Client client = clientRepository.save(TestDataFactory.client("Ana", "3001234567"));
		ServiceType mechas = serviceTypeRepository.save(TestDataFactory.serviceType("Mechas", 180));

		appointmentRepository.saveAndFlush(TestDataFactory.appointment(client, mechas,
				OffsetDateTime.parse("2026-11-18T09:00:00Z"), OffsetDateTime.parse("2026-11-18T12:00:00Z")));

		assertThatThrownBy(() -> appointmentRepository.saveAndFlush(
				TestDataFactory.appointment(client, mechas,
						OffsetDateTime.parse("2026-11-18T11:00:00Z"),
						OffsetDateTime.parse("2026-11-18T13:00:00Z"))))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void save_adjacentAppointment_isAllowed() {
		Client client = clientRepository.save(TestDataFactory.client("Ana", "3001234567"));
		ServiceType mechas = serviceTypeRepository.save(TestDataFactory.serviceType("Mechas", 180));

		appointmentRepository.saveAndFlush(TestDataFactory.appointment(client, mechas,
				OffsetDateTime.parse("2026-11-18T09:00:00Z"), OffsetDateTime.parse("2026-11-18T12:00:00Z")));

		appointmentRepository.saveAndFlush(TestDataFactory.appointment(client, mechas,
				OffsetDateTime.parse("2026-11-18T12:00:00Z"), OffsetDateTime.parse("2026-11-18T13:00:00Z")));

		assertThat(appointmentRepository.count()).isEqualTo(2);
	}
}
