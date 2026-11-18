package com.turnosia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turnosia.domain.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

	boolean existsByPhone(String phone);

	Optional<Client> findByPhone(String phone);

	List<Client> findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
}
