package art.artifact_batch.Config.Writer.WriteFile.Core;


import art.artifact_batch.Config.Exceptions.ConfigurationException;
import art.artifact_batch.Config.Writer.WriteFile.SPI.FileWriterProvider;
import art.artifact_batch.Config.Writer.WriteFile.WriteProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonObjectMarshaller;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

/**
 * Core configuration for the generic file writer component.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "batch.output.file", name = "file-path")
@EnableConfigurationProperties(WriteProperties.class)
@Slf4j
public class GenericFileWriterConfig {
    private final WriteProperties writeProperties;
    private final FileWriterProvider fileWriterProvider;


    @Bean
    @ConditionalOnMissingBean(FieldExtractor.class)
    @Lazy
    public <T> FieldExtractor<T> defaultFieldExtractor() {
        log.info("Creating default BeanWrapperFieldExtractor with names: {}", (Object) writeProperties.getFieldNames());
        BeanWrapperFieldExtractor<T> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(writeProperties.getFieldNames());
        try {
            extractor.afterPropertiesSet();
        } catch (Exception e) {
            throw new ConfigurationException("Failed to initialize default BeanWrapperFieldExtractor. Check 'batch.output.fieldNames' or 'batch.output.targetClassName'.", e);
        }
        return extractor;
    }



    @Bean
    @ConditionalOnMissingBean(LineAggregator.class)
    @Lazy
    public <T> LineAggregator<T> defaultLineAggregator(
            @Lazy FieldExtractor<T> fieldExtractor) {
        Assert.notNull(fieldExtractor, "Injected FieldExtractor cannot be null for defaultLineAggregator");
        DelimitedLineAggregator<T> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(writeProperties.getDelimiter());
        aggregator.setFieldExtractor(fieldExtractor);
        return aggregator;
    }

    /**
     * Provides a default JacksonJsonObjectMarshaller
     */
    @Bean
    @ConditionalOnMissingBean(JsonObjectMarshaller.class)
    @Lazy
    public <T> JsonObjectMarshaller<T> defaultJsonObjectMarshaller(ObjectMapper objectMapper) {
        return new JacksonJsonObjectMarshaller<>(objectMapper);
    }


    // Alternative implementation using FileWriterProvider directly
    @Bean
    @ConditionalOnMissingBean(ItemWriter.class)
    public <T> ItemWriter<T> genericItemWriter() {
        return fileWriterProvider.createWriter(writeProperties);
    }
}
