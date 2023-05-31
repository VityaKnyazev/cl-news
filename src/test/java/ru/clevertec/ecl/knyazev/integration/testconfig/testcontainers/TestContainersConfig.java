package ru.clevertec.ecl.knyazev.integration.testconfig.testcontainers;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Component
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@ConfigurationProperties("testcontainers")
public class TestContainersConfig {

	private String postgresqlDockerImage;
	
	private String jdbcUrlEnvVar;
	private String usernameEnvVar;
	private String passwordEnvVar;
	
	private String liquibaseChangelogFile;
	
}
