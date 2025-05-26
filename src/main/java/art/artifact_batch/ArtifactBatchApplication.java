package art.artifact_batch;

import art.artifact_batch.Config.BatchProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BatchProperties.class)
public class ArtifactBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtifactBatchApplication.class, args);
    }

}
