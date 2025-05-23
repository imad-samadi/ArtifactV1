package com.S2M.ArtifactTest.Config.Listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class LoggingJobListener implements JobExecutionListener {

    private long startTime;

    @Override
    public void beforeJob(JobExecution  jobExecution) {
        startTime = System.currentTimeMillis();
        log.info("{} starting.", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("{} finished with status {}. Total duration: {} ms",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                duration);
    }
}
