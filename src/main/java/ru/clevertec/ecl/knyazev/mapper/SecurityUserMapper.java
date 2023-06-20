package ru.clevertec.ecl.knyazev.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import ru.clevertec.ecl.knyazev.dto.UserDTO;

@Mapper(componentModel = "spring")
public interface SecurityUserMapper {
	
	
	default User toSecurityUser(UserDTO userDTO) {
		
		if (userDTO == null) {
			return null;
		}
		
		String name = userDTO.getName();
		String password = userDTO.getPassword();
		Set<GrantedAuthority> grantedAuthority = userDTO.getRolesDTO().stream()
	               .map(role -> new SimpleGrantedAuthority(role.getName()))
	               .collect(Collectors.toSet());
		
		return new User(name, password, grantedAuthority);
	}
	
}
