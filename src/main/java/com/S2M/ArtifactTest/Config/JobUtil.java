package com.S2M.ArtifactTest.Config;

import com.S2M.ArtifactTest.Config.Listeners.LoggingSkipListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
public  class JobUtil <R, W>{


    private final JobRepository jobRepository;
    private final  PlatformTransactionManager transactionManager;
    private final TaskExecutor configuredTaskExecutor;

    private final BatchProperties batchProperties;



    private final JobExecutionListener jobExecutionListener;
    private final StepExecutionListener stepExecutionListener;
    private final LoggingSkipListener loggingSkipListener; // Your specific SkipListener


    private final RetryPolicy retryPolicy;
    private final SkipPolicy skipPolicy;
    private final BackOffPolicy backOffPolicy;



   public Job newJob(
            final ItemReader<R> reader,
            final ItemProcessor<R, W> processor,
            final ItemWriter<W> writer) {
        return new JobBuilder(batchProperties.getJobName(), this.jobRepository)

                .listener(this.jobExecutionListener)
                .start(createStep(reader,processor,writer))
                .build();
    }

    private Step createStep(ItemReader<R> reader, ItemProcessor<R, W> processor, ItemWriter<W> writer) {
        StepBuilderDsl stepBuilder = StepBuilderDsl.create(
                        batchProperties.getJobName(),
                        jobRepository,
                        transactionManager
                )
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .chunkSize(batchProperties.getChunkSize())
                .retryPolicy(this.retryPolicy)
                .backOffPolicy(this.backOffPolicy)
                .skipPolicy(this.skipPolicy)
                .stepListener(this.stepExecutionListener)
                .skipListener(this.loggingSkipListener)


        return configureStep(stepBuilder);
    }

    private Step configureStep(StepBuilderDsl<R, W> stepBuilder) {
        switch (batchProperties.getJobType()) {
            case MULTI_THREADED_STEP:
                return stepBuilder
                        .taskExecutor(this.configuredTaskExecutor)
                        .build();

            case ASYNC_PROCESSING:
                return stepBuilder
                        .taskExecutor(this.configuredTaskExecutor)
                        .buildAsync();

            case SIMPLE:
            default:
                return stepBuilder.build();
        }
    }

    
}
