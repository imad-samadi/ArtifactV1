
package art.artifact_batch.Config.Writer.WriteDb.Providers;
import art.artifact_batch.Config.Exceptions.ConfigurationException;
import art.artifact_batch.Config.Writer.WriteDb.DatabaseWriterProperties;
import art.artifact_batch.Config.Writer.WriteDb.SPI.DatabaseWriterProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;



/**
 * Provides a Spring Data Repository ItemWriter configured using properties.
 * Suitable for simple save operations on entities.
 *
 * @param <T> The type of item being written (expected to be an Entity).
 */
@RequiredArgsConstructor
@Slf4j
public class RepositoryItemWriterProvider<T> implements DatabaseWriterProvider<T> {

    private final ApplicationContext ctx;
    private final DatabaseWriterProperties props;
    private final Class<T> entityType;

    @Override
    public ItemWriter<T> createWriter() throws Exception {
        Repository<?, ?> repository;
        try {
            repository = (Repository<?, ?>) ctx.getBean(props.getServiceBeanName());
        } catch (NoSuchBeanDefinitionException e) {
            throw new ConfigurationException("Repository bean (serviceBeanName: '" + props.getServiceBeanName() + "') not found.", e);
        } catch (ClassCastException e) {
            log.warn("Bean with name '{}' is not a standard Spring Data Repository interface. Casting may fail or method invocation may not work as expected.", props.getServiceBeanName(), e);
            repository = (Repository<?, ?>) ctx.getBean(props.getServiceBeanName());
        }
        RepositoryItemWriterBuilder<T> builder = new RepositoryItemWriterBuilder<T>()
                .repository((CrudRepository<T, ?>) repository);
        RepositoryItemWriter<T> writer = builder.build();
        writer.afterPropertiesSet();
        log.info("Repository ItemWriter built successfully for entity: {}", this.entityType.getName());
        return writer;
    }
}