package art.artifact_batch.Application.Demo.DTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.jdbc.core.RowMapper;

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
    //@Column(name = "reference_update")
    private String reference;
    private BigDecimal amount;
    @Column(name = "account_number")
    private String accountNumber;

   /* public static final RowMapper<TransactionDTO> ROW_MAPPER = (rs, rowNum) -> new TransactionDTO(
            rs.getString("referencev3"),
            rs.getBigDecimal("amount"),

            rs.getString("accountNumber")
    );*/

 /*   public static final RowMapper<TransactionDTO> ROW_MAPPER = (rs, rowNum) -> TransactionDTO.builder()
           // .reference(  rs.getString("reference_update"))
            .reference(  rs.getString("reference"))
            //.amount(rs.getBigDecimal("amount"))
            //.accountNumber(rs.getString("account_number"))

            .build()
            ;*/
}
