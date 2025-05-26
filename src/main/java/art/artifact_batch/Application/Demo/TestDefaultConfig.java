package art.artifact_batch.Application.Demo;


import art.artifact_batch.Application.Demo.DTO.Transaction;
import art.artifact_batch.Application.Demo.DTO.TransactionDTO;
import art.artifact_batch.Config.DefaultJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

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

  /*  @Bean("TransactionDtoJdbcWriter")// For now we dont have the support of creation the writer
    public ItemWriter<TransactionDTO> transactionDtoJdbcWriter() {
        return new TransactionDtoJdbcWriter(dataSource);
    }*/



    @Bean
    Job testJob(
            ItemReader<TransactionDTO> reader,// Get the Auto Reader
            DefaultJob<TransactionDTO,TransactionDTO> defaultJob,
             ItemWriter<TransactionDTO> writer
            ) {
        log.info("Starting testJob...........................................");
        defaultJob.setReader(reader);
        defaultJob.setWriter(writer);
        defaultJob.setProcessor(transactionProcessor2());


        return defaultJob.newJob();

    }



    //    //DB To file Beans

   /* @Bean("FlatFileItemWriter")
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
    }*/
    @Bean("PRO")
    public ItemProcessor<TransactionDTO, TransactionDTO> transactionProcessor2() {
        return item -> {
            log.info("Processing transaction: {}", item);
            return item;
        };
    }
}
