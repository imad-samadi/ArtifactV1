/*package com.S2M.ArtifactTest.Config;

import com.S2M.ArtifactTest.Config.Listeners.LoggingJobListener;
import com.S2M.ArtifactTest.Config.Listeners.LoggingSkipListener;
import com.S2M.ArtifactTest.Config.Listeners.LoggingStepListener;

import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.step.skip.SkipException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.BackOffPolicyBuilder;
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
public class BatchConfiguration extends DefaultBatchConfiguration {

    private final DataSource dataSource;

    private final BatchProperties batchProperties;

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return new DataSourceTransactionManager(this.dataSource);
    }



    ///How Long Should We Wait Before Retrying ?
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
        return List.of(ConstraintViolationException.class, SkipException.class);
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



}*/
