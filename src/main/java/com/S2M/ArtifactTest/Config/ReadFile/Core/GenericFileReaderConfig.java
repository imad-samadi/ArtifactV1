package com.S2M.ArtifactTest.Config.ReadFile.Core;

import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;

import com.S2M.ArtifactTest.Config.ReadFile.SPI.FileReaderProvider;
import com.S2M.ArtifactTest.Config.ReadFile.Util.TokenizerFactories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;


import java.util.List;


/**
 * Core configuration for the generic file reader component.
 */

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "batch.input", name = "filePath")//only register that bean if there is a property called batch.input.filePath defined in your application.yml
@EnableConfigurationProperties(ReadProperties.class)
@Slf4j
public class GenericFileReaderConfig {

    private final ReadProperties ReadProperties;
    //private final List<FileReaderProvider> fileReaderProviders;
    private final FileReaderProvider FileReaderProvider;


    @ConditionalOnMissingBean(name = "targetTypeClass")
    @Bean(name = "targetTypeClass")

    public Class<?> targetTypeClass() {
        try {
            return Class.forName(ReadProperties.getTargetType());
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(
                    "Invalid target class: " + ReadProperties.getTargetType(), e
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
    public <T> FieldSetMapper<T> genericFieldSetMapper(Class<T> targetTypeClass) {
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


  /*  @Bean(name = "genericItemReader")
    @ConditionalOnMissingBean(ItemReader.class)
    public <T> ItemReader<T> genericItemReader(
            Class<T> targetTypeClass
    ) {

        if (fileReaderProviders.isEmpty()) {
            throw new ConfigurationException("No FileReaderProvider beans are configured.");
        }

        final ReadProperties propsForProvider = this.ReadProperties;

        return fileReaderProviders.stream()
                .filter(provider -> provider.supports(propsForProvider))
                .findFirst()
                .orElseThrow(() -> new ConfigurationException(
                        "No FileReaderProvider found for: " + propsForProvider.toString()
                ))
                .createReader(propsForProvider, targetTypeClass);
    }*/

    @Bean(name = "genericItemReader")
    @ConditionalOnMissingBean(ItemReader.class)
    public <T> ItemReader<T> genericItemReader(
            Class<T> targetTypeClass
    ) {

        return this.FileReaderProvider.createReader(this.ReadProperties, targetTypeClass);
    }


}
