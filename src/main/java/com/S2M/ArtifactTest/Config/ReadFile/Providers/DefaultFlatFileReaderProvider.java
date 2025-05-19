package com.S2M.ArtifactTest.Config.ReadFile.Providers;

import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import com.S2M.ArtifactTest.Config.ReadFile.SPI.FileReaderProvider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.LineMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;

import org.springframework.batch.item.file.FlatFileItemReader;

import org.springframework.core.io.Resource;

import org.springframework.stereotype.Component;



/**
 * Default provider for creating FlatFileItemReader instances.
 */

@Component
@Slf4j
@ConditionalOnExpression(
        "'${batch.input.type}' == 'DELIMITED' or '${batch.input.type}' == 'FIXED_LENGTH'"
)
public class DefaultFlatFileReaderProvider  implements FileReaderProvider {

    private final ResourceLoader resourceLoader;
    private final LineMapper<Object> lineMapper;

    public DefaultFlatFileReaderProvider(ResourceLoader resourceLoader,
                                         @Lazy LineMapper<Object> lineMapper) {
        this.resourceLoader = resourceLoader;
        this.lineMapper = lineMapper;
    }

    @Override
    public boolean supports(ReadProperties props) {
        return props != null &&
                (props.getType() == ReadProperties.FileType.DELIMITED ||
                        props.getType() == ReadProperties.FileType.FIXED_LENGTH);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ItemReader<T> createReader(ReadProperties props, Class<T> targetType) {


        Resource resource = resolveAndValidateResource(
                this.resourceLoader,
                props.getFilePath()
        );
        log.info("Reading file ................: {}", resource.getFilename());
        FlatFileItemReader<T> reader = new FlatFileItemReader<>();
        reader.setName("defaultFlatFileItemReader." + props.getType().name().toLowerCase());
        reader.setResource(resource);
        reader.setEncoding(props.getEncoding());
        reader.setLinesToSkip(props.getLinesToSkip());
        reader.setLineMapper((LineMapper<T>) lineMapper);
        return reader;
    }

}
