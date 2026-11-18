package com.turnosia.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "client")
public class Client {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, unique = true, length = 20)
	private String phone;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Client() {
	}

	public Client(String name, String phone) {
		this.name = name;
		this.phone = phone;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
