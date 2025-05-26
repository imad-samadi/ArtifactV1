package art.artifact_batch.Config;

import art.artifact_batch.Config.Listeners.LoggingJobListener;
import art.artifact_batch.Config.Listeners.LoggingSkipListener;
import art.artifact_batch.Config.Listeners.LoggingStepListener;
import art.artifact_batch.Config.Listeners.SimpleChunkListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.skip.SkipException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.BackOffPolicyBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;

import org.springframework.retry.RetryPolicy;

import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;


import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfiguration  {

    private final DataSource dataSource;

    private final BatchProperties batchProperties;




    @Bean
    @ConditionalOnMissingBean
    @Qualifier("multiThreadStepExecutor")
    public TaskExecutor multiThreadStepExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        // Use as many threads as specified (e.g., cores count)
        exec.setCorePoolSize(batchProperties.getCorePoolSize());
        exec.setMaxPoolSize(batchProperties.getMaxPoolSize());
        // Daemon threads ensure the JVM can exit when job completes
        exec.setThreadFactory(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        exec.initialize();
        return exec;
    }




    ///How Long Should We Wait Before Retrying an Exception ?
    @ConditionalOnMissingBean
    @Bean
    BackOffPolicy backOffPolicy() {
        return BackOffPolicyBuilder.newBuilder()
                .delay(batchProperties.getBackoffInitialDelay().toMillis())
                .multiplier(batchProperties.getBackoffMultiplier())
                .build();
    }


    ///Group multiple RetryPolicy instances into one. When an exception is thrown
    @ConditionalOnMissingBean
    @Bean
    public RetryPolicy retryPolicy() {
        CompositeRetryPolicy composite = new CompositeRetryPolicy();
        composite.setPolicies(new RetryPolicy[]{
                noRetryPolicy(),
                daoRetryPolicy(),
        });
        return composite;
    }


    ///For certain exceptions that we want to skip rather than retry always answer “no retry.”
    private RetryPolicy noRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> exceptionClassifiers =
                this.skippedExceptions().stream().collect(Collectors.toMap(ex -> ex, ex -> Boolean.FALSE));
        return new SimpleRetryPolicy(batchProperties.getMaxRetries(), exceptionClassifiers, false);
    }


    ///For typical Spring Data access exceptions, retry only on the “transient” or “recoverable” ones.
    private RetryPolicy daoRetryPolicy() {
        return new SimpleRetryPolicy(
                batchProperties.getMaxRetries(),
                Map.of(
                        TransientDataAccessException.class,
                        true,
                        RecoverableDataAccessException.class,
                        true,
                        NonTransientDataAccessException.class,
                        false,
                        EmptyResultDataAccessException.class,
                        false),
                false);
    }


    /// when an exception is thrown the chunk should skip that item and continue
    @ConditionalOnMissingBean
    @Bean
    SkipPolicy skipPolicy() {
        Map<Class<? extends Throwable>, Boolean> exceptionClassifiers =
                this.skippedExceptions().stream()
                        .collect(Collectors.toMap(ex -> ex, ex -> Boolean.TRUE));

        return new LimitCheckingItemSkipPolicy(
                this.batchProperties.getSkipLimit(),
                exceptionClassifiers
        );
    }

    @Bean
    @ConditionalOnMissingBean
    List<Class<? extends Throwable>> skippedExceptions() {
        return List.of(SkipException.class);
    }


    /// LISTENERS
    @ConditionalOnMissingBean
    @Bean
    JobExecutionListener jobExecutionListener() {
        return new LoggingJobListener();
    }

    @ConditionalOnMissingBean
    @Bean
    StepExecutionListener stepExecutionListener() {
        return new LoggingStepListener();
    }
    @ConditionalOnMissingBean
    @Bean
    LoggingSkipListener LoggingSkipListener() {

        return new LoggingSkipListener();
    }
    @Bean
    ChunkListener getChunkListener() {
        return new SimpleChunkListener() ;
    }

    @Bean
    public <R, W> DefaultJob<R, W> defaultJob(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("multiThreadStepExecutor") TaskExecutor taskExecutor,
            BatchProperties batchProperties,
            JobExecutionListener jobListener,
            StepExecutionListener stepListener,
            LoggingSkipListener skipListener,
            ChunkListener chunkListener,
            ItemWriteListener itemWriteListener,
            RetryPolicy retryPolicy,
            SkipPolicy skipPolicy,
            BackOffPolicy backOffPolicy
            ) {


log.info("Configuring Default JOB ............");
        return new DefaultJob<R, W>(
                jobRepository,
                transactionManager,
                taskExecutor,
                batchProperties,
                jobListener,
                stepListener,
                skipListener,
                chunkListener,
                itemWriteListener,
                retryPolicy,
                skipPolicy,
                backOffPolicy
        ) ;
    }







}
