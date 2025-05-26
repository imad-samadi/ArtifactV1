package art.artifact_batch.Config.Reader.ReadFile.Providers;


import art.artifact_batch.Config.Reader.ReadFile.ReadProperties;
import art.artifact_batch.Config.Reader.ReadFile.SPI.FileReaderProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.Resource;



/**
 * Default provider for creating FlatFileItemReader instances.
 */


@Slf4j

public class DefaultFlatFileReaderProvider  implements FileReaderProvider {

    private final ResourceLoader resourceLoader;
    private final LineMapper<Object> lineMapper;

    public DefaultFlatFileReaderProvider(ResourceLoader resourceLoader,
                                         @Lazy LineMapper<Object> lineMapper) {
        this.resourceLoader = resourceLoader;
        this.lineMapper = lineMapper;
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
        reader.setName("defaultFlatFileItemReader." + props.getFileType().name().toLowerCase());
        reader.setResource(resource);
        reader.setEncoding(props.getEncoding());
        reader.setLinesToSkip(props.getLinesToSkip());
        reader.setLineMapper((LineMapper<T>) lineMapper);
        return reader;
    }

}
