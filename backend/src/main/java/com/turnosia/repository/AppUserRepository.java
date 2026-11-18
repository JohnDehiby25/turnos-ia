package com.turnosia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turnosia.domain.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	Optional<AppUser> findByUsername(String username);
}
