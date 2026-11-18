package com.turnosia.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turnosia.domain.Client;
import com.turnosia.repository.ClientRepository;

@Service
public class ClientService {

	private final ClientRepository clientRepository;

	public ClientService(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	@Transactional
	public Client resolveClient(String firstName, String lastName, String phone) {
		String first = requiredName(firstName, "CLIENT_FIRST_NAME_REQUIRED", "El nombre es obligatorio");
		String last = requiredName(lastName, "CLIENT_LAST_NAME_REQUIRED", "El apellido es obligatorio");
		String normalizedPhone = blankToNull(phone);

		List<Client> matches = clientRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase(first, last);
		if (matches.isEmpty()) {
			return createClient(first, last, normalizedPhone);
		}
		if (matches.size() == 1) {
			return matches.get(0);
		}
		return matches.stream()
				.filter(client -> normalizedPhone != null && normalizedPhone.equals(client.getPhone()))
				.findFirst()
				.orElseThrow(() -> new BusinessRuleException("CLIENT_AMBIGUOUS",
						"Hay varios clientes con ese nombre; indica el teléfono para distinguirlos"));
	}

	private Client createClient(String firstName, String lastName, String phone) {
		if (phone != null && clientRepository.existsByPhone(phone)) {
			throw new BusinessRuleException("CLIENT_PHONE_TAKEN",
					"Ese teléfono ya está registrado a otro cliente");
		}
		return clientRepository.save(new Client(firstName, lastName, phone));
	}

	private static String requiredName(String value, String code, String message) {
		if (value == null || value.isBlank()) {
			throw new BusinessRuleException(code, message);
		}
		return value.strip();
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}
}
