package msa.transactionservice.domain;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyEntity {

	@Id
	@Column(name = "idempotency_key", length = 120)
	private String idempotencyKey;

	@Column(name = "request_hash", nullable = false, columnDefinition = "char(64)")
	@JdbcTypeCode(SqlTypes.CHAR)
	private String requestHash;

	@Column(name = "status", nullable = false, length = 20)
	private String status; // IN_PROGRESS/COMPLETED/FAILED/EXPIRED

	@Column(name = "first_seen_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime firstSeenAt;

	@Column(name = "last_seen_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime lastSeenAt;

	public String getIdempotencyKey() { return idempotencyKey; }
	public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

	public String getRequestHash() { return requestHash; }
	public void setRequestHash(String requestHash) { this.requestHash = requestHash; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
}
