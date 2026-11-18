package com.turnosia.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.turnosia.domain.Appointment;
import com.turnosia.domain.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	/**
	 * Citas que empiezan dentro de [from, to), opcionalmente filtradas por estado (null = todos).
	 * Base para "turnos de un día" (RF1) y listados.
	 */
	@Query("""
			SELECT a FROM Appointment a
			WHERE a.startAt >= :from AND a.startAt < :to
			  AND (:status IS NULL OR a.status = :status)
			ORDER BY a.startAt
			""")
	List<Appointment> findStartingBetween(@Param("from") Instant from, @Param("to") Instant to,
			@Param("status") AppointmentStatus status);

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
