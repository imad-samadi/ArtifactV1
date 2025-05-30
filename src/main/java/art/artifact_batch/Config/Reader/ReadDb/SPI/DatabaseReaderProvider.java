package art.artifact_batch.Config.Reader.ReadDb.SPI;

import org.springframework.batch.item.ItemReader;

public interface DatabaseReaderProvider<T> {

    /**
     * Creates an ItemReader for database reading for type T
     * using its pre-configured dependencies.
     *
     * @return A configured ItemReader for type T.
     */
    ItemReader<T> createReader();
}
