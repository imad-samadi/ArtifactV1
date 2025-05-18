package com.S2M.ArtifactTest.Config.ReadFile.SPI;

import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import org.springframework.batch.item.file.transform.LineTokenizer;

/**
 * Service Provider Interface (SPI) for creating {@link LineTokenizer} instances.
 * Implementations of this interface can provide different tokenization strategies
 * (e.g., for delimited files, fixed-length files, etc.) based on the
 * provided {@link ReadProperties}.
 *
 * This allows the file reading artifact to be extensible with new tokenizer types
 * without modifying its core configuration logic.
 */
public interface TokenizerProvider {

    /**
     * Checks if this provider can create a {@link LineTokenizer} suitable for
     * the given configuration properties.
     *
     * @param properties The configuration properties defining the file format and content.
     * @return {@code true} if this provider supports the configuration, {@code false} otherwise.
     */
    boolean supports(ReadProperties properties);

    /**
     * Creates and configures a {@link LineTokenizer} based on the given configuration properties.
     * This method should only be called if {@link #supports(ReadProperties)} returns {@code true}
     */
    LineTokenizer createTokenizer(ReadProperties properties);
}
