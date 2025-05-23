package com.S2M.ArtifactTest;

import com.S2M.ArtifactTest.Config.BatchProperties;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BatchProperties.class)

public class ArtifactTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArtifactTestApplication.class, args);
	}

}
