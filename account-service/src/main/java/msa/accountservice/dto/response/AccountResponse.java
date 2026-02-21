package msa.accountservice.dto.response;

import msa.accountservice.domain.Account;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AccountResponse {
	private String accountId;
	private UUID ownerUserId;
	private BigDecimal balance;
	private String currency;
	private String status;
	private Long version;
	private OffsetDateTime createdAt;

	public static AccountResponse from(Account a) {
		AccountResponse r = new AccountResponse();
		r.accountId = a.getAccountId();
		r.ownerUserId = a.getOwnerUserId();
		r.balance = a.getBalance();
		r.currency = a.getCurrency();
		r.status = a.getStatus().name();
		r.version = a.getVersion();
		r.createdAt = a.getCreatedAt();
		return r;
	}

	public String getAccountId() {
		return accountId;
	}

	public UUID getOwnerUserId() {
		return ownerUserId;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public String getCurrency() {
		return currency;
	}

	public String getStatus() {
		return status;
	}

	public Long getVersion() {
		return version;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
