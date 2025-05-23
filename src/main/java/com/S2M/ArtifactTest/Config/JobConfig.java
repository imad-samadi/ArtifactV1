//package com.S2M.ArtifactTest.Config;
//import com.S2M.ArtifactTest.Config.Listeners.LoggingSkipListener;
//import com.S2M.ArtifactTest.Demo.DTO.Transaction;
//import com.S2M.ArtifactTest.Demo.DTO.TransactionDTO;
//import com.S2M.ArtifactTest.Demo.TransactionDtoJdbcWriter;
//import com.S2M.ArtifactTest.Demo.TransactionToDtoProcessor;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.core.JobExecutionListener;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.StepExecutionListener;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.skip.SkipPolicy;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.SimpleAsyncTaskExecutor;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.retry.RetryPolicy;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//
//@Configuration
//@Slf4j
//@EnableConfigurationProperties({BatchProperties.class})
//@ConditionalOnBean({ItemReader.class})
//@ConditionalOnProperty(prefix = "batch.execution", name = "job-name")
//public class JobConfig {
//
//    private final JobRepository jobRepository;
//    private final PlatformTransactionManager transactionManager;
//    private final JobExecutionListener jobExecutionListener;
//    private final StepExecutionListener stepExecutionListener;
//    private final LoggingSkipListener loggingSkipListener;
//    private final RetryPolicy retryPolicy;
//    private final SkipPolicy skipPolicy;
//    private final BatchProperties batchProperties;
//    private final DataSource dataSource;
//
//
//    private final Class<?> inputClass ;
//    private final ItemReader<?> genericItemReader;
//
//
//    public JobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, JobExecutionListener jobExecutionListener, StepExecutionListener stepExecutionListener, LoggingSkipListener loggingSkipListener, RetryPolicy retryPolicy, SkipPolicy skipPolicy, BatchProperties batchProperties, DataSource dataSource,@Qualifier("targetInputTypeClass") Class<?> inputClass, ItemReader<?> genericItemReader) {
//        this.jobRepository = jobRepository;
//        this.transactionManager = transactionManager;
//        this.jobExecutionListener = jobExecutionListener;
//        this.stepExecutionListener = stepExecutionListener;
//        this.loggingSkipListener = loggingSkipListener;
//        this.retryPolicy = retryPolicy;
//        this.skipPolicy = skipPolicy;
//        this.batchProperties = batchProperties;
//        this.dataSource = dataSource;
//        this.inputClass = inputClass;
//        this.genericItemReader = genericItemReader;
//    }
//
//    public ItemProcessor<Transaction, TransactionDTO> transactionToDtoProcessor() {
//       return new TransactionToDtoProcessor();
//    }
//
//
//
//
//
//
//
//}
