package com.S2M.ArtifactTest.Config;

import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
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

public class StepBuilderDsl <R, W> {

    private final String stepName;

    private ItemReader<R> reader;
    private ItemProcessor<R, W> processor;
    private ItemWriter<W> writer;

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;
    private Integer ChunkSize ;

    private RetryPolicy retryPolicy;
    private BackOffPolicy backOffPolicy;
    private SkipPolicy skipPolicy;

    private StepExecutionListener stepExecutionListener;
    private SkipListener<Object,Object> skipListener;



    private StepBuilderDsl(String stepName) {
        this.stepName = stepName;
    }

    public static <R, W> StepBuilderDsl<R, W> create(String stepName) {
        return new StepBuilderDsl<>(stepName);
    }

    public Step build() {

        FaultTolerantStepBuilder<R, W> ft = new StepBuilder(stepName, jobRepository)
                .<R, W>chunk(this.ChunkSize, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant();


        ft.processorNonTransactional() ;

        if (retryPolicy != null) {
            ft = ft.retryPolicy(retryPolicy);
        }
        if (backOffPolicy != null) {
            ft = ft.backOffPolicy(backOffPolicy);
        }
        if(skipPolicy != null) {
            ft = ft.skipPolicy(skipPolicy);
        }

        if (stepExecutionListener != null) {
            ft.listener(stepExecutionListener);
        }
        if (skipListener != null) {
            ft.listener(skipListener);
        }

        return ft.build();
    }



    public StepBuilderDsl<R,W> repository(JobRepository jr) {
        this.jobRepository = jr;
        return this;
    }
    public StepBuilderDsl<R,W>  transactionManager(PlatformTransactionManager tm) {
        this.transactionManager = tm;
        return this;
    }
    public StepBuilderDsl<R,W>  chunkSize(int size) {
        this.ChunkSize = size;
        return this;
    }
    public StepBuilderDsl<R,W> retryPolicy(RetryPolicy rp) {
        this.retryPolicy = rp;
        return this;
    }
    public StepBuilderDsl<R,W> backOffPolicy(BackOffPolicy bp) {
        this.backOffPolicy = bp;
        return this;
    }
    public StepBuilderDsl<R,W>  skipPolicy(SkipPolicy sp) {
        this.skipPolicy = sp;
        return this;
    }

    public StepBuilderDsl<R,W>  stepListener(StepExecutionListener sl) {
        this.stepExecutionListener = sl;
        return this;
    }
    public StepBuilderDsl<R,W>  skipListener(SkipListener<Object,Object> sl) {
        this.skipListener = sl;
        return this;
    }

    public StepBuilderDsl<R,W>  reader(ItemReader<R> r)    { this.reader = r; return this; }
    public StepBuilderDsl<R,W>  processor(ItemProcessor<R,W> p) { this.processor = p; return this; }
    public StepBuilderDsl<R,W> writer(ItemWriter<W> w)    { this.writer = w; return this; }
}
