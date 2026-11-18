package com.turnosia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.turnosia.config.AppProperties;
import com.turnosia.domain.AppUser;
import com.turnosia.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

	@Mock
	private AppUserRepository appUserRepository;

	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		passwordEncoder = new BCryptPasswordEncoder();
	}

	private static AppProperties adminProperties(String username, String password) {
		return new AppProperties(
				new AppProperties.Security(new AppProperties.Security.Cors(List.of()),
						new AppProperties.Security.Login(5, 15, 15)),
				new AppProperties.Admin(username, password));
	}

	@Test
	void syncFromEnvironment_blankPassword_doesNotTouchRepository() {
		AdminUserService service = new AdminUserService(appUserRepository, passwordEncoder,
				adminProperties("admin", "  "));

		service.syncFromEnvironment();

		verify(appUserRepository, never()).save(any(AppUser.class));
		verify(appUserRepository, never()).findByUsername(any());
	}

	@Test
	void syncFromEnvironment_userDoesNotExist_createsAdminWithHashedPassword() {
		when(appUserRepository.findByUsername("admin")).thenReturn(Optional.empty());
		AdminUserService service = new AdminUserService(appUserRepository, passwordEncoder,
				adminProperties("admin", "secreta123"));

		service.syncFromEnvironment();

		org.mockito.ArgumentCaptor<AppUser> captor = org.mockito.ArgumentCaptor.forClass(AppUser.class);
		verify(appUserRepository).save(captor.capture());
		AppUser saved = captor.getValue();
		assertThat(saved.getUsername()).isEqualTo("admin");
		assertThat(saved.getRole()).isEqualTo("ADMIN");
		assertThat(saved.getPasswordHash()).isNotEqualTo("secreta123");
		assertThat(passwordEncoder.matches("secreta123", saved.getPasswordHash())).isTrue();
	}

	@Test
	void syncFromEnvironment_userExists_updatesPasswordHash() {
		AppUser existing = new AppUser("admin", "hash-anterior", "ADMIN");
		when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(existing));
		AdminUserService service = new AdminUserService(appUserRepository, passwordEncoder,
				adminProperties("admin", "nueva-clave"));

		service.syncFromEnvironment();

		assertThat(existing.getPasswordHash()).isNotEqualTo("hash-anterior");
		assertThat(passwordEncoder.matches("nueva-clave", existing.getPasswordHash())).isTrue();
		verify(appUserRepository, never()).save(any(AppUser.class));
	}
}
