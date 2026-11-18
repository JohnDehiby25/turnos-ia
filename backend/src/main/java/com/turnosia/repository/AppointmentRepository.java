package com.turnosia.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.turnosia.domain.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	/**
	 * Citas que empiezan dentro de [from, to), ordenadas por inicio. Base para
	 * "turnos de un día" (RF1) y disponibilidad (RF2).
	 */
	List<Appointment> findAllByStartAtBetweenOrderByStartAtAsc(Instant from, Instant to);

	/**
	 * Citas no canceladas que se solapan con [from, to). Bloquean la agenda igual
	 * que la restricción appointment_no_overlap.
	 */
	@Query("""
			SELECT a FROM Appointment a
			WHERE a.startAt < :to AND a.endAt > :from
			  AND a.status <> com.turnosia.domain.AppointmentStatus.CANCELLED
			ORDER BY a.startAt
			""")
	List<Appointment> findNotCancelledOverlapping(@Param("from") Instant from, @Param("to") Instant to);
}
