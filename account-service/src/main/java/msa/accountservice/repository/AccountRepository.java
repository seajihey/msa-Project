package msa.accountservice.repository;

import msa.accountservice.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, String> {
	List<Account> findAllByOwnerUserId(UUID ownerUserId);
}
