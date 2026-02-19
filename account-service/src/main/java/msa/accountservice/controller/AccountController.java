package msa.accountservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AccountController {

	@GetMapping("/me")
	public ResponseEntity<?> me(Authentication authentication) {
		return ResponseEntity.ok(Map.of(
			"user", authentication.getName(),
			"status", "OK"
		));
	}
}
