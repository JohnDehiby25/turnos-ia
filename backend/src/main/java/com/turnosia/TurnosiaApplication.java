package com.turnosia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.turnosia.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class TurnosiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TurnosiaApplication.class, args);
	}

}
