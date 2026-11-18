package com.turnosia;

import org.springframework.boot.SpringApplication;

public class TestTurnosiaApplication {

	public static void main(String[] args) {
		SpringApplication.from(TurnosiaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
