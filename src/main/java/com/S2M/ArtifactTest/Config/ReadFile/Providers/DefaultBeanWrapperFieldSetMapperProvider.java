package com.S2M.ArtifactTest.Config.ReadFile.Providers;

import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import com.S2M.ArtifactTest.Config.ReadFile.SPI.FieldSetMapperProvider;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;

public class DefaultBeanWrapperFieldSetMapperProvider implements FieldSetMapperProvider {

    @Override
    public <T> FieldSetMapper<T> createFieldSetMapper(ReadProperties properties, Class<T> targetTypeClass) {
        BeanWrapperFieldSetMapper<T> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(targetTypeClass);

        return fieldSetMapper;
    }
}
