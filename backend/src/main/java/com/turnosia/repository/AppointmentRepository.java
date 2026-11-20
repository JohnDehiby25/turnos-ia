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

	/** Totales por estado de las citas que empiezan en [from, to). Base del resumen de dinero (RF13). */
	@Query("""
			SELECT new com.turnosia.repository.StatusTotal(a.status, COUNT(a), SUM(a.priceCharged), SUM(a.tip))
			FROM Appointment a
			WHERE a.startAt >= :from AND a.startAt < :to
			GROUP BY a.status
			""")
	List<StatusTotal> totalsByStatus(@Param("from") Instant from, @Param("to") Instant to);

	/** Servicios más pedidos en [from, to), sin contar citas canceladas. */
	@Query("""
			SELECT new com.turnosia.repository.ServiceCount(a.serviceType.name, COUNT(a))
			FROM Appointment a
			WHERE a.startAt >= :from AND a.startAt < :to
			  AND a.status <> com.turnosia.domain.AppointmentStatus.CANCELLED
			GROUP BY a.serviceType.name
			ORDER BY COUNT(a) DESC, a.serviceType.name
			""")
	List<ServiceCount> topServices(@Param("from") Instant from, @Param("to") Instant to);
}
