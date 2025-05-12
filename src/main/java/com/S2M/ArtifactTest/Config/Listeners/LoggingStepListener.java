package com.S2M.ArtifactTest.Config.Listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import java.time.Duration;
import java.time.LocalDateTime;
@Slf4j
public class LoggingStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        stepExecution.setStartTime(LocalDateTime.now());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.setEndTime(LocalDateTime.now());

        log.info("{} Step completed in time: {}\nSummary{}", stepExecution.getStepName(), Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime()), stepExecution.getSummary());

        if (stepExecution.getProcessSkipCount() > 0) {
            return StepStatus.COMPLETED_WITH_SKIPS;
        } else {
            return stepExecution.getExitStatus();
        }
    }
}
