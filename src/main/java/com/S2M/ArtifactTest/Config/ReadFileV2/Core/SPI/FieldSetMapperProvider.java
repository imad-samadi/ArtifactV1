package com.S2M.ArtifactTest.Config.ReadFileV2.Core.SPI;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.AbstractFileReaderConfig;
import org.springframework.batch.item.file.mapping.FieldSetMapper;


public interface FieldSetMapperProvider<T> {

    FieldSetMapper<T> createFieldSetMapper(AbstractFileReaderConfig<T> config);
}
