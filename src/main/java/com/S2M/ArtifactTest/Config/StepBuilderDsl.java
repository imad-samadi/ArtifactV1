//package com.S2M.ArtifactTest.Config;
//import org.springframework.batch.core.SkipListener;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.StepExecutionListener;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.core.step.skip.SkipPolicy;
//import org.springframework.batch.integration.async.AsyncItemProcessor;
//import org.springframework.batch.integration.async.AsyncItemWriter;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.retry.RetryPolicy;
//import org.springframework.retry.backoff.BackOffPolicy;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import java.util.concurrent.Future;
//
//public class StepBuilderDsl<R, W> {
//
//    private  String stepName;
//
//    private  final JobRepository jobRepository;
//    private final PlatformTransactionManager transactionManager;
//    private TaskExecutor taskExecutor;
//
//    private ItemReader<R> reader;
//    private ItemProcessor<R, W> processor;
//    private ItemWriter<W> writer;
//    private Integer chunkSize;
//
//    private RetryPolicy retryPolicy;
//    private BackOffPolicy backOffPolicy;
//    private SkipPolicy skipPolicy;
//
//    private StepExecutionListener stepExecutionListener;
//    private SkipListener<? super R, ? super W> skipListener;
//
//    private StepBuilderDsl(String stepName,
//                           JobRepository jobRepository,
//                           PlatformTransactionManager transactionManager) {
//        this.stepName = stepName;
//        this.jobRepository = jobRepository;
//        this.transactionManager = transactionManager;
//
//    }
//
//    public static <R, W> StepBuilderDsl<R, W> create(String stepName,
//                                                     JobRepository jobRepository,
//                                                     PlatformTransactionManager transactionManager) {
//        return new StepBuilderDsl<>(stepName, jobRepository, transactionManager);
//    }
//
//    public StepBuilderDsl<R, W> reader(ItemReader reader) {
//        this.reader = reader;
//        return this;
//    }
//
//    public StepBuilderDsl<R, W> processor(ItemProcessor processor) {
//        this.processor = processor;
//        return this;
//    }
//
//    public StepBuilderDsl<R, W> writer(ItemWriter writer) {
//        this.writer = writer;
//        return this;
//    }
//
//    public StepBuilderDsl<R, W> chunkSize(int chunkSize) {
//        this.chunkSize = chunkSize;
//        return this;
//    }
//
//    public StepBuilderDsl<R, W> retryPolicy(RetryPolicy retryPolicy) {
//        this.retryPolicy = retryPolicy;
//        return this;
//    }
//
//    public StepBuilderDsl<R, W> backOffPolicy(BackOffPolicy backOffPolicy) {
//        this.backOffPolicy = backOffPolicy;
//        return this;
//    }
//
//    public StepBuilderDsl<R, W> skipPolicy(SkipPolicy skipPolicy) {
//        this.skipPolicy = skipPolicy;
//        return this;
//    }
//
//    public StepBuilderDsl<R, W> stepListener(StepExecutionListener listener) {
//        this.stepExecutionListener = listener;
//        return this;
//    }
//
//    public StepBuilderDsl<R, W> skipListener(SkipListener<? super R, ? super W> listener) {
//        this.skipListener = listener;
//        return this;
//    }
//    public StepBuilderDsl<R, W> taskExecutor(TaskExecutor taskExecutor) {
//        this.taskExecutor = taskExecutor;
//        return this;
//    }
//
//    public Step build() {
//     //  validate();
//        FaultTolerantStepBuilder<R, W> builder = new StepBuilder(stepName, jobRepository)
//                .<R, W>chunk(chunkSize, transactionManager)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                .faultTolerant() ;
//
//
//        configureCommonSettings(builder);
//        if (taskExecutor != null) builder.taskExecutor(taskExecutor);
//        return builder.build();
//    }
//
//    private <T,U> void configureCommonSettings(FaultTolerantStepBuilder<T,U> builder) {
//
//        builder.processorNonTransactional();
//
//        if (retryPolicy != null) builder.retryPolicy(retryPolicy);
//        if (backOffPolicy != null) builder.backOffPolicy(backOffPolicy);
//        if (skipPolicy != null) builder.skipPolicy(skipPolicy);
//        if (stepExecutionListener != null) builder.listener(stepExecutionListener);
//        if (skipListener != null) builder.listener(skipListener);
//    }
//
//    public Step buildAsync() {
//       // validate();
//        AsyncItemProcessor<R, W> asyncProcessor = new AsyncItemProcessor<>();
//        asyncProcessor.setDelegate(processor);
//        asyncProcessor.setTaskExecutor(taskExecutor);
//
//        AsyncItemWriter<W> asyncWriter = new AsyncItemWriter<>();
//        asyncWriter.setDelegate(writer);
//
//        FaultTolerantStepBuilder<R, Future<W>> builder = new StepBuilder(stepName, jobRepository)
//                .<R, Future<W>>chunk(chunkSize, transactionManager)
//                .reader(reader)
//                .processor(asyncProcessor)
//                .writer(asyncWriter)
//                .faultTolerant() ;
//
//        configureCommonSettings(builder);
//        return builder.build();
//    }
//
//}
