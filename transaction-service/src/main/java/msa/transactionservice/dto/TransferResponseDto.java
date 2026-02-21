package msa.transactionservice.dto;

import java.util.UUID;

public class TransferResponseDto {
	public UUID transferId;
	public String status;

	public TransferResponseDto(UUID transferId, String status) {
		this.transferId = transferId;
		this.status = status;
	}
}
