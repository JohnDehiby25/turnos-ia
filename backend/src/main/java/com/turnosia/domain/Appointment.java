package com.turnosia.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointment")
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	@ManyToOne(optional = false)
	@JoinColumn(name = "service_type_id", nullable = false)
	private ServiceType serviceType;

	/** Inicio y fin en UTC (TIMESTAMPTZ); definen la duración real de la cita. */
	@Column(name = "start_at", nullable = false)
	private Instant startAt;

	@Column(name = "end_at", nullable = false)
	private Instant endAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AppointmentStatus status = AppointmentStatus.SCHEDULED;

	/** Precio real de la cita (por caso). Obligatorio. */
	@Column(name = "price_charged", nullable = false, precision = 12, scale = 2)
	private BigDecimal priceCharged;

	/** Propina opcional. */
	@Column(precision = 12, scale = 2)
	private BigDecimal tip;

	private String notes;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Appointment() {
	}

	public Appointment(Client client, ServiceType serviceType, Instant startAt, Instant endAt) {
		this.client = client;
		this.serviceType = serviceType;
		this.startAt = startAt;
		this.endAt = endAt;
		this.createdAt = Instant.now();
		this.updatedAt = this.createdAt;
	}

	public void changeStatus(AppointmentStatus next, Instant now) {
		this.status = next;
		this.updatedAt = now;
	}

	public void chargePrice(BigDecimal price) {
		this.priceCharged = price;
	}

	public void recordTip(BigDecimal amount) {
		this.tip = amount;
	}

	public void reschedule(Instant newStartAt, Instant newEndAt, Instant now) {
		this.startAt = newStartAt;
		this.endAt = newEndAt;
		this.updatedAt = now;
	}

	public Long getId() {
		return id;
	}

	public Client getClient() {
		return client;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public Instant getStartAt() {
		return startAt;
	}

	public Instant getEndAt() {
		return endAt;
	}

	public AppointmentStatus getStatus() {
		return status;
	}

	public BigDecimal getPriceCharged() {
		return priceCharged;
	}

	public BigDecimal getTip() {
		return tip;
	}

	public String getNotes() {
		return notes;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
