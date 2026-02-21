package msa.transactionservice.repository;

import msa.transactionservice.domain.AccountReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountReadRepository extends JpaRepository<AccountReadEntity, String> {
}
