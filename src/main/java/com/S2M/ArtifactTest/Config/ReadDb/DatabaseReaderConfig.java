package com.S2M.ArtifactTest.Config.ReadDb;

import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Map;

/**
 * Main configuration class for database reader setup
 * Creates and wires all necessary components
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(DatabaseReaderProperties.class)
@ConditionalOnProperty(prefix = "batch.input.database", name = "targetType", matchIfMissing = false)
public class DatabaseReaderConfig {


    private final DataSource dataSource;
    private final DatabaseReaderProperties props; // Renamed for brevity

    @Bean(name = "genericDatabaseItemReader") // More generic name
    @StepScope
    public JdbcPagingItemReader<?> databaseItemReader(
            @Qualifier("databasePagingQueryProvider") PagingQueryProvider queryProvider,
            @Qualifier("databaseRowMapper") RowMapper<?> rowMapper) { // Renamed qualifier

        log.info("Configuring genericDatabaseItemReader for target: {}, page size: {}", props.getTargetType(), props.getPageSize());
        if (!props.getParameterValues().isEmpty()) log.debug("Reader parameters: {}", props.getParameterValues());

        return new JdbcPagingItemReaderBuilder<>()
                .name("genericDatabaseItemReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider)
                .parameterValues(props.getParameterValues())
                .pageSize(props.getPageSize())
                .rowMapper(rowMapper)
                .build();
    }

    @Bean(name = "databasePagingQueryProvider")
    public PagingQueryProvider queryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
        factory.setDataSource(dataSource);

        String actualSelect = props.getActualSelectClause();
        String actualFrom = props.getActualTableName();
        Map<String, Order> actualSorts = props.getActualSortOrders();

        log.debug("QueryProvider - SELECT: [{}], FROM: [{}], SORT: [{}]", actualSelect, actualFrom, actualSorts);
        factory.setSelectClause(actualSelect);
        factory.setFromClause(actualFrom);
        factory.setSortKeys(actualSorts);

        if (StringUtils.hasText(props.getWhereClause())) {
            log.debug("QueryProvider - WHERE: [{}]", props.getWhereClause());
            factory.setWhereClause(props.getWhereClause());
        }
        return factory.getObject();
    }

    @Bean(name = "databaseRowMapper") // Renamed bean
    public RowMapper<?> databaseRowMapper() {
        if (props.getResolvedTargetType() == null)
            throw new IllegalStateException("targetType not resolved in properties.");
        log.debug("Creating BeanPropertyRowMapper for: {}", props.getResolvedTargetType().getName());
        return new BeanPropertyRowMapper<>(props.getResolvedTargetType());
    }
}
