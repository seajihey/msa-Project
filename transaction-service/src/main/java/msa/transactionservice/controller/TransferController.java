package msa.transactionservice.controller;

import msa.transactionservice.dto.TransferRequestDto;
import msa.transactionservice.dto.TransferResponseDto;
import msa.transactionservice.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
public class TransferController {

	private final TransferService transferService;

	public TransferController(TransferService transferService) {
		this.transferService = transferService;
	}

	// 외부: POST /transfers  → 내부: POST /
	@PostMapping("/")
	public ResponseEntity<?> createTransfer(
		@RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
		@RequestBody TransferRequestDto body,
		Authentication authentication
	) {
		UUID userId = UUID.fromString(authentication.getName());

		var result = transferService.createTransfer(
			idemKey,
			userId,
			body.fromAccountId,
			body.toAccountId,
			body.amount,
			body.currency
		);

		if (result.httpStatus() == 200) {
			return ResponseEntity.ok(new TransferResponseDto(result.transferId(), result.transferStatus()));
		}
		if (result.httpStatus() == 202) {
			return ResponseEntity.status(202).body(Map.of("message", result.message()));
		}
		return ResponseEntity.status(result.httpStatus()).body(Map.of("message", result.message()));
	}
}
