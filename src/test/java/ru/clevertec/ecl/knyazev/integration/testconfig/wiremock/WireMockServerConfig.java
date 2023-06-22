package ru.clevertec.ecl.knyazev.integration.testconfig.wiremock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.github.tomakehurst.wiremock.WireMockServer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@ConfigurationProperties("wiremock.server")
public class WireMockServerConfig {
	
	private int port;
	
	@Bean(initMethod = "start", destroyMethod = "stop")
	WireMockServer wireMockServer() {
		WireMockServer wireMockServer = new WireMockServer(port);
		return wireMockServer;
	}
	
}
