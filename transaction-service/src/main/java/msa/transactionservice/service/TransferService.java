package msa.transactionservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import msa.transactionservice.domain.IdempotencyKeyEntity;
import msa.transactionservice.domain.OutboxEventEntity;
import msa.transactionservice.domain.TransferRequestEntity;
import msa.transactionservice.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TransferService {

	private final IdempotencyKeyRepository idempotencyKeyRepository;
	private final TransferRequestRepository transferRequestRepository;
	private final OutboxEventRepository outboxEventRepository;
	private final AccountReadRepository accountReadRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public TransferService(
		IdempotencyKeyRepository idempotencyKeyRepository,
		TransferRequestRepository transferRequestRepository,
		OutboxEventRepository outboxEventRepository,
		AccountReadRepository accountReadRepository
	) {
		this.idempotencyKeyRepository = idempotencyKeyRepository;
		this.transferRequestRepository = transferRequestRepository;
		this.outboxEventRepository = outboxEventRepository;
		this.accountReadRepository = accountReadRepository;
	}

	@Transactional
	public Result createTransfer(
		String idempotencyKey,
		UUID userId,
		String fromAccountId,
		String toAccountId,
		BigDecimal amount,
		String currency
	) {
		// 0) 기본 검증
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			return Result.badRequest("Idempotency-Key header is required");
		}
		if (fromAccountId == null || fromAccountId.isBlank() || toAccountId == null || toAccountId.isBlank()) {
			return Result.badRequest("fromAccountId/toAccountId is required");
		}
		if (fromAccountId.equals(toAccountId)) {
			return Result.badRequest("fromAccountId and toAccountId must be different");
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return Result.badRequest("amount must be > 0");
		}
		if (currency == null || currency.isBlank()) {
			return Result.badRequest("currency is required");
		}

		// 1) request_hash 생성 (단순/안정)
		String requestHash = sha256(fromAccountId + "|" + toAccountId + "|" + amount.toPlainString() + "|" + currency);

		// 2) idempotency_keys insert 시도
		IdempotencyKeyEntity idem = new IdempotencyKeyEntity();
		idem.setIdempotencyKey(idempotencyKey);
		idem.setRequestHash(requestHash);
		idem.setStatus("IN_PROGRESS");

		try {
			idempotencyKeyRepository.save(idem);
		} catch (DataIntegrityViolationException e) {
			// 이미 존재: 비교/상태 처리
			IdempotencyKeyEntity existing = idempotencyKeyRepository.findById(idempotencyKey).orElse(null);
			if (existing == null) {
				return Result.conflict("idempotency key conflict");
			}
			if (!existing.getRequestHash().equals(requestHash)) {
				return Result.conflict("Idempotency-Key reused with different request body");
			}

			if ("COMPLETED".equals(existing.getStatus())) {
				// 기존 결과 반환(멱등)
				var tr = transferRequestRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
				if (tr == null) return Result.conflict("completed but missing transfer record");
				return Result.ok(tr.getTransferId(), tr.getStatus());
			}

			// IN_PROGRESS면 처리 중으로 간주
			return Result.accepted("IN_PROGRESS");
		}

		// 3) 계좌 검증 (DB read-only)
		var fromAcc = accountReadRepository.findById(fromAccountId).orElse(null);
		if (fromAcc == null) return Result.badRequest("fromAccountId not found");
		if (!fromAcc.getOwnerUserId().equals(userId)) return Result.forbidden("fromAccountId not owned by caller");
		if (!"ACTIVE".equals(fromAcc.getStatus())) return Result.badRequest("from account not ACTIVE");
		if (!fromAcc.getCurrency().equalsIgnoreCase(currency)) return Result.badRequest("currency mismatch (from account)");

		var toAcc = accountReadRepository.findById(toAccountId).orElse(null);
		if (toAcc == null) return Result.badRequest("toAccountId not found");
		if (!"ACTIVE".equals(toAcc.getStatus())) return Result.badRequest("to account not ACTIVE");
		if (!toAcc.getCurrency().equalsIgnoreCase(currency)) return Result.badRequest("currency mismatch (to account)");

		// 4) transfer_requests 생성 + 상태 ACCEPTED
		TransferRequestEntity tr = new TransferRequestEntity();
		tr.setTransferId(UUID.randomUUID());
		tr.setIdempotencyKey(idempotencyKey);
		tr.setFromAccountId(fromAccountId);
		tr.setToAccountId(toAccountId);
		tr.setAmount(amount);
		tr.setStatus("ACCEPTED"); // 요청서 생성과 동시에 처리 시작 상태로

		transferRequestRepository.save(tr);

		// 5) outbox_events 적재 (TransferAccepted)
		OutboxEventEntity ob = new OutboxEventEntity();
		ob.setOutboxId(UUID.randomUUID());
		ob.setAggregateType("TRANSFER");
		ob.setAggregateId(tr.getTransferId());
		ob.setEventType("TransferAccepted");
		ob.setStatus("NEW");

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("transferId", tr.getTransferId().toString());
		payload.put("fromAccountId", fromAccountId);
		payload.put("toAccountId", toAccountId);
		payload.put("amount", amount);
		payload.put("currency", currency);
		payload.put("requestedByUserId", userId.toString());

		try {
			ob.setPayload(objectMapper.valueToTree(payload));
		} catch (Exception e) {
			return Result.conflict("payload json serialization failed");
		}

		outboxEventRepository.save(ob);

		// 6) idem COMPLETED (여기선 “접수+이벤트적재까지 완료”를 완료로 봄)
		IdempotencyKeyEntity savedIdem = idempotencyKeyRepository.findById(idempotencyKey).orElse(null);
		if (savedIdem != null) {
			savedIdem.setStatus("COMPLETED");
			idempotencyKeyRepository.save(savedIdem);
		}

		return Result.ok(tr.getTransferId(), tr.getStatus());
	}

	private static String sha256(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : dig) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public record Result(
		int httpStatus,
		String message,
		UUID transferId,
		String transferStatus
	) {
		static Result ok(UUID id, String status) { return new Result(200, null, id, status); }
		static Result accepted(String msg) { return new Result(202, msg, null, null); }
		static Result badRequest(String msg) { return new Result(400, msg, null, null); }
		static Result forbidden(String msg) { return new Result(403, msg, null, null); }
		static Result conflict(String msg) { return new Result(409, msg, null, null); }
	}
}
