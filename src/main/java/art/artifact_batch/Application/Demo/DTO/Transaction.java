package art.artifact_batch.Application.Demo.DTO;

import lombok.*;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Transaction {

    
    private String reference;
    private BigDecimal amount;
    private String currency;
    private String accountNumber;
}
