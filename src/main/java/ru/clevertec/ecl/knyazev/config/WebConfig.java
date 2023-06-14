package ru.clevertec.ecl.knyazev.config;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.TransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.clevertec.ecl.knyazev.config.connection.DatabaseConfig;
import ru.clevertec.ecl.knyazev.config.connection.HibernateConfig;

@Configuration
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = { @Autowired })
public class WebConfig {
	
	private DatabaseConfig databaseConfig;
	
	private HibernateConfig hibernateConfig;
	

	@Bean
	DataSource hikariDataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(databaseConfig.getDriverClassName());
		hikariConfig.setJdbcUrl(databaseConfig.getJdbcUrl());
		hikariConfig.setUsername(databaseConfig.getUsername());
		hikariConfig.setPassword(databaseConfig.getPassword());
		hikariConfig.setMaximumPoolSize(databaseConfig.getMaxPoolSize());
		hikariConfig.setConnectionTimeout(databaseConfig.getConnectionTimeOut());

		HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
		return hikariDataSource;
	}
	
	@Bean
	JpaVendorAdapter hibernateJpaVendorAdapter() {
		JpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
		return hibernateJpaVendorAdapter;
	}

	@Bean(name="entityManagerFactory")
	LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean(DataSource hikariDataSource,
			JpaVendorAdapter hibernateJpaVendorAdapter) {
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		localContainerEntityManagerFactoryBean.setDataSource(hikariDataSource);
		localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
		localContainerEntityManagerFactoryBean.setPackagesToScan("ru.clevertec.ecl.knyazev.entity");

		final HashMap<String, Object> properties = new HashMap<String, Object>();

		properties.put("hibernate.connection.datasource", hikariDataSource);
		properties.put("hibernate.current_session_context_class", hibernateConfig.getSessionContext());
		properties.put("hbm2ddl.auto", hibernateConfig.getSchema());
		properties.put("hibernate.show.sql", hibernateConfig.isShowSql());
		properties.put("hibernate.dialect", hibernateConfig.getDialect());
		properties.put("hibernate.connection.isolation", hibernateConfig.getTransactionIsolationValue());
		localContainerEntityManagerFactoryBean.setJpaPropertyMap(properties);

		return localContainerEntityManagerFactoryBean;
	}
	
	@Bean
	TransactionManager transactionManager(LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean) {
		TransactionManager transactionManager = new JpaTransactionManager(
				localContainerEntityManagerFactoryBean.getObject());

		return transactionManager;
	}
	
	@Bean
    PageableHandlerMethodArgumentResolverCustomizer pageableResolverCustomizer() {
        return pageableResolver -> pageableResolver.setOneIndexedParameters(true);
    }
}
