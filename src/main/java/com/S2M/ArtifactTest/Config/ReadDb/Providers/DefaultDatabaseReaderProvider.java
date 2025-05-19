package com.S2M.ArtifactTest.Config.ReadDb.Providers;

import com.S2M.ArtifactTest.Config.ReadDb.DatabaseReaderProperties;
import com.S2M.ArtifactTest.Config.ReadDb.SPI.DatabaseReaderProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;


@Slf4j

public class DefaultDatabaseReaderProvider<T> implements DatabaseReaderProvider<T> {

    private final DataSource dataSource;
    private final DatabaseReaderProperties props;
    private final PagingQueryProvider queryProvider;
    private final RowMapper<T> rowMapper;
    private final Class<T> targetType;


    public DefaultDatabaseReaderProvider(
            DataSource dataSource,
            DatabaseReaderProperties props,
            PagingQueryProvider queryProvider,
            RowMapper<T> rowMapper,
            Class<T> targetType) {
        this.dataSource = dataSource;
        this.props = props;
        this.queryProvider = queryProvider;
        this.rowMapper = rowMapper;
        this.targetType = targetType;
        log.info("DefaultDatabaseReaderProvider initialized for type: {}", targetType.getName());
    }

    @Override
    public ItemReader<T> createReader() {
        log.debug("DefaultDatabaseReaderProvider creating JDBC Paging reader for target type: {}, page size: {}",
                this.targetType.getName(), this.props.getPageSize());

        JdbcPagingItemReaderBuilder<T> builder = new JdbcPagingItemReaderBuilder<T>()
                .name("jdbcPagingItemReader." + this.targetType.getSimpleName()) // Use targetType for naming
                .dataSource(this.dataSource)
                .queryProvider(this.queryProvider)
                .rowMapper(this.rowMapper)
                .pageSize(this.props.getPageSize());

        if (this.props.getParameterValues() != null && !this.props.getParameterValues().isEmpty()) {
            builder.parameterValues(this.props.getParameterValues());
            log.info("Reader will use parameters: {}", this.props.getParameterValues());
        }

        return builder.build();
    }
}
