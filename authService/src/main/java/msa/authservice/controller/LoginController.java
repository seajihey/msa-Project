package msa.authservice.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import msa.authservice.domain.AuthUser;
import msa.authservice.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@RestController
public class LoginController {

	private final AuthUserRepository authUserRepository;
	private final PasswordEncoder passwordEncoder;

	public LoginController(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
		this.authUserRepository = authUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration-seconds:3600}")
	private long expirationSeconds;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> body) {

		String username = body.getOrDefault("username", "");
		String password = body.getOrDefault("password", "");

		AuthUser user = authUserRepository.findByUsername(username).orElse(null);

		if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
			return ResponseEntity.status(401).body(Map.of("message", "invalid credentials"));
		}

		if (!passwordEncoder.matches(password, user.getPasswordHash())) {

			return ResponseEntity.status(401).body(Map.of("message", "invalid credentials"));
		}

		Instant now = Instant.now();
		byte[] key = secret.getBytes(StandardCharsets.UTF_8);

		// ✅ A안: subject = user_id(UUID)
		String token = Jwts.builder()
			.setSubject(user.getUserId().toString())
			.claim("username", user.getUsername())
			.claim("role", user.getRole())
			.setIssuedAt(Date.from(now))
			.setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
			.signWith(Keys.hmacShaKeyFor(key), SignatureAlgorithm.HS256)
			.compact();
		System.out.println("LOGIN REQ username=" + username);
		System.out.println("DB user found? " + (user != null));

		return ResponseEntity.ok(Map.of("accessToken", token));
	}
}
