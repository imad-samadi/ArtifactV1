package com.S2M.ArtifactTest.Config.Listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
@Slf4j
public class LoggingSkipListener implements SkipListener {

    @Override
    public void onSkipInRead(Throwable t) {

        log.error("Item skipped during processing Reason: {}",t.getLocalizedMessage());
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {

        log.error("Item skipped during processing:{}Reason: {}", item.toString(), t.getLocalizedMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {

        log.error("Item skipped during writing:{}Reason: {}", item.toString(), t.getLocalizedMessage());
    }
}
