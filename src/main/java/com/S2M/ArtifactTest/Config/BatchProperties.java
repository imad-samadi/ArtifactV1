package com.S2M.ArtifactTest.Config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Validated
@ConfigurationProperties(prefix = "batch")

public class BatchProperties {


    @Min(1)
    @Max(10_000)
    private int chunkSize =(10); // 5000

    @Min(1)
    @Max(10_000)
    private int pageSize = 10 ; // 5000



    private int corePoolSize = 3 ;

    @Min(1)
    @Max(21)
    private int partitionSize = 3;

    private String InputFile = "C:/Users/msi/Downloads/TransaktionsBatch/TransaktionsBatch/src/main/resources/transactions.csv" ;


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



    @Min(1)
    private int triggerPartitioningThreshold = 20;


    private String taskExecutor;

    private String outputFile = "C:/Users/msi/Downloads/perfermanceTest/perfermanceTest/src/main/resources/transactions.csv" ;


}
