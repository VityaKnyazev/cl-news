package ru.clevertec.ecl.knyazev.token;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConfigurationProperties(value = "spring.security.token")
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class JwtUtil {
	private String secretKey;
	
	private Long lifeTime;

	public String generateJWT(Authentication authentication) {
		UserDetails user = (UserDetails) authentication.getPrincipal();

		Map<String, Object> header = new HashMap<>();
		header.put("alg", "HMAC-SHA512");
		header.put("typ", "JWT");

		Map<String, String> payload = new HashMap<>();
		payload.put("name", user.getUsername());
		
		return JWT.create().withHeader(header).withPayload(payload)
				.withExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusSeconds(lifeTime)))
				.sign(Algorithm.HMAC512(secretKey));
	}

	private DecodedJWT verifyJWT(String jwt) {
		try {
			return JWT.require(Algorithm.HMAC512(secretKey)).build().verify(jwt);
		} catch (RuntimeException e) {
			log.error("Unable to verify java web token or algorithm failed: {}", e.getMessage());
			return null;
		}
	}

	public String getUserNameFromJWT(String jwt) {
		DecodedJWT decodedJWT = verifyJWT(jwt);

		if (decodedJWT != null) {
			return decodedJWT.getClaim("name").asString();
		}

		return null;
	}
}
