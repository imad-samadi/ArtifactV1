package com.S2M.ArtifactTest.Config.ReadFileV2.Core;

import com.S2M.ArtifactTest.Config.ReadFileV2.Core.SPI.FieldSetMapperProvider;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.SPI.TokenizerProvider;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.Validation.ConfigValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
@Component
@Slf4j
public class GenericReaderFactory {

    private final ConfigValidator configValidator;
    private final ResourceLoader resourceLoader;
    private final List<TokenizerProvider> tokenizerProviders;
    private final FieldSetMapperProvider fieldSetMapperProvider;


    public GenericReaderFactory(
            ConfigValidator configValidator,
            ResourceLoader resourceLoader,
            List<TokenizerProvider> tokenizerProviders,
            FieldSetMapperProvider<?> fieldSetMapperProvider) {
        this.configValidator = configValidator;
        this.resourceLoader = resourceLoader;
        this.tokenizerProviders = List.copyOf(tokenizerProviders);
        this.fieldSetMapperProvider = fieldSetMapperProvider;


        log.info("GenericReaderFactory: Initialized with {} TokenizerProvider(s).", this.tokenizerProviders.size());
        this.tokenizerProviders.forEach(p -> System.out.println(" - TokenizerProvider: " + p.getClass().getName()));
        log.info("GenericReaderFactory: Using FieldSetMapperProvider: " + this.fieldSetMapperProvider.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    public <T> ItemReader<T> createReader(AbstractFileReaderConfig<T> config) {
        configValidator.validate(config); // Step 1: Validate POJO annotations

        String readerNameSuffix = config.getClass().getSimpleName().replace("FileReaderConfig", "");
        String readerName = (config.getItemType() != null ? config.getItemType().getSimpleName() : "CustomMapped")
                + readerNameSuffix + "Reader";

        FlatFileItemReaderBuilder<T> springBatchItemReaderBuilder = new FlatFileItemReaderBuilder<T>()
                .name(readerName)
                .resource(resourceLoader.getResource(config.getResourcePath()))
                .linesToSkip(config.getLinesToSkip());

        if (config.getCustomLineMapper() != null) {
            springBatchItemReaderBuilder.lineMapper(config.getCustomLineMapper());
        } else {
            TokenizerProvider selectedTokenizerProvider = tokenizerProviders.stream()
                    .filter(tp -> tp.supports(config))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No suitable TokenizerProvider found for config: " + config.getClass().getName()));

            // createTokenizer and createFieldSetMapper will throw if config lacks their required fields
            LineTokenizer tokenizer = selectedTokenizerProvider.createTokenizer(config);
            FieldSetMapper<T> mapper = (FieldSetMapper<T>) fieldSetMapperProvider.createFieldSetMapper(config);

            DefaultLineMapper<T> defaultLineMapper = new DefaultLineMapper<>();
            defaultLineMapper.setLineTokenizer(tokenizer);
            defaultLineMapper.setFieldSetMapper(mapper);
            springBatchItemReaderBuilder.lineMapper(defaultLineMapper);
        }
        return springBatchItemReaderBuilder.build();
    }
}
