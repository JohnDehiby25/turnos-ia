package com.turnosia;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SchemaMigrationTest {

	private static final String INSERT_APPOINTMENT =
			"INSERT INTO appointment (client_id, service_type_id, start_at, end_at) VALUES (?, ?, ?, ?)";

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void flywayMigration_seedsInitialServices_threeRowsWithNullPrice() {
		List<String> names = jdbc.queryForList(
				"SELECT name FROM service_type ORDER BY id", String.class);
		assertThat(names).containsExactly("Mechas", "Cepillado", "Detalle");

		Integer pricesNull = jdbc.queryForObject(
				"SELECT count(*) FROM service_type WHERE price IS NULL", Integer.class);
		assertThat(pricesNull).isEqualTo(3);
	}

	@Test
	void exclusionConstraint_overlappingAppointment_rejectsInsert() {
		jdbc.update("INSERT INTO client (first_name, last_name, phone) VALUES ('Ana', 'Gómez', '3000000001')");

		jdbc.update(INSERT_APPOINTMENT, 1, 1,
				OffsetDateTime.parse("2026-11-18T15:00:00Z"),
				OffsetDateTime.parse("2026-11-18T16:00:00Z"));

		assertThatThrownBy(() -> jdbc.update(INSERT_APPOINTMENT, 1, 1,
				OffsetDateTime.parse("2026-11-18T15:30:00Z"),
				OffsetDateTime.parse("2026-11-18T16:30:00Z")))
				.hasMessageContaining("appointment_no_overlap");
	}

	@Test
	void exclusionConstraint_adjacentAppointment_isAllowed() {
		jdbc.update("INSERT INTO client (first_name, last_name, phone) VALUES ('Luis', 'Pérez', '3000000002')");

		jdbc.update(INSERT_APPOINTMENT, 1, 1,
				OffsetDateTime.parse("2026-11-18T16:00:00Z"),
				OffsetDateTime.parse("2026-11-18T17:00:00Z"));

		jdbc.update(INSERT_APPOINTMENT, 1, 2,
				OffsetDateTime.parse("2026-11-18T17:00:00Z"),
				OffsetDateTime.parse("2026-11-18T17:45:00Z"));
	}

}
