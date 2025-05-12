package com.S2M.ArtifactTest.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
public abstract class JobConfiguration <R, W>{

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final StepExecutionListener stepExecutionListener;
    private final JobExecutionListener jobExecutionListener;
    private final SkipListener<Object,Object> skipListener;

    private final BatchProperties batchProperties;

    private final SkipPolicy skipPolicy;
    private final RetryPolicy retryPolicy;
    private final BackOffPolicy backOffPolicy;




    protected Job newSimpleJob(
            String jobName,
            ItemReader<R> reader,
            ItemProcessor<R,W> processor,
            ItemWriter<W> writer
    ) {
        Step step = StepBuilderDsl.<R,W>create(jobName + "-step")
                .repository(jobRepository)
                .transactionManager(transactionManager)
                .chunkSize(batchProperties.getChunkSize())
                .retryPolicy(retryPolicy)
                .backOffPolicy(backOffPolicy)
                .skipPolicy(skipPolicy)
                .stepListener(this.stepExecutionListener)
                .skipListener(this.skipListener)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();

        return new JobBuilder(jobName, jobRepository)
                .listener(this.jobExecutionListener)
                .start(step)
                .build();
    }


}
