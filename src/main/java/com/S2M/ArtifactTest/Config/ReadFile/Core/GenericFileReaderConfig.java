package com.S2M.ArtifactTest.Config.ReadFile.Core;

import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import com.S2M.ArtifactTest.Config.ReadFile.SPI.TokenizerProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.List;

/**
 * Core Spring Configuration class for setting up a generic flat file reader.
 * This class provides beans for {@link ItemReader}, {@link LineMapper},
 * {@link LineTokenizer}, and {@link FieldSetMapper} based on externalized
 * properties from {@link ReadProperties}.
 * It leverages a list of {@link TokenizerProvider}s to support different
 * file tokenization strategies (e.g., delimited, fixed-length).
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ReadProperties.class)
public class GenericFileReaderConfig {


    private final ReadProperties properties;
    private final ResourceLoader resourceLoader;
    private final List<TokenizerProvider> tokenizerProviders; // All implementations of TokenizerProvider will be injected

    /**
     * Provides the target class for mapping file records.
     * The class name is read from configuration properties.
     * This bean can be overridden by the user if needed.
     * @return The Class object for the target type.
     * @throws ConfigurationException if the class specified in properties cannot be found.
     */
    @Bean(name = "targetTypeClass") // Bean name matches qualifier used in genericFieldSetMapper
    @ConditionalOnMissingBean(name = "targetTypeClass")
    public Class<?> targetTypeClass() {
        try {
            return Class.forName(properties.getTargetType());
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(
                    "Invalid target class specified in properties: " + properties.getTargetType(), e
            );
        }
    }

    /**
     * Dynamically creates a {@link LineTokenizer} based on the configured file type.
     * It iterates through available {@link TokenizerProvider}s to find one that
     * supports the current configuration.
     * This bean can be overridden by the user.
     * @return A configured {@link LineTokenizer}.
     * @throws ConfigurationException if no suitable {@link TokenizerProvider} is found.
     */
    @Bean
    @ConditionalOnMissingBean(LineTokenizer.class)
    public LineTokenizer lineTokenizer() {
        return tokenizerProviders.stream()
                .filter(provider -> provider.supports(properties))
                .findFirst()
                .orElseThrow(() -> new ConfigurationException(
                        "No TokenizerProvider found for configured file type: " + properties.getType()
                ))
                .createTokenizer(properties);
    }

    /**
     * Provides a default {@link FieldSetMapper} that maps fields to a Java bean.
     * Uses {@link BeanWrapperFieldSetMapper} for this purpose.
     * This bean can be overridden by the user for custom mapping logic.
     * @param targetTypeClass The class to which each line's fields will be mapped.
     * @param <T> The generic type of the target class.
     * @return A configured {@link FieldSetMapper}.
     */
    @Bean
    @ConditionalOnMissingBean(FieldSetMapper.class)
    public <T> FieldSetMapper<T> genericFieldSetMapper(
            /*@Qualifier("targetTypeClass") implicitly used due to bean name match*/ Class<T> targetTypeClass
    ) {
        BeanWrapperFieldSetMapper<T> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(targetTypeClass);

        return fieldSetMapper;
    }

    /**
     * Provides a default {@link LineMapper} that combines a {@link LineTokenizer}
     * and a {@link FieldSetMapper}.
     * This bean can be overridden by the user.
     * @param lineTokenizer The tokenizer to split a line into fields.
     * @param fieldSetMapper The mapper to convert fields into an object.
     * @param <T> The generic type of the object to be mapped.
     * @return A configured {@link LineMapper}.
     */
    @Bean
    @ConditionalOnMissingBean(LineMapper.class)
    public <T> LineMapper<T> genericLineMapper(LineTokenizer lineTokenizer,
                                               FieldSetMapper<T> fieldSetMapper) {
        DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    /**
     * Creates a {@link FlatFileItemReader} configured according to {@link ReadProperties}.
     * This reader is {@link StepScope} to ensure it's instantiated per step execution,
     * which is crucial for restartability and handling step-specific parameters (though not used here for file path).
     * This bean can be overridden by the user.
     * @param lineMapper The line mapper to process each line of the file.
     * @param stepProperties The {@link ReadProperties} injected at step scope (same instance as application scope here).
     * @param stepResourceLoader The {@link ResourceLoader} injected at step scope.
     * @param <T> The generic type of the items to be read.
     * @return A configured {@link FlatFileItemReader}.
     * @throws ConfigurationException if the input file is not found.
     */
    @Bean(name = "genericFlatFileItemReader") // Explicit bean name for clarity if qualified elsewhere
    @ConditionalOnMissingBean(name = "genericFlatFileItemReader")
    @StepScope // Crucial for readers/writers: new instance per step execution
    public <T> FlatFileItemReader<T> genericFlatFileItemReader(
            LineMapper<T> lineMapper,

            ReadProperties stepProperties,
            ResourceLoader stepResourceLoader
    )  {
        // Resolve the resource using the injected ResourceLoader
        Resource resource = stepResourceLoader.getResource(stepProperties.getFilePath());

        if (!resource.exists()) {
            String resolvedPathInfo = "";

            try {
                if (resource.isFile()) {
                    resolvedPathInfo = ". Resolved absolute path: " + resource.getFile().getAbsolutePath();
                }
            } catch (IOException ignored) {
                // Ignored if it's not a file resource (e.g., classpath resource not found on filesystem)
                // or if getFile() throws an exception for non-file resources.
            }
            throw new ConfigurationException(
                    "Input file not found at path: " + stepProperties.getFilePath() + resolvedPathInfo
            );
        }

        FlatFileItemReader<T> reader = new FlatFileItemReader<>();
        reader.setName("genericFlatFileItemReader");
        reader.setResource(resource);
        reader.setEncoding(stepProperties.getEncoding());
        reader.setLinesToSkip(stepProperties.getLinesToSkip());
        reader.setLineMapper(lineMapper);

        return reader;
    }
}
