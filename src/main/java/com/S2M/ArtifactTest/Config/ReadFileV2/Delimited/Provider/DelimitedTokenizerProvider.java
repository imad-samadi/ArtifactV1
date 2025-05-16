package com.S2M.ArtifactTest.Config.ReadFileV2.Delimited.Provider;

import com.S2M.ArtifactTest.Config.ReadFileV2.Core.AbstractFileReaderConfig;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.SPI.TokenizerProvider;
import com.S2M.ArtifactTest.Config.ReadFileV2.Delimited.Config.DelimitedFileReaderConfig;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.util.StringUtils;

public class DelimitedTokenizerProvider implements TokenizerProvider {

    @Override
    public boolean supports(AbstractFileReaderConfig<?> config) {
        return config instanceof DelimitedFileReaderConfig;
    }

    @Override
    public LineTokenizer createTokenizer(AbstractFileReaderConfig<?> abstractConfig) {

        DelimitedFileReaderConfig<?> config = (DelimitedFileReaderConfig<?>) abstractConfig;

        if (!StringUtils.hasText(config.getDelimiter())) {
            throw new IllegalArgumentException("DelimitedTokenizerProvider: 'delimiter' must be provided in DelimitedFileReaderConfig.");
        }
        if (config.getNames() == null || config.getNames().length == 0) {
            throw new IllegalArgumentException("DelimitedTokenizerProvider: 'names' array must be provided in DelimitedFileReaderConfig.");
        }

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(config.getDelimiter());
        tokenizer.setNames(config.getNames());
        if (config.getQuoteCharacter() != null) {
            tokenizer.setQuoteCharacter(config.getQuoteCharacter());
        }
        return tokenizer;
    }
}
