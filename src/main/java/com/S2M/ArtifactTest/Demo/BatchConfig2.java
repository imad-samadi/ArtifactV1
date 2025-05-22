package com.S2M.ArtifactTest.Demo;

import com.S2M.ArtifactTest.Demo.DTO.Transaction;
import com.S2M.ArtifactTest.Demo.DTO.TransactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
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

import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfig2 {

    // DataSource for JDBC-based steps
    private final DataSource dataSource;
    // JobRepository stores job/step metadata
    private final JobRepository jobRepository;
    // Transaction manager for chunk boundaries
    private final PlatformTransactionManager transactionManager;


    //File to DB beans
    @Bean("TransactionDtoJdbcWriter")
    public ItemWriter<TransactionDTO> transactionDtoJdbcWriter() {
        return new TransactionDtoJdbcWriter(dataSource);
    }
    @Bean("TransactionToDtoProcessor")
    public ItemProcessor<Transaction, TransactionDTO> transactionToDtoProcessor() {
        return new TransactionToDtoProcessor();
    }

    //DB To file Beans

    @Bean("FlatFileItemWriter")
    public FlatFileItemWriter<TransactionDTO> transactionFileWriter() {
        return new FlatFileItemWriterBuilder<TransactionDTO>()
                .name("transactionWriter")
                .resource(new FileSystemResource("output/transactions.csv"))
                .lineAggregator(new DelimitedLineAggregator<TransactionDTO>() {{
                    setDelimiter(",");
                    setFieldExtractor(new BeanWrapperFieldExtractor<TransactionDTO>() {{
                        setNames(new String[]{"reference", "amount", "accountNumber"});
                    }});
                }})
                .headerCallback(writer -> writer.write("reference,amount,accountNumber"))
                .build();
    }
    @Bean("PRO")
    public org.springframework.batch.item.ItemProcessor<TransactionDTO, TransactionDTO> transactionProcessor2() {
        return item -> {
            log.info("Processing transaction: {}", item);
            return item;
        };
    }

   /* @Bean("transactionStep2")
    public Step transactionStep( @Qualifier("genericDatabaseItemReader") ItemReader<TransactionDTO> reader) {

        return new StepBuilder("simpleTransactionStep", jobRepository)

                .<TransactionDTO, TransactionDTO>chunk(10, transactionManager)
                .reader(reader)
                .processor(transactionProcessor2())
                .writer(transactionFileWriter())
                .build();
    }*/


    @Bean("simpleTransactionStep")
    public Step simpleTransactionStep(
            @Qualifier("genericItemReader") ItemReader<Transaction> reader,

            ItemProcessor<Transaction, TransactionDTO> transactionProcessor,
         @Qualifier("TransactionDtoJdbcWriter")   ItemWriter<TransactionDTO> compositeWriter
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
        return new JobBuilder("DQSSCCCD    ", jobRepository)
                .flow(simpleTransactionStep)
                .end()
                .build();
    }

  /*  @Bean
    @Primary
    public RowMapper<TransactionDTO> transactionDtoRowMapper() {
        log.info("TransactionDtoRowMapper............................;;;;");
        return TransactionDTO.ROW_MAPPER;
    }*/


}
