package ru.clevertec.ecl.knyazev.filter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.clevertec.ecl.knyazev.service.SecurityUserService;
import ru.clevertec.ecl.knyazev.token.JwtUtil;


@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
	
	private static final String AUTHORIZATION_ERROR = "Authorization error";
	private static final String AUTHORIZATION_HEADER = "Bearer ";

	private String AUTHENTICATION_URL;
	
	private SecurityUserService securityUsersService;
	
	private JwtUtil jwtUtil;
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		String jwt = parseAuthorizationHeader(authorizationHeader);

		if (jwt == null) {
			filterChain.doFilter(request, response);
			return;
		}

		String userName = jwtUtil.getUserNameFromJWT(jwt);

		if (userName == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			UserDetails userDetails = securityUsersService.loadUserByUsername(userName);
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
					userDetails, null, userDetails.getAuthorities());
			authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		} catch (UsernameNotFoundException e) {
			log.error(AUTHORIZATION_ERROR + ": {}", e.getMessage());
		}
		
		filterChain.doFilter(request, response);
	}

	private final String parseAuthorizationHeader(String authorizationHeader) {
		if (authorizationHeader != null && !authorizationHeader.isEmpty() && !authorizationHeader.isBlank()
				&& authorizationHeader.startsWith(AUTHORIZATION_HEADER)) {
			return authorizationHeader.substring(AUTHORIZATION_HEADER.length() - 1).trim();
		} else {
			return null;
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		
		if ((request.getMethod().equals(HttpMethod.POST.name())) && (request.getServletPath().equals(AUTHENTICATION_URL))) {
			return true;
		}
		
		return false;
	}	
	
}