package com.S2M.ArtifactTest.Config.ReadFile.Providers;

import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import com.S2M.ArtifactTest.Config.ReadFile.SPI.LineMapperProvider;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;

public class DefaultLineMapperProvider implements LineMapperProvider {

    @Override
    public <T> LineMapper<T> createLineMapper(ReadProperties properties,
                                              LineTokenizer lineTokenizer,
                                              FieldSetMapper<T> fieldSetMapper) {
        DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }
}
