package ru.clevertec.ecl.knyazev.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import ru.clevertec.ecl.knyazev.config.UserClientConfig;
import ru.clevertec.ecl.knyazev.dto.UserDTO;

@FeignClient(value = "userClient", configuration = UserClientConfig.class)
public interface UserClient {

	@GetMapping(value = "/users")
	UserDTO getSecurityUser(@RequestParam(name = "user_name") String userName);
	
	@PostMapping(value = "/users")
	UserDTO addUser(@RequestBody UserDTO userDTO);

}
