package com.S2M.ArtifactTest.Config.ReadFileV2.ReaderTest;


import com.S2M.ArtifactTest.Config.ReadFileV2.ReaderTest.DTO.Transaction;
import com.S2M.ArtifactTest.Config.ReadFileV2.ReaderTest.DTO.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class TransactionToDtoProcessor implements ItemProcessor<Transaction, TransactionDTO> {

    @Override
    public TransactionDTO process(Transaction item) throws Exception {

        log.info("Processing TransactionDTO : {}", item.getReference());
       /* if(item.getReference().equals("REF013")){
            throw new CanNotProcessItemException("INVALID REFERENCE ............");
        }*/

        return TransactionDTO.builder()
                .reference(item.getReference())
                .amount(item.getAmount())
                .accountNumber(item.getAccountNumber())
                .build();

    }
}
