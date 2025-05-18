package com.S2M.ArtifactTest.Config.ReadFile.SPI;

import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import org.springframework.batch.item.file.mapping.FieldSetMapper;

public interface FieldSetMapperProvider {

    /**
     * Creates a FieldSetMapper.
     * @param properties The configuration properties (can be used if needed by a specific provider).
     * @param targetTypeClass The class to which records will be mapped.
     * @param <T> The type of the target object.
     * @return A configured FieldSetMapper.
     */
    <T> FieldSetMapper<T> createFieldSetMapper(ReadProperties properties, Class<T> targetTypeClass);
}
