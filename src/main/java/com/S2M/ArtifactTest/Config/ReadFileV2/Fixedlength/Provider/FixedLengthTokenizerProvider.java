package com.S2M.ArtifactTest.Config.ReadFileV2.Fixedlength.Provider;

import com.S2M.ArtifactTest.Config.ReadFileV2.Core.SPI.TokenizerProvider;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.AbstractFileReaderConfig;

import com.S2M.ArtifactTest.Config.ReadFileV2.Fixedlength.Config.FixedLengthFileReaderConfig;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
public class FixedLengthTokenizerProvider  implements TokenizerProvider {

    @Override
    public boolean supports(AbstractFileReaderConfig<?> config) {
        return config instanceof FixedLengthFileReaderConfig;
    }

    @Override
    public LineTokenizer createTokenizer(AbstractFileReaderConfig<?> abstractConfig) {
        FixedLengthFileReaderConfig<?> config = (FixedLengthFileReaderConfig<?>) abstractConfig;

        if (config.getRanges() == null || config.getRanges().length == 0) {
            throw new IllegalArgumentException("FixedLengthTokenizerProvider: 'ranges' array must be provided.");
        }
        if (config.getNames() == null || config.getNames().length == 0) {
            throw new IllegalArgumentException("FixedLengthTokenizerProvider: 'names' array must be provided.");
        }
        if (config.getRanges().length != config.getNames().length) {
            throw new IllegalArgumentException("FixedLengthTokenizerProvider: The number of 'ranges' must match 'names'.");
        }

        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setNames(config.getNames());
        tokenizer.setColumns(config.toRangeArray());
        return tokenizer;
    }
}
