package com.S2M.ArtifactTest.Demo;

import com.S2M.ArtifactTest.Demo.DTO.Transaction;
import com.S2M.ArtifactTest.Demo.DTO.TransactionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class BatchConfig2 {

    // DataSource for JDBC-based steps
    private final DataSource dataSource;
    // JobRepository stores job/step metadata
    private final JobRepository jobRepository;
    // Transaction manager for chunk boundaries
    private final PlatformTransactionManager transactionManager;

    @Bean("TransactionDtoJdbcWriter")
    public ItemWriter<TransactionDTO> transactionDtoJdbcWriter() {
        return new TransactionDtoJdbcWriter(dataSource);
    }
    @Bean("TransactionToDtoProcessor")
    public ItemProcessor<Transaction, TransactionDTO> transactionToDtoProcessor() {
        return new TransactionToDtoProcessor();
    }

    @Bean("simpleTransactionStep")
    public Step simpleTransactionStep(
            @Qualifier("genericItemReader") ItemReader<Transaction> reader,

            ItemProcessor<Transaction, TransactionDTO> transactionProcessor,
            ItemWriter<TransactionDTO> compositeWriter
    ) {
        return new StepBuilder("simpleTransactionStep", jobRepository)

                .<Transaction, TransactionDTO>chunk(10, transactionManager)
                .reader(reader)
                .processor(transactionProcessor)
                .writer(compositeWriter)
                .build();
    }

    @Bean
    public Job simpleTransactionJob(
            @Qualifier("simpleTransactionStep") Step simpleTransactionStep
    ) {
        return new JobBuilder("AWR33s", jobRepository)
                .flow(simpleTransactionStep)
                .end()
                .build();
    }
}
