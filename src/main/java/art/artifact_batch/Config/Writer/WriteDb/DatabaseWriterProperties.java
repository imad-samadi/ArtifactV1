
package art.artifact_batch.Config.Writer.WriteDb;

import art.artifact_batch.Config.Enums.DatabaseType;
import art.artifact_batch.Config.Exceptions.ConfigurationException;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import java.util.*;
import java.util.stream.Collectors;
import java.beans.PropertyDescriptor;


/**
 * Configuration properties for database-based writers in Spring Batch.
 * Supports both JDBC and Spring Data Repository writers.
 */
@Getter
@Setter
@Validated
@Slf4j
@ConfigurationProperties(prefix = "batch.output.database")
public class DatabaseWriterProperties {

    // ----------------------------------
    // Common properties
    // ----------------------------------

    /**
     * DatabaseType of writer to use. Default = JDBC.
     */
    @NotNull(message = "DatabaseType must be specified.")
    private DatabaseType databaseType = DatabaseType.JDBC;

    /**
     * FQCN of DTO (for JDBC) or Entity (for REPOSITORY).
     */
    @NotBlank(message = "targetDatabaseType must be specified.")
    private String targetModel;


    /**
     * Runtime-resolved Class<?> from targetDatabaseType.
     * Transient so it is not bound directly from config.
     */
    private transient Class<?> resolvedTargetDatabaseType;


    // ----------------------------------
    // JDBC-specific properties
    // ----------------------------------

    /**
     * Optional raw SQL statement (INSERT, UPDATE, or DELETE) with named parameters (e.g., INSERT ... VALUES (:name, :value)).
     * If provided, clause-based properties will be ignored.
     */
    private String sql;

    // --- Clause-based JDBC properties (used if 'sql' is not provided) ---


    /** Optional override for table name. Derived from class name if blank. */
    private String tableName;

    /**
     * List of DTO property names whose values should be used in the SQL statement
     * (e.g., for INSERT column list, UPDATE set list).
     * The order matters for parameter mapping (if using positional parameters, though named are preferred).
     * For BeanPropertyItemSqlParameterSourceProvider, names must match DTO property names.
     */
    private List<String> fieldNames;

    /**
     * Map of DTO property name to DB column name.
     * Used to map fieldNames (DTO properties) to actual column names in the generated SQL.
     * Defaults to snake_case if no mapping provided for a fieldName.
     */
    private Map<String, String> columnMappings = new HashMap<>();


    // ----------------------------------
    // Repository-specific properties
    // ----------------------------------

    /**
     * Spring bean name of the Repository to invoke.
     */
    private String serviceBeanName;


    // ----------------------------------
    // Initialization logic
    // ----------------------------------

    @PostConstruct
    public void initialize() {
        // Resolve the targetDatabaseType String into a Class object
        try {
            this.resolvedTargetDatabaseType = Class.forName(targetModel);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Invalid target class for writer: " + targetModel, e);
        }

        if (DatabaseType.JDBC.equals(this.databaseType)) {
            log.info("JDBC Writer configured for target DatabaseType: {}", targetModel);

            if (!StringUtils.hasText(this.sql)) {
                String actualTableName = getActualTableName();
                if (actualTableName == null) {
                    throw new ConfigurationException("JDBC Writer requires 'tableName' property or resolvable 'targetDatabaseType' to derive table name in clause-based mode.");
                }

                if (CollectionUtils.isEmpty(this.fieldNames)) {
                    this.fieldNames = deriveFieldNamesFromTargetDatabaseTypeProperties();
                    if (CollectionUtils.isEmpty(this.fieldNames)) {
                        throw new ConfigurationException("JDBC Writer Could not derive 'fieldNames' from targetDatabaseType readable properties and 'fieldNames' property is empty.");
                    }
                    log.info("JDBC Writer: Derived fieldNames: {}", this.fieldNames);
                }
            } else {
                // Raw SQL mode validation (minimal)
                log.info("JDBC Writer using raw SQL from properties. Clause-based properties will be ignored.");
                if (!StringUtils.hasText(this.sql)) { // Re-validation pour être sûr
                    throw new ConfigurationException("JDBC Writer requires 'sql' property to be set if not using clause-based properties.");
                }
            }

        } else if (DatabaseType.REPOSITORY.equals(this.databaseType)) {
            log.info("REPOSITORY Writer configured for entity: {}, repository bean: '{}'", targetModel, serviceBeanName);
        }
    }

    // ----------------------------------
    // JDBC helper methods for clause-based mode
    // ----------------------------------

    /**
     * Get the effective table name: explicit property or derived from class.
     */
    public String getActualTableName() {
        if (!DatabaseType.JDBC.equals(databaseType) || StringUtils.hasText(this.sql)) {
            return null;
        }
        if (StringUtils.hasText(tableName)) {
            return tableName;
        }
        if (resolvedTargetDatabaseType == null) {
            throw new IllegalStateException("JDBC Writer: targetDatabaseType not resolved to derive table name.");
        }
        return NamingStrategy.DEFAULT.toTableName(resolvedTargetDatabaseType.getSimpleName());
    }

    /**
     * Get the map of DTO property name to DB column name, using explicit mappings or defaults.
     * Applicable in JDBC clause-based mode for INSERT/UPDATE.
     */
    public Map<String, String> getActualColumnMappings() {
        if (!DatabaseType.JDBC.equals(databaseType) || StringUtils.hasText(this.sql) || CollectionUtils.isEmpty(this.fieldNames)) {
            return Collections.emptyMap();
        }

        Map<String, String> actualMappings = new HashMap<>();
        for (String fieldName : fieldNames) {
            String columnName = columnMappings.getOrDefault(
                    fieldName,
                    NamingStrategy.DEFAULT.toColumnName(fieldName)
            );
            actualMappings.put(fieldName, columnName);
        }
        return actualMappings;
    }



    // ----------------------------------
    // Repository helper methods
    // ----------------------------------

    /**
     * Validate repository configuration: bean and method must be set.
     */
    @AssertTrue(message = "When DatabaseType is REPOSITORY, serviceBeanName must be set.")
    public boolean isRepositoryConfigValid() {
        if (DatabaseType.REPOSITORY.equals(databaseType)) {
            return StringUtils.hasText(serviceBeanName);
        }
        return true;
    }

    private List<String> deriveFieldNamesFromTargetDatabaseTypeProperties() {
        if (resolvedTargetDatabaseType == null) {
            throw new IllegalStateException("targetDatabaseType must be resolved before deriving field names.");
        }
        return Arrays.stream(BeanUtils.getPropertyDescriptors(resolvedTargetDatabaseType))
                .filter(pd -> pd.getReadMethod() != null && !"class".equals(pd.getName()))
                .map(PropertyDescriptor::getName)
                .collect(Collectors.toList());
    }


}