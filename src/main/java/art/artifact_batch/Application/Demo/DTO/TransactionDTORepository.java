package art.artifact_batch.Application.Demo.DTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("transactionDTORepository")
public interface TransactionDTORepository extends JpaRepository<TransactionDTO, String> {

    Page<TransactionDTO> findByAmountGreaterThan(Double amount, Pageable pageable);
}
