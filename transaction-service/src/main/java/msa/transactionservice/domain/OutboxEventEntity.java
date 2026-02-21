package msa.transactionservice.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

	@Id
	@Column(name = "outbox_id", nullable = false)
	private UUID outboxId;

	@Column(name = "aggregate_type", nullable = false, length = 30)
	private String aggregateType; // TRANSFER

	@Column(name = "aggregate_id", nullable = false)
	private UUID aggregateId; // transfer_id

	@Column(name = "event_type", nullable = false, length = 60)
	private String eventType; // TransferAccepted 등

	@Column(name = "payload", nullable = false, columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private JsonNode payload;

	@Column(name = "status", nullable = false, length = 20)
	private String status; // NEW/PUBLISHED/FAILED/RETRYING

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "published_at")
	private OffsetDateTime publishedAt;

	public UUID getOutboxId() { return outboxId; }
	public void setOutboxId(UUID outboxId) { this.outboxId = outboxId; }

	public String getAggregateType() { return aggregateType; }
	public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

	public UUID getAggregateId() { return aggregateId; }
	public void setAggregateId(UUID aggregateId) { this.aggregateId = aggregateId; }

	public String getEventType() { return eventType; }
	public void setEventType(String eventType) { this.eventType = eventType; }

	public JsonNode getPayload() { return payload; }
	public void setPayload(JsonNode payload) { this.payload = payload; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
}
