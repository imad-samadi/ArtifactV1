package art.artifact_batch.Config.Writer.WriteFile.Providers;

import art.artifact_batch.Config.Writer.WriteFile.SPI.FileWriterProvider;
import art.artifact_batch.Config.Writer.WriteFile.WriteProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "batch.output.file", name = "file-type", havingValue = "DELIMITED")
public class DefaultFlatFileWriterProvider implements FileWriterProvider {
    private final ResourceLoader resourceLoader;
    private final LineAggregator<Object> lineAggregator;
    public DefaultFlatFileWriterProvider(ResourceLoader resourceLoader,
                                         @Lazy LineAggregator<Object> lineAggregator) {
        this.resourceLoader = resourceLoader;
        this.lineAggregator = lineAggregator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ItemWriter<T> createWriter(WriteProperties props) {
        WritableResource resource = resolveResource(this.resourceLoader, props.getFilePath());
        FlatFileItemWriter<T> writer = new FlatFileItemWriter<>();
        writer.setName("defaultFlatFileItemWriter." + props.getFileType().name().toLowerCase());
        writer.setResource(resource);
        writer.setEncoding(props.getEncoding());
        writer.setAppendAllowed(props.isAppend());
        writer.setTransactional(props.isTransactional());
        writer.setLineAggregator((LineAggregator<T>) lineAggregator);
        writer.setHeaderCallback(writer1 -> writer1.write(props.getHeader()));
        if (props.getFooter() != null && !props.getFooter().isEmpty()) {
            writer.setFooterCallback(writer1 -> writer1.write(props.getFooter()));
        }
        return writer;
    }

}
