package com.S2M.ArtifactTest.Demo.DTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.util.List;

@Repository("transactionDTORepository")
public interface TransactionDTORepository extends JpaRepository<TransactionDTO, String> {

    Page<TransactionDTO> findByAmountGreaterThan(Double amount, Pageable pageable);
}
