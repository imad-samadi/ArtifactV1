package art.artifact_batch.Config.Reader.ReadFile.Core;

import art.artifact_batch.Config.Reader.ReadFile.Providers.DefaultFlatFileReaderProvider;
import art.artifact_batch.Config.Reader.ReadFile.Providers.JsonFileReaderProvider;
import art.artifact_batch.Config.Reader.ReadFile.ReadProperties;
import art.artifact_batch.Config.Reader.ReadFile.SPI.FileReaderProvider;
import art.artifact_batch.Config.Reader.ReadFile.Util.TokenizerFactories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import art.artifact_batch.Config.Exceptions.ConfigurationException;



/**
 * Core configuration for the generic file reader component.
 */

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "batch.input.file", name = "filePath")//only register that bean if there is a property called batch.input.filePath defined in your application.yml
@EnableConfigurationProperties(ReadProperties.class)
@Slf4j
public class GenericFileReaderConfig {

    private final ReadProperties ReadProperties;
    private final ResourceLoader resourceLoader;


    @ConditionalOnMissingBean(name = "targetInputTypeClass")
    @Bean(name = "targetInputTypeClass")

    public Class<?> targetTypeClass() {
        try {
            return Class.forName(ReadProperties.getTargetModel());
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(
                    "Invalid target class: " + ReadProperties.getTargetModel(), e
            );
        }
    }


    @Bean
    @ConditionalOnMissingBean(LineTokenizer.class)
    @Lazy
    public LineTokenizer lineTokenizer() {
        log.info("LineTokenizer...........................;");
       return TokenizerFactories.getLineTokenizer(this.ReadProperties); //Choose the tokenizer based on the fileType

    }

    @Bean
    @ConditionalOnMissingBean(FieldSetMapper.class)
    @Lazy
    public <T> FieldSetMapper<T> genericFieldSetMapper(@Qualifier("targetInputTypeClass") Class<T> targetTypeClass) {
        log.info("FieldSetMapper...........................;");
        BeanWrapperFieldSetMapper<T> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(targetTypeClass);
        return fieldSetMapper;
    }

    @Bean
    @ConditionalOnMissingBean(LineMapper.class)
    @Lazy
    public <T> LineMapper<T> genericLineMapper(
            LineTokenizer lineTokenizer,
            FieldSetMapper<T> fieldSetMapper
    ) {
        log.info("LineMapper...........................;");
        DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    @Bean
    @ConditionalOnExpression("'${batch.input.file.file-type}' == 'DELIMITED' or '${batch.input.file.file-type}' == 'FIXED_LENGTH'")
    public FileReaderProvider defaultFlatFileReaderProvider(

             LineMapper<Object> lineMapper

    ) {
        log.info("Creating DefaultFlatFileReaderProvider for file type: {}", this.ReadProperties.getFileType());

        return new DefaultFlatFileReaderProvider(this.resourceLoader, lineMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "batch.input.file", name = "file-type", havingValue = "JSON")

    public FileReaderProvider jsonFileReaderProvider(

    ) {
        log.info("Creating JsonFileReaderProvider for file type: JSON");
        return new JsonFileReaderProvider(this.resourceLoader);
    }






    @ConditionalOnMissingBean(ItemReader.class)
    @Bean
    public <T> ItemReader<T> genericItemReader(
            FileReaderProvider fileReaderProvider,
           @Qualifier("targetInputTypeClass") Class<T> targetTypeClass
    ) {

        return fileReaderProvider.createReader(this.ReadProperties, targetTypeClass);
    }


}
