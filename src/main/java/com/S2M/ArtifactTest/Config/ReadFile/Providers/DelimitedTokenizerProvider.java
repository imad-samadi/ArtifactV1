package com.S2M.ArtifactTest.Config.ReadFile.Providers;

import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import com.S2M.ArtifactTest.Config.ReadFile.SPI.TokenizerProvider;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * A {@link TokenizerProvider} implementation for creating {@link DelimitedLineTokenizer}.
 * This provider is used when the configured file type is {@link ReadProperties.FileType#DELIMITED}.
 */
@Component
public class DelimitedTokenizerProvider implements TokenizerProvider {

    /**
     * Checks if this provider supports the given properties, specifically if the
     * file type is DELIMITED.
     * @param properties The configuration properties.
     * @return true if file type is DELIMITED, false otherwise.
     */
    @Override
    public boolean supports(ReadProperties properties) {
        return ReadProperties.FileType.DELIMITED.equals(properties.getType());
    }

    /**
     * Creates and configures a {@link DelimitedLineTokenizer} based on the provided properties.
     * Sets the delimiter, quote character (if specified), and column names.
     * @param properties The configuration properties.
     * @return A configured {@link DelimitedLineTokenizer}.
     * @throws ConfigurationException if the delimiter is missing for a DELIMITED file type.
     */
    @Override
    public LineTokenizer createTokenizer(ReadProperties properties) {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();


        if (!StringUtils.hasText(properties.getDelimiter())) {
            throw new ConfigurationException("Delimiter is required for DELIMITED file type but was not provided or is empty.");
        }
        tokenizer.setDelimiter(properties.getDelimiter());

        // Set quote character if provided
        if (properties.getQuoteCharacter() != null) {
            tokenizer.setQuoteCharacter(properties.getQuoteCharacter());
        }


        tokenizer.setNames(properties.getNames());


        return tokenizer;
    }
}
