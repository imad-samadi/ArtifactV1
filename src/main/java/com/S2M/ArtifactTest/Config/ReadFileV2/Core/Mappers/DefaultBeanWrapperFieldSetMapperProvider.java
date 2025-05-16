package com.S2M.ArtifactTest.Config.ReadFileV2.Core.Mappers;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.AbstractFileReaderConfig;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.SPI.FieldSetMapperProvider;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
public class DefaultBeanWrapperFieldSetMapperProvider<T> implements FieldSetMapperProvider<T> {

    @Override
    public FieldSetMapper<T> createFieldSetMapper(AbstractFileReaderConfig<T> config) {
        // @AssertTrue on AbstractFileReaderConfig ensures itemType is present if customLineMapper is null
        if (config.getItemType() == null) {
            throw new IllegalArgumentException(
                    "DefaultBeanWrapperFieldSetMapperProvider: 'itemType' must be provided in the configuration."
            );
        }
        BeanWrapperFieldSetMapper<T> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(config.getItemType());
        return fieldSetMapper;
    }
}
