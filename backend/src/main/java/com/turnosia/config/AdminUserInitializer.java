package com.turnosia.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.turnosia.service.AdminUserService;

/**
 * Crea o actualiza la usuaria administradora al arrancar, a partir de
 * ADMIN_USERNAME y ADMIN_PASSWORD. Si la contraseña no está definida, no hace nada.
 */
@Component
public class AdminUserInitializer implements ApplicationRunner {

	private final AdminUserService adminUserService;

	public AdminUserInitializer(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@Override
	public void run(ApplicationArguments args) {
		adminUserService.syncFromEnvironment();
	}
}
