package com.turnosia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.turnosia.domain.Client;
import com.turnosia.repository.ClientRepository;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

	@Mock
	private ClientRepository clientRepository;

	private ClientService clientService;

	@BeforeEach
	void setUp() {
		clientService = new ClientService(clientRepository);
	}

	@Test
	void resolveClient_noMatchesAndBlankPhone_createsClientWithoutPhone() {
		when(clientRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase("Ana", "Gómez")).thenReturn(List.of());
		when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Client client = clientService.resolveClient("Ana", "Gómez", "   ");

		assertThat(client.getFirstName()).isEqualTo("Ana");
		assertThat(client.getLastName()).isEqualTo("Gómez");
		assertThat(client.getPhone()).isNull();
	}

	@Test
	void resolveClient_noMatchesWithPhone_createsClientWithPhone() {
		when(clientRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase("Ana", "Gómez")).thenReturn(List.of());
		when(clientRepository.existsByPhone("3001234567")).thenReturn(false);
		when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Client client = clientService.resolveClient("Ana", "Gómez", "3001234567");

		assertThat(client.getPhone()).isEqualTo("3001234567");
	}

	@Test
	void resolveClient_noMatchesButPhoneBelongsToSomeoneElse_throwsPhoneTaken() {
		when(clientRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase("Ana", "Gómez")).thenReturn(List.of());
		when(clientRepository.existsByPhone("3001234567")).thenReturn(true);

		assertThatThrownBy(() -> clientService.resolveClient("Ana", "Gómez", "3001234567"))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("CLIENT_PHONE_TAKEN"));
		verify(clientRepository, never()).save(any(Client.class));
	}

	@Test
	void resolveClient_singleMatch_returnsExistingClientWithoutSaving() {
		Client existing = new Client("Ana", "Gómez", null);
		when(clientRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase("Ana", "Gómez"))
				.thenReturn(List.of(existing));

		Client client = clientService.resolveClient("Ana", "Gómez", null);

		assertThat(client).isSameAs(existing);
		verify(clientRepository, never()).save(any(Client.class));
	}

	@Test
	void resolveClient_homonymsAndPhoneOfOneOfThem_returnsThatClient() {
		Client first = new Client("Ana", "Gómez", "3001111111");
		Client second = new Client("Ana", "Gómez", "3002222222");
		when(clientRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase("Ana", "Gómez"))
				.thenReturn(List.of(first, second));

		Client client = clientService.resolveClient("Ana", "Gómez", "3002222222");

		assertThat(client).isSameAs(second);
	}

	@Test
	void resolveClient_homonymsWithoutPhone_throwsAmbiguous() {
		when(clientRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase("Ana", "Gómez"))
				.thenReturn(List.of(new Client("Ana", "Gómez", "3001111111"), new Client("Ana", "Gómez", "3002222222")));

		assertThatThrownBy(() -> clientService.resolveClient("Ana", "Gómez", null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("CLIENT_AMBIGUOUS"));
	}

	@Test
	void resolveClient_homonymsWithUnknownPhone_throwsAmbiguous() {
		when(clientRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase("Ana", "Gómez"))
				.thenReturn(List.of(new Client("Ana", "Gómez", "3001111111"), new Client("Ana", "Gómez", "3002222222")));

		assertThatThrownBy(() -> clientService.resolveClient("Ana", "Gómez", "3009999999"))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("CLIENT_AMBIGUOUS"));
	}

	@Test
	void resolveClient_blankFirstName_throwsFirstNameRequired() {
		assertThatThrownBy(() -> clientService.resolveClient("  ", "Gómez", null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("CLIENT_FIRST_NAME_REQUIRED"));
		verify(clientRepository, never()).findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase(any(), any());
	}

	@Test
	void resolveClient_blankLastName_throwsLastNameRequired() {
		assertThatThrownBy(() -> clientService.resolveClient("Ana", null, null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("CLIENT_LAST_NAME_REQUIRED"));
	}

	@Test
	void resolveClient_namesWithSpaces_searchesWithTrimmedNames() {
		Client existing = new Client("Ana", "Gómez", null);
		when(clientRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase("Ana", "Gómez"))
				.thenReturn(List.of(existing));

		Client client = clientService.resolveClient("  Ana ", " Gómez ", null);

		assertThat(client).isSameAs(existing);
	}
}
