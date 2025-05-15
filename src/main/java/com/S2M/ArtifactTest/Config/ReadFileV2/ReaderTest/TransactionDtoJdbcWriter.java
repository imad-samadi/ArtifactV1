package com.S2M.ArtifactTest.Config.ReadFileV2.ReaderTest;


import com.S2M.ArtifactTest.Config.ReadFileV2.ReaderTest.DTO.TransactionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;

import javax.sql.DataSource;

@RequiredArgsConstructor
public class TransactionDtoJdbcWriter implements ItemWriter<TransactionDTO>  {

    private final DataSource dataSource;

    private static final String INSERT_SQL =
            "INSERT INTO transactions_dto " +
                    "(reference, amount, account_number) " +
                    "VALUES (:reference, :amount, :accountNumber)";

    private JdbcBatchItemWriter<TransactionDTO> delegate;


    private void initDelegate() {
        if (delegate == null) {
            delegate = new JdbcBatchItemWriterBuilder<TransactionDTO>()
                    .dataSource(dataSource)
                    .sql(INSERT_SQL)
                    .itemSqlParameterSourceProvider(
                            new BeanPropertyItemSqlParameterSourceProvider<>()
                    )
                    .build();

            delegate.afterPropertiesSet();
        }
    }

    @Override
    public void write(Chunk<? extends TransactionDTO> chunk) throws Exception {
        initDelegate();
        delegate.write(chunk);
    }
}
