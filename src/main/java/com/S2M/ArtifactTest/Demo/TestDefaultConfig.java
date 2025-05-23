package com.S2M.ArtifactTest.Demo;

import com.S2M.ArtifactTest.Config.DefaultJob;
import com.S2M.ArtifactTest.Demo.DTO.Transaction;
import com.S2M.ArtifactTest.Demo.DTO.TransactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
@RequiredArgsConstructor
@Configuration
@Slf4j
public class TestDefaultConfig {

    private final DataSource dataSource;

    @Bean("TransactionToDtoProcessor") //The pocessor that the user provide
    public ItemProcessor<Transaction, TransactionDTO> transactionToDtoProcessor() {
        return new TransactionToDtoProcessor();
    }

    @Bean("TransactionDtoJdbcWriter")// For now we dont have the support of creation the writer
    public ItemWriter<TransactionDTO> transactionDtoJdbcWriter() {
        return new TransactionDtoJdbcWriter(dataSource);
    }



    @Bean
    Job testJob(
            @Qualifier("genericItemReader") ItemReader<Transaction> reader,// Get the Auto Reader
            @Qualifier("DefaultJob") DefaultJob<Transaction,TransactionDTO> defaultJob
            ) {
        log.info("Starting testJob...........................................");
        defaultJob.setReader(reader);
        defaultJob.setWriter(transactionDtoJdbcWriter());
        defaultJob.setProcessor(transactionToDtoProcessor());


        return defaultJob.newJob();

    }
}
