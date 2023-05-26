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
@ConfigurationProperties("connection.hibernate")
public class HibernateConfig {
	
	private String schema;
	
	private String sessionContext;
	
	private String dialect;
	
	private boolean showSql;
	
	private int transactionIsolationValue;
	
}
