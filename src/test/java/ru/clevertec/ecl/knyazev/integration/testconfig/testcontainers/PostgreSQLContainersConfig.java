package ru.clevertec.ecl.knyazev.integration.testconfig.testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;


public class PostgreSQLContainersConfig {
		
	@Bean
	public TestContainersConfig testContainersConfig() {
		return new TestContainersConfig();
	}
	
	@Bean(initMethod = "start", destroyMethod = "stop")
	public PostgreSQLContainer<?> postgreSQLContainer(TestContainersConfig testContainersConfig) {
		return new PostgresContainer(testContainersConfig.getPostgresqlDockerImage(),
									 testContainersConfig.getJdbcUrlEnvVar(),
									 testContainersConfig.getUsernameEnvVar(),
									 testContainersConfig.getPasswordEnvVar());
	}
	
	@Bean(initMethod = "update", destroyMethod = "close")
	public Liquibase liquibase(PostgresContainer postgresContainer, TestContainersConfig testContainersConfig) throws DatabaseException, SQLException {
		String url = postgresContainer.getJdbcUrl();
		String username = postgresContainer.getUsername();
		String password = postgresContainer.getPassword();	
		
		
		Connection connection = DriverManager.getConnection(url, username, password);
		Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
		Liquibase liquibase = new Liquibase(testContainersConfig.getLiquibaseChangelogFile(), new ClassLoaderResourceAccessor(), database);
		return liquibase;
	}
	
	private class PostgresContainer extends PostgreSQLContainer<PostgresContainer> {
		
		private final String jdbcUrlEnvVar;
		private final String usernameEnvVar;
		private final String passwordEnvVar;
		
		
		private PostgresContainer(String dockerImage, 
				                  String jdbcUrlEnvVar, 
				                  String usernameEnvVar, 
				                  String passwordEnvVar) {
			super(dockerImage);
			
			this.jdbcUrlEnvVar = jdbcUrlEnvVar;
			this.usernameEnvVar = usernameEnvVar;
			this.passwordEnvVar = passwordEnvVar;			
		}		

		@Override
		public void start() {
			super.start();
			System.setProperty(jdbcUrlEnvVar, getJdbcUrl());
			System.setProperty(usernameEnvVar, getUsername());
			System.setProperty(passwordEnvVar, getPassword());		
		}

		@Override
		public void stop() {
			super.stop();
		}

	}
}
