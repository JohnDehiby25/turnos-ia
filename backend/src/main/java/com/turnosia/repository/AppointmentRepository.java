package com.turnosia.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turnosia.domain.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	/**
	 * Citas que empiezan dentro de [from, to), ordenadas por inicio. Base para
	 * "turnos de un día" (RF1) y disponibilidad (RF2).
	 */
	List<Appointment> findAllByStartAtBetweenOrderByStartAtAsc(Instant from, Instant to);
}
