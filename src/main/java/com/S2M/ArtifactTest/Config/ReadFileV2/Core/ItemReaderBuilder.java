package com.S2M.ArtifactTest.Config.ReadFileV2.Core;

import org.springframework.batch.item.ItemReader;

/**
 * Interface for components that can build an ItemReader based on a specific configuration.
 *
 * @param <T> The type of item the reader will produce.
 * @param <C> The type of configuration this builder supports, extending AbstractFileReaderConfig.
 */

public interface ItemReaderBuilder <T, C extends AbstractFileReaderConfig<T>>{

    /**
     * Checks if this builder can handle the provided configuration instance.
     * Typically, this involves an 'instanceof' check against the specific
     * configuration type the builder is designed for.
     *
     * @param config The configuration object to check.
     * @return true if this builder supports the given configuration, false otherwise.
     */
    boolean supports(AbstractFileReaderConfig<?> config);

    /**
     * Builds and returns an {@link ItemReader} based on the provided configuration.
     * @param config The configuration object, castable to the specific type C.
     * @return A configured ItemReader
     */
    ItemReader<T> build(C config);


    /**
     * Validates the provided configuration object.
     * Implementations should use a {@link jakarta.validation.Validator} to check
     * constraints defined on the configuration object.
     *
     * @param config The configuration object to validate.
     * @throws IllegalArgumentException if the configuration is found to be invalid.
     *                                  The exception message should detail the validation failures.
     */
    void validate(C config) throws IllegalArgumentException;
}
