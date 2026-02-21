package msa.transactionservice.dto;

import java.math.BigDecimal;

public class TransferRequestDto {
	public String fromAccountId;
	public String toAccountId;
	public BigDecimal amount;
	public String currency; // KRW
}
