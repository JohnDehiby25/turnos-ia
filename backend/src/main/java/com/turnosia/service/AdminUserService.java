package com.turnosia.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turnosia.config.AppProperties;
import com.turnosia.domain.AppUser;
import com.turnosia.repository.AppUserRepository;

/**
 * Sincroniza la usuaria administradora con las variables de entorno
 * ADMIN_USERNAME y ADMIN_PASSWORD al arrancar. Si la contraseña no está definida,
 * no se toca nada: la cuenta existente conserva su hash.
 */
@Service
public class AdminUserService {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final String username;
	private final String password;

	public AdminUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder,
			AppProperties properties) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.username = properties.admin().username();
		this.password = properties.admin().password();
	}

	@Transactional
	public void syncFromEnvironment() {
		if (password == null || password.isBlank()) {
			return;
		}
		String hash = passwordEncoder.encode(password);
		appUserRepository.findByUsername(username)
				.ifPresentOrElse(
						existing -> existing.setPasswordHash(hash),
						() -> appUserRepository.save(new AppUser(username, hash, "ADMIN")));
	}
}
