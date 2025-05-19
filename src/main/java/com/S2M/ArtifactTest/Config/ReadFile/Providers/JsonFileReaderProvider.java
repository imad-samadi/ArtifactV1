package com.S2M.ArtifactTest.Config.ReadFile.Providers;

import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import com.S2M.ArtifactTest.Config.ReadFile.SPI.FileReaderProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;


@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "batch.input", name = "type", havingValue = "JSON")
public class JsonFileReaderProvider implements FileReaderProvider {

    private final ResourceLoader resourceLoader;



    @Override
    public boolean supports(ReadProperties props) {
        return props != null && props.getType() == ReadProperties.FileType.JSON;
    }

    @Override
    public <T> ItemReader<T> createReader(ReadProperties props, Class<T> targetType) {



        Resource resource = resolveAndValidateResource(
                this.resourceLoader,
                props.getFilePath()
        );




        return new JsonItemReaderBuilder<T>()
                .name("jsonItemReader." + targetType.getSimpleName())
                .jsonObjectReader(new JacksonJsonObjectReader<>(targetType))
                .resource(resource)
                .build();
    }
}
