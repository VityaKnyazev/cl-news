package ru.clevertec.ecl.knyazev.config.connection;

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
@ConfigurationProperties("connection.db")
public class DatabaseConfig {
	
	private String driverClassName;
	
	private String jdbcUrl;
	
	private String username;
	
	private String password;
	
	private int maxPoolSize;
	
	private long connectionTimeOut;
}
