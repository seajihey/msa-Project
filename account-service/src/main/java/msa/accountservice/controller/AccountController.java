package msa.accountservice.controller;

import msa.accountservice.dto.request.AccountRequest;
import msa.accountservice.dto.response.AccountResponse;
import msa.accountservice.domain.Account;
import msa.accountservice.domain.AccountStatus;
import msa.accountservice.repository.AccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
public class AccountController {

	private final AccountRepository accountRepository;

	public AccountController(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	/**
	 * 외부: POST /accounts
	 * 게이트웨이 StripPrefix=1 → 내부(account-service): POST /
	 */
	@PostMapping("/")
	public ResponseEntity<?> create(@RequestBody AccountRequest req, Authentication authentication) {

		UUID userId = UUID.fromString(authentication.getName());

		String accountId = req.getAccountId();
		String currency = req.getCurrency();

		if (accountId == null || accountId.isBlank()) {
			return ResponseEntity.badRequest().body("accountId is required");
		}
		if (currency == null || currency.isBlank()) {
			return ResponseEntity.badRequest().body("currency is required");
		}
		if (currency.length() != 3) {
			return ResponseEntity.badRequest().body("currency must be 3 letters (e.g., KRW)");
		}

		if (accountRepository.existsById(accountId)) {
			return ResponseEntity.status(409).body("accountId already exists");
		}

		Account a = new Account();
		a.setAccountId(accountId);
		a.setOwnerUserId(userId);
		a.setCurrency(currency);
		a.setBalance(BigDecimal.ZERO);
		a.setStatus(AccountStatus.ACTIVE);

		Account saved = accountRepository.save(a);
		return ResponseEntity.ok(AccountResponse.from(saved));
	}

	/**
	 * 외부: GET /accounts/me
	 * 내부: GET /me
	 */
	@GetMapping("/me")
	public ResponseEntity<?> myAccounts(Authentication authentication) {
		UUID userId = UUID.fromString(authentication.getName());

		List<AccountResponse> list = accountRepository.findAllByOwnerUserId(userId)
			.stream()
			.map(AccountResponse::from)
			.toList();

		return ResponseEntity.ok(list);
	}

	/**
	 * 외부: GET /accounts/{accountId}
	 * 내부: GET /{accountId}
	 */
	@GetMapping("/{accountId}")
	public ResponseEntity<?> getOne(@PathVariable String accountId, Authentication authentication) {

		UUID userId = UUID.fromString(authentication.getName());

		Account a = accountRepository.findById(accountId).orElse(null);
		if (a == null) {
			return ResponseEntity.notFound().build();
		}
		if (!a.getOwnerUserId().equals(userId)) {
			return ResponseEntity.status(403).body("forbidden");
		}

		return ResponseEntity.ok(AccountResponse.from(a));
	}

	/**
	 * 외부: PATCH /accounts/{accountId}/status
	 * 내부: PATCH /{accountId}/status
	 *
	 * Body:
	 * { "status": "FROZEN" }
	 */
	@PatchMapping("/{accountId}/status")
	public ResponseEntity<?> updateStatus(
		@PathVariable String accountId,
		@RequestBody AccountRequest req,
		Authentication authentication
	) {
		UUID userId = UUID.fromString(authentication.getName());

		Account a = accountRepository.findById(accountId).orElse(null);
		if (a == null) {
			return ResponseEntity.notFound().build();
		}
		if (!a.getOwnerUserId().equals(userId)) {
			return ResponseEntity.status(403).body("forbidden");
		}

		String statusStr = req.getStatus();
		if (statusStr == null || statusStr.isBlank()) {
			return ResponseEntity.badRequest().body("status is required");
		}

		AccountStatus newStatus;
		try {
			newStatus = AccountStatus.valueOf(statusStr);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("invalid status (ACTIVE/FROZEN/CLOSED/DORMANT)");
		}

		a.setStatus(newStatus);
		Account saved = accountRepository.save(a);

		return ResponseEntity.ok(AccountResponse.from(saved));
	}
}
