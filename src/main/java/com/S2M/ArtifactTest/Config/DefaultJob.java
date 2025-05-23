package com.S2M.ArtifactTest.Config;

import com.S2M.ArtifactTest.Config.Listeners.LoggingSkipListener;
import com.S2M.ArtifactTest.Config.Listeners.SimpleChunkListener;
import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.Future;

@RequiredArgsConstructor
@Setter
public  class DefaultJob <R, W>{


    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor configuredTaskExecutor;

    private final BatchProperties batchProperties;


    private final JobExecutionListener jobExecutionListener;
    private final StepExecutionListener stepExecutionListener;
    private final LoggingSkipListener loggingSkipListener;// Your specific SkipListener
    private final ChunkListener simpleChunkListener;
    private final ItemWriteListener itemWriteListener;


    private final RetryPolicy retryPolicy;
    private final SkipPolicy skipPolicy;
    private final BackOffPolicy backOffPolicy;

    private ItemReader<R> reader ;
    private ItemProcessor<R, W> processor ;
    private ItemWriter<W> writer ;



   public Job newJob(
            ) {
        return new JobBuilder(batchProperties.getJobName(), this.jobRepository)

                .listener(this.jobExecutionListener)
                .start(getStep())
                .build();
    }



    private Step getStep() {

       if(batchProperties.getJobType().equals(BatchProperties.JobType.ASYNC_PROCESSING)){
           return this.buildAsync() ;
       }
       else {
           return this.build();
       }


    }




    private Step build() {
          validate();
        FaultTolerantStepBuilder<R, W> builder = new StepBuilder(batchProperties.getJobName(), jobRepository)
                .<R, W>chunk(batchProperties.getChunkSize(), transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant() ;


        configureCommonSettings(builder);
        if (batchProperties.getJobType().equals(BatchProperties.JobType.MULTI_THREADED_STEP)) builder.taskExecutor(this.configuredTaskExecutor);
        return builder.build();
    }

    private <T,U> void configureCommonSettings(FaultTolerantStepBuilder<T,U> builder) {

        builder.processorNonTransactional();

        if (retryPolicy != null) builder.retryPolicy(retryPolicy);
        if (backOffPolicy != null) builder.backOffPolicy(backOffPolicy);
        if (skipPolicy != null) builder.skipPolicy(skipPolicy);
        if (stepExecutionListener != null) builder.listener(stepExecutionListener);
        if (this.loggingSkipListener!= null) builder.listener(this.loggingSkipListener);
        if (this.simpleChunkListener!=null) builder.listener(this.simpleChunkListener);
        if(this.itemWriteListener!=null) builder.listener(this.itemWriteListener);


    }

    private Step buildAsync() {
         validate();
        AsyncItemProcessor<R, W> asyncProcessor = new AsyncItemProcessor<>();
        asyncProcessor.setDelegate(processor);
        asyncProcessor.setTaskExecutor(this.configuredTaskExecutor);

        AsyncItemWriter<W> asyncWriter = new AsyncItemWriter<>();
        asyncWriter.setDelegate(writer);

        FaultTolerantStepBuilder<R, Future<W>> builder = new StepBuilder(batchProperties.getStepName(), jobRepository)
                .<R, Future<W>>chunk(batchProperties.getChunkSize(), transactionManager)
                .reader(reader)
                .processor(asyncProcessor)
                .writer(asyncWriter)
                .faultTolerant() ;

        configureCommonSettings(builder);
        return builder.build();
    }
    private void validate() {

        if (reader == null) throw new ConfigurationException("Reader must be configured");
        if (processor == null) throw new ConfigurationException("Processor must be configured");
        if (writer == null) throw new ConfigurationException("Writer must be configured");
    }

    
}
