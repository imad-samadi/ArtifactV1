package com.S2M.ArtifactTest.Config.ReadFile.SPI;

import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;

public interface LineMapperProvider {

    /**
     * Creates a LineMapper.
     * @param properties The configuration properties (can be used if needed by a specific provider).
     * @param lineTokenizer The LineTokenizer to be used by the LineMapper.
     * @param fieldSetMapper The FieldSetMapper to be used by the LineMapper.
     * @param <T> The type of the target object.
     * @return A configured LineMapper.
     */
    <T> LineMapper<T> createLineMapper(ReadProperties properties,
                                       LineTokenizer lineTokenizer,
                                       FieldSetMapper<T> fieldSetMapper);
}
