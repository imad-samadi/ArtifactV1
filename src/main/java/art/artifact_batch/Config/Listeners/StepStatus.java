package art.artifact_batch.Config.Listeners;

import org.springframework.batch.core.ExitStatus;

public class StepStatus {

    public static final ExitStatus COMPLETED_WITH_SKIPS =
            new ExitStatus("COMPLETED_WITH_SKIPS", "Completed with skips");

    public static ExitStatus unknown() {
        return ExitStatus.UNKNOWN;
    }

    public static ExitStatus executing() {
        return ExitStatus.EXECUTING;
    }

    public static ExitStatus completed() {
        return ExitStatus.COMPLETED;
    }

    public static ExitStatus noProcessing() {
        return ExitStatus.NOOP;
    }

    public static ExitStatus failed() {
        return ExitStatus.FAILED;
    }

    public static ExitStatus stopped() {
        return ExitStatus.STOPPED;
    }
}
