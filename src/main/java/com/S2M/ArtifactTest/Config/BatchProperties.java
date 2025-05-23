package com.S2M.ArtifactTest.Config;

import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Getter
@Setter
@ToString
@Validated
@ConfigurationProperties(prefix = "job.execution")

public class BatchProperties {


    public enum JobType {
        SIMPLE,    // Default: One step, chunk-oriented
        MULTI_THREADED_STEP,  // A single chunk-oriented step executed with multiple threads
        ASYNC_PROCESSING     // Step with AsyncItemProcessor and AsyncItemWriter
        // Future: PARTITIONED_STEP, FLOW_BASED
    }


    @NotNull(message = "Job type (batch.execution.job-type) must be specified.")
    private JobType jobType = JobType.SIMPLE; // Default job type

    @NotBlank(message = "Job name (batch.execution.job-name) must be provided.")
    private String jobName = "defaultGenericJob"; // Default job name

    // --- Step Configuration (for the primary step) ---
    @NotBlank(message = "Primary step name (batch.execution.step-name) must be provided.")
    private String stepName = "defaultGenericStep";


    @Min(1)
    @Max(10_000)
    private int chunkSize =(10); // 5000


    private int corePoolSize = 3 ;
    private int maxPoolSize = 12 ;



    @Min(1)
    private int skipLimit = 1;

    /**
     * Default: 3, Maximum number of retry attempts as configured Retry policy, exceeding which fails
     * the job.
     */
    @Min(1)
    @Max(10)
    private int maxRetries = 3;

    /**
     * Default: 3s, Time duration to wait before the first retry attempt is made after a failure.
     */
    @NotNull
    private Duration backoffInitialDelay = Duration.ofSeconds(3);

    /** Default: 2, Factor by which the delay between consecutive retries is multiplied. */
    @Min(1)
    @Max(5)
    private int backoffMultiplier = 2;













}
