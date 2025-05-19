package com.S2M.ArtifactTest.Config.ReadDb;

import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import com.S2M.ArtifactTest.Demo.DTO.TransactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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


    @ConditionalOnMissingBean(name = "targetTypeClass")
    @Bean(name = "targetTypeClass")

    public Class<?> targetTypeClass() {
        try {
            return Class.forName(this.props.getTargetType());
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(
                    "Invalid target class: " + this.props.getTargetType(), e
            );
        }
    }

    private final DataSource dataSource;
    private final DatabaseReaderProperties props;

    /**
     * Builds a type-safe JdbcPagingItemReader<T> based on resolved targetTypeClass.
     */
    @Bean(name = "genericDatabaseItemReader")

    public <T> JdbcPagingItemReader<T> databaseItemReader(
            @Qualifier("databasePagingQueryProvider") PagingQueryProvider queryProvider,
            RowMapper<T> rowMapper,
            @Qualifier("targetTypeClass") Class<T> targetTypeClass) {

        log.info("Configuring genericDatabaseItemReader for target: {}, page size: {}",
                props.getTargetType(), props.getPageSize());
        if (!props.getParameterValues().isEmpty()) {
            log.info("Reader parameters: {}", props.getParameterValues());
        }

        return new JdbcPagingItemReaderBuilder<T>()
                .name("genericDatabaseItemReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider)
                .parameterValues(props.getParameterValues())
                .pageSize(props.getPageSize())
                .rowMapper(rowMapper)
                .build();
    }

    /**
     * Creates the SQL PagingQueryProvider based on properties.
     */
    @Bean(name = "databasePagingQueryProvider")
    public PagingQueryProvider queryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
        factory.setDataSource(dataSource);

        String select = props.getActualSelectClause();
        String from   = props.getActualTableName();
        Map<String, Order> sorts = props.getActualSortOrders();

        log.info("QueryProvider - SELECT: [{}], FROM: [{}], SORT: [{}]", select, from, sorts);
        factory.setSelectClause(select);
        factory.setFromClause("FROM " + from);
        factory.setSortKeys(sorts);

        if (StringUtils.hasText(props.getWhereClause())) {
            log.info("QueryProvider - WHERE: [{}]", props.getWhereClause());
            factory.setWhereClause(props.getWhereClause());
        }
        return factory.getObject();
    }


    /**
     * Creates a RowMapper<T> for the target type via BeanPropertyRowMapper.
     */
    @Bean(name = "defaultDatabaseRowMapper") // Different name for clarity
    @ConditionalOnMissingBean(name = "userProvidedDatabaseRowMapper") // Condition on a specific user bean name
    public <T> RowMapper<T> defaultDatabaseRowMapper(
            @Qualifier("targetTypeClass") Class<T> targetTypeClass) {
        log.info("Creating DEFAULT BeanPropertyRowMapper .........** for: {}", targetTypeClass.getName());
        return new BeanPropertyRowMapper<>(targetTypeClass);
    }



}
