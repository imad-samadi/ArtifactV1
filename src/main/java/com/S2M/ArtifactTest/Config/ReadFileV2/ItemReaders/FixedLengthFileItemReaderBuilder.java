package com.S2M.ArtifactTest.Config.ReadFileV2.ItemReaders;
import com.S2M.ArtifactTest.Config.ReadFileV2.Config.AbstractFileReaderConfig;
import com.S2M.ArtifactTest.Config.ReadFileV2.Exceptions.ReaderConfigValidationException;
import com.S2M.ArtifactTest.Config.ReadFileV2.Config.FixedLengthFileReaderConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
// import org.springframework.stereotype.Component; // If using Spring

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class  FixedLengthFileItemReaderBuilder<T> implements ItemReaderBuilder<T, FixedLengthFileReaderConfig<T>> {
    private final Validator validator;
    private final ResourceLoader resourceLoader;

    @Override
    public boolean supports(AbstractFileReaderConfig<?> config) {
        return config instanceof FixedLengthFileReaderConfig;
    }


    @Override
    public ItemReader<T> build(FixedLengthFileReaderConfig<T> config) {
        validate(config);
        String readerName = (config.getItemType() != null ? config.getItemType().getSimpleName() : "CustomMapped") + "FixedLengthReader";

        FlatFileItemReaderBuilder<T> springBatchItemReaderBuilder = new FlatFileItemReaderBuilder<T>()
                .name(readerName)
                .resource(resourceLoader.getResource(config.getResourcePath()))
                .linesToSkip(config.getLinesToSkip());

        if (config.getCustomLineMapper() != null) {
            springBatchItemReaderBuilder.lineMapper(config.getCustomLineMapper());
        } else {
            // Default LineMapper creation - assumes validate() ensured necessary fields are present
            FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
            tokenizer.setColumns(config.toRangeArray()); // Relies on ranges being valid
            tokenizer.setNames(config.getNames());       // Relies on names being valid
            BeanWrapperFieldSetMapper<T> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
            fieldSetMapper.setTargetType(config.getItemType()); // Relies on itemType being valid
            DefaultLineMapper<T> defaultLineMapper = new DefaultLineMapper<>();
            defaultLineMapper.setLineTokenizer(tokenizer);
            defaultLineMapper.setFieldSetMapper(fieldSetMapper);
            springBatchItemReaderBuilder.lineMapper(defaultLineMapper);
        }

        return springBatchItemReaderBuilder.build();
    }



    @Override
    public void validate(FixedLengthFileReaderConfig<T> config) {
        Set<ConstraintViolation<FixedLengthFileReaderConfig<T>>> violations = validator.validate(config);

        List<String> errors = new ArrayList<>();
        violations.stream()
                .map(v -> String.format("%s: %s", v.getPropertyPath(), v.getMessage()))
                .forEach(errors::add);

        // Additional programmatic validation
        if (config.getCustomLineMapper() == null) {
            try {
                Range[] ranges = config.toRangeArray();
                validateRangeContinuity(ranges, errors);
            } catch (Exception e) {
                errors.add("Invalid range specification: " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new ReaderConfigValidationException(
                    "Fixed-length configuration validation failed", errors
            );
        }
    }

    private void validateRangeContinuity(Range[] ranges, List<String> errors) {
        int previousEnd = 0;
        for (int i = 0; i < ranges.length; i++) {
            Range current = ranges[i];
            if (current.getMin() <= previousEnd) {
                errors.add(String.format(
                        "Range %d-%d overlaps with previous range ending at %d",
                        current.getMin(), current.getMax(), previousEnd
                ));
            }
            previousEnd = current.getMax();
        }
    }
}
