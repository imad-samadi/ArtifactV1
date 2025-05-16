package com.S2M.ArtifactTest.Config.ReadFileV2.Core.AutoConfigure;

import com.S2M.ArtifactTest.Config.ReadFileV2.Core.GenericReaderFactory;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.Mappers.DefaultBeanWrapperFieldSetMapperProvider;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.SPI.FieldSetMapperProvider;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.SPI.TokenizerProvider;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.Validation.ConfigValidator;
import com.S2M.ArtifactTest.Config.ReadFileV2.Delimited.Provider.DelimitedTokenizerProvider;
import com.S2M.ArtifactTest.Config.ReadFileV2.Fixedlength.Provider.FixedLengthTokenizerProvider;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Removed @Import for specific ItemReaderBuilder impls as they no longer exist
// The providers and factory are now the main components.
// ConfigValidator should be @Component. GenericReaderFactory should be @Component.
// The default providers are defined as beans here.
import org.springframework.core.io.ResourceLoader;

import java.util.List;
@Configuration
public class BatchReaderAutoConfiguration {

    // --- Tokenizer Providers ---
    @Bean("defaultDelimitedTokenizerProvider") // Explicit name for clarity
    @ConditionalOnMissingBean(name = "defaultDelimitedTokenizerProvider")
    public TokenizerProvider delimitedTokenizerProvider() {
        return new DelimitedTokenizerProvider();
    }

    @Bean("defaultFixedLengthTokenizerProvider") // Explicit name
    @ConditionalOnMissingBean(name = "defaultFixedLengthTokenizerProvider")
    public TokenizerProvider fixedLengthTokenizerProvider() {
        return new FixedLengthTokenizerProvider();
    }

    // --- FieldSetMapper Provider ---
    @Bean
    @ConditionalOnMissingBean(FieldSetMapperProvider.class)
    @SuppressWarnings({"rawtypes", "unchecked"}) // For FieldSetMapperProvider<?>
    public FieldSetMapperProvider<?> defaultFieldSetMapperProvider() {
        return new DefaultBeanWrapperFieldSetMapperProvider();
    }


}
