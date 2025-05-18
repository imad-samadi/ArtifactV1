package com.S2M.ArtifactTest.Demo.DTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "transactions_dto")
public class TransactionDTO {

    @Id
    private String reference;
    private BigDecimal amount;
    @Column(name = "account_number")
    private String accountNumber;
}
