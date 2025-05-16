package com.S2M.ArtifactTest.Config.ReadFileV2.Core.Util;

import com.S2M.ArtifactTest.Config.ReadFileV2.Core.AbstractFileReaderConfig;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;

public class MapperUtil {




    public static <T> FieldSetMapper<T> DefaultFieldSetMapper(Class<T> itemType) {

        BeanWrapperFieldSetMapper<T> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(itemType);

        return fieldSetMapper;
    }
}
