package art.artifact_batch.Config.Writer.WriteDb.Providers;

import art.artifact_batch.Config.Exceptions.ConfigurationException;
import art.artifact_batch.Config.Writer.WriteDb.DatabaseWriterProperties;
import art.artifact_batch.Config.Writer.WriteDb.SPI.DatabaseWriterProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides a JDBC Batch ItemWriter configured using properties.
 * Assumes named parameters in the SQL match the property names of the item type T.
 *
 * @param <T> The type of item being written.
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultDatabaseWriterProvider<T> implements DatabaseWriterProvider<T> {

    private final DataSource dataSource;
    private final DatabaseWriterProperties props;
    private final ItemSqlParameterSourceProvider<T> parameterSourceProvider;
    private final Class<T> targetType;

    @Override
    public ItemWriter<T> createWriter() throws Exception {
        String sql;
        // === LOGIQUE DE CONSTRUCTION SQL ===
        if (StringUtils.hasText(this.props.getSql())) {
            sql = this.props.getSql();
            log.info("JDBC Writer using raw SQL: [{}]", sql);
        } else {
            String tableName = this.props.getActualTableName();
            Map<String, String> columnMappings = this.props.getActualColumnMappings();
            List<String> fieldNames = this.props.getFieldNames();
            if (tableName == null) {
                throw new ConfigurationException("JDBC Writer: Cannot determine table name from properties or targetType.");
            }
            StringBuilder sqlBuilder = new StringBuilder();

                if (CollectionUtils.isEmpty(fieldNames)) {
                    throw new ConfigurationException("JDBC Writer: Cannot build INSERT SQL. 'fieldNames' is empty.");
                }
                // INSERT INTO table_name (column1, column2, ...) VALUES (:propName1, :propName2, ...)
                sqlBuilder.append("INSERT INTO ").append(tableName).append(" (");
                // Utilise les noms de colonnes (les valeurs de la map de mapping réelle)
                sqlBuilder.append(columnMappings.values().stream().collect(Collectors.joining(", ")));
                sqlBuilder.append(") VALUES (");
                // Utilise les noms de paramètres nommés qui correspondent aux noms de PROPRIETES DTO (les clés de la map de mapping réelle)
                sqlBuilder.append(columnMappings.keySet().stream().map(key -> ":" + key).collect(Collectors.joining(", ")));
                sqlBuilder.append(")");
                sql = sqlBuilder.toString();
                log.info("JDBC Writer built SQL: [{}]", sql);
        }
        // === FIN LOGIQUE DE CONSTRUCTION SQL ===
        JdbcBatchItemWriterBuilder<T> builder = new JdbcBatchItemWriterBuilder<T>()
                .dataSource(this.dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(this.parameterSourceProvider);
        return builder.build();
    }
}