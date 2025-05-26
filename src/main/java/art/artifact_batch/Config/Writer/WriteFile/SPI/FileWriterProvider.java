package art.artifact_batch.Config.Writer.WriteFile.SPI;


import art.artifact_batch.Config.Writer.WriteFile.WriteProperties;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
public interface FileWriterProvider {
    <T> ItemWriter<T> createWriter(WriteProperties props);

    default WritableResource resolveResource(ResourceLoader resourceLoader, String filePath) {
        WritableResource resource = (WritableResource) resourceLoader.getResource(filePath);
        return resource;
    }
}