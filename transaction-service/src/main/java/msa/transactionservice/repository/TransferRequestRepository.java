package msa.transactionservice.repository;

import msa.transactionservice.domain.TransferRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransferRequestRepository extends JpaRepository<TransferRequestEntity, UUID> {
	Optional<TransferRequestEntity> findByIdempotencyKey(String idempotencyKey);
}
