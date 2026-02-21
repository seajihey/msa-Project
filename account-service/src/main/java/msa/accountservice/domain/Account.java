package msa.accountservice.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

	@Id
	@Column(name = "account_id", length = 40)
	private String accountId;

	@Column(name = "owner_user_id", nullable = false)
	private UUID ownerUserId;

	@Column(name = "balance", nullable = false, precision = 19, scale = 2)
	private BigDecimal balance = BigDecimal.ZERO;

	@Column(name = "currency", nullable = false, length = 3)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private AccountStatus status = AccountStatus.ACTIVE;

	// ERD의 version(bigint) → JPA 낙관적 락
	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	// DB default now() 사용하려면 insertable/updatable false가 편함
	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	public Account() {}

	// ---- getters/setters ----
	public String getAccountId() { return accountId; }
	public void setAccountId(String accountId) { this.accountId = accountId; }

	public UUID getOwnerUserId() { return ownerUserId; }
	public void setOwnerUserId(UUID ownerUserId) { this.ownerUserId = ownerUserId; }

	public BigDecimal getBalance() { return balance; }
	public void setBalance(BigDecimal balance) { this.balance = balance; }

	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }

	public AccountStatus getStatus() { return status; }
	public void setStatus(AccountStatus status) { this.status = status; }

	public Long getVersion() { return version; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
}
