package com.S2M.ArtifactTest.Config.ReadFileV2.Delimited;

import com.S2M.ArtifactTest.Config.ReadFileV2.Core.AbstractFileReaderConfig;
import com.S2M.ArtifactTest.Config.ReadFileV2.Delimited.Config.DelimitedFileReaderConfig;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.Exceptions.ReaderConfigValidationException;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.ItemReaderBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class DelimitedFileItemReaderBuilder <T> implements ItemReaderBuilder<T, DelimitedFileReaderConfig<T>> {

    private final Validator validator;
    private final ResourceLoader resourceLoader;

    @Override
    public boolean supports(AbstractFileReaderConfig<?> config) {
        return config instanceof DelimitedFileReaderConfig;
    }

    @Override
    public void validate(DelimitedFileReaderConfig<T> config) {
        Set<ConstraintViolation<DelimitedFileReaderConfig<T>>> violations = validator.validate(config);

        List<String> errors = violations.stream()
                .map(v -> String.format("%s: %s (invalid value: '%s')",
                        v.getPropertyPath(), v.getMessage(), v.getInvalidValue()))
                .collect(Collectors.toList());

        if (!errors.isEmpty()) {
            throw new ReaderConfigValidationException(
                    "Invalid delimited file configuration", errors
            );
        }
    }

    @Override
    public ItemReader<T> build(DelimitedFileReaderConfig<T> config) {
        // 1. Validate the configuration
        validate(config); // Call the new validate method

        // 2. Build the ItemReader
        FlatFileItemReaderBuilder<T> springBatchItemReaderBuilder = new FlatFileItemReaderBuilder<T>()
                .name(config.getItemType().getSimpleName() + "DelimitedReader") // itemType should be valid here or validate() failed
                .resource(resourceLoader.getResource(config.getResourcePath()))
                .linesToSkip(config.getLinesToSkip());

        if (config.getCustomLineMapper() != null) {
            springBatchItemReaderBuilder.lineMapper(config.getCustomLineMapper());
        } else {
            // Default LineMapper creation (uses names and itemType, validated above)
            DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
            tokenizer.setDelimiter(config.getDelimiter());
            tokenizer.setNames(config.getNames()); // Assured non-null by validate()

            if (config.getQuoteCharacter() != null) {
                tokenizer.setQuoteCharacter(config.getQuoteCharacter());
            }

            BeanWrapperFieldSetMapper<T> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
            fieldSetMapper.setTargetType(config.getItemType()); // Assured non-null by validate()

            DefaultLineMapper<T> defaultLineMapper = new DefaultLineMapper<>();
            defaultLineMapper.setLineTokenizer(tokenizer);
            defaultLineMapper.setFieldSetMapper(fieldSetMapper);

            springBatchItemReaderBuilder.lineMapper(defaultLineMapper);
        }

        return springBatchItemReaderBuilder.build();
    }
}
