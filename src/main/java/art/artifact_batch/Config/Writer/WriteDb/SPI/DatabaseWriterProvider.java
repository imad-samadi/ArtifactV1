package art.artifact_batch.Config.Writer.WriteDb.SPI;


import org.springframework.batch.item.ItemWriter;

/**
 * Service Provider Interface for creating database ItemWriters.
 * Implementations provide specific writer types (e.g., JDBC, Repository).
 *
 * @param <T> The type of item being written.
 */
public interface DatabaseWriterProvider<T> {

    /**
     * Creates an ItemWriter for database writing for type T
     * using its pre-configured dependencies.
     *
     * @return A configured ItemWriter for type T.
     */
    ItemWriter<T> createWriter() throws Exception;
}