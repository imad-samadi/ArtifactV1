package com.S2M.ArtifactTest.Config.ReadFileV2.ReaderTest;

import com.S2M.ArtifactTest.Config.ReadFileV2.Delimited.Config.DelimitedFileReaderConfig;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.GenericReaderFactory;
import com.S2M.ArtifactTest.Config.ReadFileV2.ReaderTest.DTO.Transaction;
import com.S2M.ArtifactTest.Config.ReadFileV2.ReaderTest.DTO.TransactionDTO;
import lombok.RequiredArgsConstructor;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
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
           @Qualifier("READER") ItemReader<Transaction> reader,

            ItemProcessor<Transaction, TransactionDTO> transactionProcessor,
           ItemWriter<TransactionDTO> compositeWriter
    ) {
        return new StepBuilder("simpleTransactionStep", jobRepository)
                // chunk-oriented step configuration
                .<Transaction, TransactionDTO>chunk(10, transactionManager)
                .reader(reader)
                .processor(transactionProcessor)
                .writer(compositeWriter)

                .faultTolerant()
                .skip(DuplicateKeyException.class)
                .skipLimit(1)
                .processorNonTransactional()
                .build();
    }
    @Bean("READER")
    public ItemReader<Transaction> reader(GenericReaderFactory genericReaderFactory) {

        DelimitedFileReaderConfig<Transaction> readerConfig = DelimitedFileReaderConfig.<Transaction>builder()
                .resourcePath("classpath:transactions.csv") // Path to your CSV file
                .itemType(Transaction.class)                 // The DTO class for direct mapping from CSV
                .names(new String[]{"reference", "amount", "currency", "accountNumber"}) // CSV column name
                .build();


        /*FixedLengthFileReaderConfig<Transaction> readerConfig = FixedLengthFileReaderConfig.<Transaction>builder()
                .resourcePath("classpath:transactionsFixedLength.txt") // Path to your fixed-length file
                .itemType(Transaction.class)
                .names(new String[]{"reference", "amount", "currency", "accountNumber"})
                .ranges(new String[]{"1-6","7-12","13-15","16-19"}) // Set the ranges for fixed-length
                .linesToSkip(0) // Assuming no header lines in this fixed-length format
                // .customLineMapper(null) // Using default processing
                .build();*/

        return genericReaderFactory.createReader(readerConfig);
    }

    @Bean
    public Job simpleTransactionJob(
            @Qualifier("simpleTransactionStep") Step simpleTransactionStep
    ) {
        return new JobBuilder("zsfddsss", jobRepository)
                .flow(simpleTransactionStep)
                .end()
                .build();
    }
}
