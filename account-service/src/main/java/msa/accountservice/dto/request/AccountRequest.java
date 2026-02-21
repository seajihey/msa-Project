package msa.accountservice.dto.request;

public class AccountRequest {
	// 계좌 생성 시 사용
	private String accountId;
	private String currency; // KRW

	// 상태 변경 시 사용
	private String status; // ACTIVE/FROZEN/CLOSED/DORMANT

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
