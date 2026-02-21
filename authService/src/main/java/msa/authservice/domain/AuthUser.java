package msa.authservice.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_users")
public class AuthUser {

	@Id
	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "username", nullable = false, unique = true, length = 100)
	private String username;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "role", nullable = false, length = 30)
	private String role;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	public AuthUser() {}

	// ----- getters / setters -----

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean active) {
		isActive = active;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
