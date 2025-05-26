
package art.artifact_batch.Config.Writer.WriteDb;
import art.artifact_batch.Config.Writer.WriteDb.Providers.DefaultDatabaseWriterProvider;
import art.artifact_batch.Config.Writer.WriteDb.Providers.RepositoryItemWriterProvider;
import art.artifact_batch.Config.Writer.WriteDb.SPI.DatabaseWriterProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider; // Needed for JDBC
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider; // Interface for JDBC

import javax.sql.DataSource;

/**
 * Configuration for database-based ItemWriters (JDBC or Repository).
 * Active when batch.output.database.targetType is defined.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(DatabaseWriterProperties.class)
@ConditionalOnProperty(prefix = "batch.output.database", name = "database-type")
public class DatabaseWriterConfig {

    private final DataSource dataSource;
    private final DatabaseWriterProperties props;
    private final ApplicationContext ctx;

    /**
     * Provides the Class object for the target type (DTO/Entity) being written.
     * Uses a specific qualifier to avoid conflicts with a reader targetTypeClass bean.
     */
    @Bean(name = "writerTargetTypeClass")
    @ConditionalOnMissingBean(name = "writerTargetTypeClass")
    public Class<?> writerTargetTypeClass() {
        if (this.props.getResolvedTargetDatabaseType() == null) {
            throw new IllegalStateException("writerTargetTypeClass could not be resolved from DatabaseWriterProperties.");
        }
        return this.props.getResolvedTargetDatabaseType();
    }

    /**
     * Provides an ItemSqlParameterSourceProvider for JDBC writers.
     * Defaults to BeanPropertyItemSqlParameterSourceProvider which maps item properties to named parameters in SQL.
     * This bean can be overridden by a custom ItemSqlParameterSourceProvider<T>.

     */
    @Bean(name = "writerItemSqlParameterSourceProvider")
    @ConditionalOnMissingBean(name = "writerItemSqlParameterSourceProvider")
    @ConditionalOnProperty(prefix = "batch.output.database", name = "database-type", havingValue = "JDBC", matchIfMissing = true)
    @Lazy
    public <T> ItemSqlParameterSourceProvider<T> writerItemSqlParameterSourceProvider(
            @Qualifier("writerTargetTypeClass") Class<T> targetTypeClass) {
        log.info("Creating DEFAULT BeanPropertyItemSqlParameterSourceProvider for JDBC writer, target type: {}", targetTypeClass.getName());
        return new BeanPropertyItemSqlParameterSourceProvider<>();
    }


    /**
     * Creates a DatabaseWriterProvider for JDBC.
     * Conditional on writer-type=JDBC.
     */
    @Bean
    @ConditionalOnProperty(prefix = "batch.output.database", name = "database-type", havingValue = "JDBC")
    public <T> DatabaseWriterProvider<T> jdbcDatabaseWriterProvider(
            @Qualifier("writerItemSqlParameterSourceProvider") ItemSqlParameterSourceProvider<T> parameterSourceProvider,
            @Qualifier("writerTargetTypeClass") Class<T> targetTypeClass
    ) {
        log.info("Creating DefaultDatabaseWriterProvider instance for JDBC type: {}", targetTypeClass.getName());
        return new DefaultDatabaseWriterProvider<>(
                this.dataSource,
                this.props,
                parameterSourceProvider,
                targetTypeClass
        );
    }


    /**
     * Creates a DatabaseWriterProvider for Repository.
     * Conditional on writer-type=REPOSITORY.
     * Uses a specific qualifier.
     */
    @Bean
    @ConditionalOnProperty(prefix = "batch.output.database", name = "database-type", havingValue = "REPOSITORY")
    public <T> DatabaseWriterProvider<T> repositoryDatabaseWriterProvider(
            @Qualifier("writerTargetTypeClass") Class<T> entityTypeClass
    ) {
        return new RepositoryItemWriterProvider<>(
                this.ctx,
                this.props,
                entityTypeClass
        );
    }

    /**
     * Builds a type-safe ItemWriter<T> using the fully configured DatabaseWriterProvider.
     * Uses a specific qualifier for the main writer bean.
     */
    @Bean
    @ConditionalOnMissingBean(ItemWriter.class)
    public <T> ItemWriter<T> genericDatabaseItemWriter(
            DatabaseWriterProvider<T> provider
    ) throws Exception {
        return provider.createWriter();
    }
}
