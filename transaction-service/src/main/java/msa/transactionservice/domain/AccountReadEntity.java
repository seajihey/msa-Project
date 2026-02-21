package msa.transactionservice.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity
@Table(name = "accounts")
public class AccountReadEntity {

	@Id
	@Column(name = "account_id", length = 40)
	private String accountId;

	@Column(name = "owner_user_id", nullable = false)
	private UUID ownerUserId;

	@Column(name = "balance", nullable = false, precision = 19, scale = 2)
	private BigDecimal balance;

	@Column(name = "currency", nullable = false, columnDefinition = "char(3)")
	@JdbcTypeCode(SqlTypes.CHAR)
	private String currency;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	public String getAccountId() { return accountId; }
	public UUID getOwnerUserId() { return ownerUserId; }
	public String getCurrency() { return currency; }
	public String getStatus() { return status; }
}
