package msa.authservice.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@RestController
public class LoginController {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration-seconds:3600}")
	private long expirationSeconds;

	@PostMapping("/auth/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
		// ✅ 일단은 하드코딩으로 최소 구현 (나중에 DB/Redis/유저테이블로 교체)
		String username = body.getOrDefault("username", "");
		String password = body.getOrDefault("password", "");

		if (!("user".equals(username) && "pass".equals(password))) {
			return ResponseEntity.status(401).body(Map.of("message", "invalid credentials"));
		}

		Instant now = Instant.now();
		byte[] key = secret.getBytes(StandardCharsets.UTF_8);

		String token = Jwts.builder()
			.setSubject(username)
			.setIssuedAt(Date.from(now))
			.setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
			.signWith(Keys.hmacShaKeyFor(key), SignatureAlgorithm.HS256)
			.compact();

		return ResponseEntity.ok(Map.of("accessToken", token));
	}
}