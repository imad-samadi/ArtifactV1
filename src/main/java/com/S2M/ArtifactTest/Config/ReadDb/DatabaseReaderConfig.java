package com.S2M.ArtifactTest.Config.ReadDb;

import com.S2M.ArtifactTest.Config.ReadDb.Providers.DefaultDatabaseReaderProvider;
import com.S2M.ArtifactTest.Config.ReadDb.Providers.RepositoryItemReaderProvider;
import com.S2M.ArtifactTest.Config.ReadDb.SPI.DatabaseReaderProvider;
import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Configuration for JDBC paging ItemReader.
 * Active when batch.input.database.targetType is defined.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(DatabaseReaderProperties.class)
@ConditionalOnProperty(prefix = "batch.input.database", name = "targetType")
public class DatabaseReaderConfig {

    private final DataSource dataSource;
    private final DatabaseReaderProperties props;
    private final ApplicationContext ctx;


    @ConditionalOnMissingBean(name = "targetInputTypeClass")
    @Bean(name = "targetInputTypeClass")

    public Class<?> targetTypeClass() {
        try {
            return Class.forName(this.props.getTargetType());
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(
                    "Invalid target class: " + this.props.getTargetType(), e
            );
        }
    }


    @Bean(name = "databasePagingQueryProvider")
    @ConditionalOnMissingBean(name = "databasePagingQueryProvider")
    @Lazy
    public PagingQueryProvider databasePagingQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
        factory.setDataSource(this.dataSource);

        String select = this.props.getActualSelectClause();
        String from = this.props.getActualTableName();
        Map<String, Order> sorts = this.props.getActualSortOrders();

        log.info("Database PagingQueryProvider - SELECT: [{}], FROM: [{}], SORT: [{}]", select, from, sorts);
        factory.setSelectClause(select);
        factory.setFromClause("FROM " + from);
        factory.setSortKeys(sorts);

        if (StringUtils.hasText(this.props.getWhereClause())) {
            log.info("Database PagingQueryProvider - WHERE: [{}]", this.props.getWhereClause());
            factory.setWhereClause(this.props.getWhereClause());
        }
        return factory.getObject();
    }

    /**
     * Creates a RowMapper<T> for the target type via BeanPropertyRowMapper.
     */

    @Bean
    @ConditionalOnMissingBean(RowMapper.class)
    @Lazy
    public <T> RowMapper<T> defaultDatabaseRowMapper(
            @Qualifier("targetInputTypeClass") Class<T> targetTypeClass) {
        log.info("Creating DEFAULT BeanPropertyRowMapper for database reader, target type: {}", targetTypeClass.getName());
        return new BeanPropertyRowMapper<>(targetTypeClass);
    }

    @Bean
    @ConditionalOnProperty(prefix = "batch.input.database", name = "reader-type", havingValue = "JDBC")
    public <T> DatabaseReaderProvider<T> genericDatabaseReaderProvider(

            @Qualifier("databasePagingQueryProvider") PagingQueryProvider queryProvider,
            RowMapper<T> rowMapper,
            @Qualifier("targetInputTypeClass") Class<T> targetTypeClass
    ) {
        log.info("Creating DefaultDatabaseReaderProvider instance for type: {}", targetTypeClass.getName());
        return new DefaultDatabaseReaderProvider<>(
                this.dataSource,
                this.props,
                queryProvider,
                rowMapper,
                targetTypeClass
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "batch.input.database", name = "reader-type", havingValue = "REPOSITORY")
    public <T> DatabaseReaderProvider<T> repositoryDatabaseReaderProvider(
            @Qualifier("targetInputTypeClass") Class<T> entityTypeClass
    ) {
        log.info("Creating RepositoryItemReaderProvider for repository (serviceBeanName): {}, method (serviceMethodName): {}, entity: {}",
                props.getServiceBeanName(), props.getServiceMethodName(), entityTypeClass.getName());
        return new RepositoryItemReaderProvider<>(
                this.ctx,
                this.props,
                entityTypeClass
        );
    }

    /**
     * Builds a type-safe ItemReader<T> using the fully configured DatabaseReaderProvider.
     */
    @Bean
    @Qualifier("genericItemReader")

    public <T> ItemReader<T> genericDatabaseItemReader(
            DatabaseReaderProvider<T> provider
    ) {
        log.info("Configuring genericDatabaseItemReader using pre-configured Provider for target type: {}",
                props.getTargetType()); // props.getTargetType() for logging context
        return provider.createReader();
    }



}
