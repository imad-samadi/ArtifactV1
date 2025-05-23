package com.S2M.ArtifactTest.Config.Listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import java.time.Duration;
import java.time.LocalDateTime;
@Slf4j
public class LoggingStepListener implements StepExecutionListener {

    private final ThreadLocal<Long> startMillis = new ThreadLocal<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        LocalDateTime now = LocalDateTime.now();
        startMillis.set(System.currentTimeMillis());
        stepExecution.setStartTime(now);
        log.info("----> Step '{}' starting at {}.", stepExecution.getStepName(), now);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        LocalDateTime end = LocalDateTime.now();
        stepExecution.setEndTime(end);

        long chunkMs = System.currentTimeMillis() - startMillis.get();
        Duration wallClock = Duration.between(stepExecution.getStartTime(), end);

        log.info("<---- Step '{}' finished with status {}.",
                stepExecution.getStepName(), stepExecution.getStatus());
        log.info("Read={}, Write={}, Commits={}, Duration={}ms (chunk) / {} (wall-clock)",
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getCommitCount(),
                chunkMs,
                wallClock);

        startMillis.remove();

        Long skips = stepExecution.getProcessSkipCount();
        if (skips > 0) {
            // addExitDescription returns a new ExitStatus with appended description
            return ExitStatus.COMPLETED.addExitDescription("Skipped " + skips + " items");
        }
        return stepExecution.getExitStatus();
    }
}
