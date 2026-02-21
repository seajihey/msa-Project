package msa.transactionservice.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfer_requests")
public class TransferRequestEntity {

	@Id
	@Column(name = "transfer_id", nullable = false)
	private UUID transferId;

	@Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
	private String idempotencyKey;

	@Column(name = "from_account_id", nullable = false, length = 40)
	private String fromAccountId;

	@Column(name = "to_account_id", nullable = false, length = 40)
	private String toAccountId;

	@Column(name = "amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(name = "status", nullable = false, length = 20)
	private String status; // REQUESTED/ACCEPTED/COMPLETED/FAILED/DUPLICATE

	@Column(name = "requested_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime requestedAt;

	@Column(name = "completed_at")
	private OffsetDateTime completedAt;

	public UUID getTransferId() { return transferId; }
	public void setTransferId(UUID transferId) { this.transferId = transferId; }

	public String getIdempotencyKey() { return idempotencyKey; }
	public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

	public String getFromAccountId() { return fromAccountId; }
	public void setFromAccountId(String fromAccountId) { this.fromAccountId = fromAccountId; }

	public String getToAccountId() { return toAccountId; }
	public void setToAccountId(String toAccountId) { this.toAccountId = toAccountId; }

	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
}
