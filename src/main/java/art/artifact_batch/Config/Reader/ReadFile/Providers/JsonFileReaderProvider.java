package art.artifact_batch.Config.Reader.ReadFile.Providers;


import art.artifact_batch.Config.Reader.ReadFile.ReadProperties;
import art.artifact_batch.Config.Reader.ReadFile.SPI.FileReaderProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;


@RequiredArgsConstructor


public class JsonFileReaderProvider implements FileReaderProvider {

    private final ResourceLoader resourceLoader;



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
