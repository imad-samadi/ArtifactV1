package art.artifact_batch.Config.Writer.WriteFile.Providers;
import art.artifact_batch.Config.Writer.WriteFile.SPI.FileWriterProvider;
import art.artifact_batch.Config.Writer.WriteFile.WriteProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.core.io.WritableResource;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "batch.output.file", name = "file-type", havingValue = "JSON")
public class JsonFileWriterProvider implements FileWriterProvider {
    private final ResourceLoader resourceLoader;
    private final JsonObjectMarshaller<Object> jsonObjectMarshaller;

    public JsonFileWriterProvider(ResourceLoader resourceLoader, @Lazy
           JsonObjectMarshaller<Object> jsonObjectMarshaller) {
        this.resourceLoader = resourceLoader;
        this.jsonObjectMarshaller = jsonObjectMarshaller;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ItemWriter<T> createWriter(WriteProperties props) {
        WritableResource resource = resolveResource(resourceLoader, props.getFilePath());
        return new JsonFileItemWriterBuilder<T>()
                .jsonObjectMarshaller((JsonObjectMarshaller<T>) this.jsonObjectMarshaller)
                .resource(resource)
                .name("jsonFileItemWriter")
                .encoding(props.getEncoding())
                .append(props.isAppend())
                .transactional(props.isTransactional())
                .build();
    }
}
