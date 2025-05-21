package com.S2M.ArtifactTest.Config.ReadDb;

import com.S2M.ArtifactTest.Config.ReadDb.NamingStrategy;
import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Sort; // Keep Spring Data Sort
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Configuration properties for database-based readers in Spring Batch.
 * Supports both JDBC and Spring Data Repository readers.
 */
@Getter
@Setter
@Validated
@Slf4j
@ConfigurationProperties(prefix = "batch.input.database")
public class DatabaseReaderProperties {

    /**
     * Supported reader types: JDBC or Spring Data REPOSITORY.
     */
    public enum ReaderType {
        JDBC,
        REPOSITORY
    }

    // ----------------------------------
    // Common properties
    // ----------------------------------

    /**
     * Type of reader to use. Default = JDBC.
     */
    @NotNull(message = "readerType must be specified.")
    private ReaderType readerType = ReaderType.JDBC;

    /**
     * FQCN of DTO (for JDBC) or Entity (for REPOSITORY).
     */
    @NotBlank(message = "targetType (DTO class for JDBC, Entity class for REPOSITORY) must be specified.")
    private String targetType;

    /**
     * Number of items per page/chunk.
     */
    @Min(value = 1, message = "pageSize must be at least 1.")
    private int pageSize = 50;

    /**
     * Runtime-resolved Class<?> from targetType.
     * Transient so it is not bound directly from config.
     */
    private transient Class<?> resolvedTargetType;

    // ----------------------------------
    // JDBC-specific properties
    // ----------------------------------

    /** Optional override for table name; will be derived from class name if blank. */
    private String tableName;

    /**
     * Custom SELECT clause (columns) or full query. Derived automatically if blank.
     */
    private String selectClause;

    /**
     * Optional WHERE clause (without "WHERE" keyword). Supports :named parameters.
     */
    private String whereClause;

    /**
     * Map of DTO field name to DB column name for SELECT and filters.
     */
    private Map<String, String> columnMappings = new HashMap<>();

    /**
     * Map of DB column name to sort direction for paging. Required for reliable pagination.
     */
    private Map<String, Sort.Direction> sortKeys = new LinkedHashMap<>();

    /**
     * Values for named parameters used in whereClause.
     */
    private Map<String, Object> parameterValues = new HashMap<>();

    // ----------------------------------
    // Repository-specific properties
    // ----------------------------------

    /**
     * Spring bean name of the Repository to invoke.
     */
    private String serviceBeanName;

    /**
     * Method name on the repository bean to call (excluding pageable argument).
     */
    private String serviceMethodName;

    /**
     * Arguments to pass to the repository method, in order (excluding Pageable).
     */
    private List<Object> repositoryMethodArguments = new ArrayList<>();

    /**
     * List of strings for sorting in repository mode, e.g. "name,DESC".
     */
    private List<String> repositorySorts = new ArrayList<>();





    // ----------------------------------
    // Initialization logic
    // ----------------------------------

    @PostConstruct
    public void initialize() {
        // Resolve the targetType String into a Class object
        try {
            this.resolvedTargetType = Class.forName(targetType);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Invalid target class: " + targetType, e);
        }

        if (ReaderType.JDBC.equals(this.readerType)) {
            // Warn if WHERE clause has named params but no values provided
            if (StringUtils.hasText(this.whereClause)
                    && containsNamedParameters(this.whereClause)
                    && CollectionUtils.isEmpty(this.parameterValues)) {
                log.warn("JDBC Reader: 'whereClause' may require parameters, but 'parameterValues' is empty. Where: {}", this.whereClause);
            }
            log.info("JDBC Reader configured for target DTO: {}", targetType);

        } else if (ReaderType.REPOSITORY.equals(this.readerType)) {

            // Log details or warnings about parameter usage
            log.info("REPOSITORY Reader configured for entity: {}, repository bean: '{}', method: '{}'", targetType, serviceBeanName, serviceMethodName);

            if (!this.repositoryMethodArguments.isEmpty()) {
                log.info("Repository method arguments: {}", this.repositoryMethodArguments);
            } else if (!CollectionUtils.isEmpty(this.parameterValues)) {
                log.warn("REPOSITORY Reader: 'parameterValues' is set but 'repositoryMethodArguments' list is empty. " +
                        "Order-dependent fallback in use; consider using explicit 'repositoryMethodArguments'.");
            }
            if (!this.repositorySorts.isEmpty()) {
                log.info("Repository sorts: {}", this.repositorySorts);
            }
        }
    }





// ----------------------------------
    // JDBC helper methods
    // ----------------------------------

    /**
     * Get the effective table name: explicit or derived from class.
     */
    public String getActualTableName() {
        if (!ReaderType.JDBC.equals(readerType)) {
            return null;
        }
        if (StringUtils.hasText(tableName)) {
            return tableName;  // User provided override
        }
        // Fallback: convert class name to snake_case table name
        if (resolvedTargetType == null) {
            throw new IllegalStateException("JDBC: targetType not resolved to derive table name.");
        }
        return NamingStrategy.DEFAULT.toTableName(resolvedTargetType.getSimpleName());
    }


    /**
     * Get the SELECT clause: explicit or derived from DTO fields.
     */
    public String getActualSelectClause() {
        if (!ReaderType.JDBC.equals(readerType)) {
            return null;
        }
        if (StringUtils.hasText(selectClause)) {
            return selectClause;  // User provided override
        }
        if (resolvedTargetType == null) {
            throw new IllegalStateException("JDBC: targetType not resolved to derive select clause.");
        }
        List<String> columnNames = Arrays.stream(BeanUtils.getPropertyDescriptors(resolvedTargetType))
                .filter(pd -> pd.getReadMethod() != null && !"class".equals(pd.getName()))
                .map(pd -> columnMappings.getOrDefault(
                        pd.getName(),
                        NamingStrategy.DEFAULT.toColumnName(pd.getName())
                ))
                .collect(Collectors.toList());
        if (columnNames.isEmpty()) {
            throw new ConfigurationException("JDBC: Could not derive select clause for DTO " + targetType);
        }
        return String.join(", ", columnNames);
    }



    /**
     * Get the map of column → batch Order (ASC/DESC) for paging queries.
     * Uses user-defined sortKeys or defaults to 'id' or first field ascending.
     */
    public Map<String, org.springframework.batch.item.database.Order> getActualSortOrders() {
        if (!ReaderType.JDBC.equals(readerType)) {
            return Collections.emptyMap();
        }
        if (resolvedTargetType == null) {
            throw new IllegalStateException("JDBC: targetType not resolved for deriving sort orders.");
        }
        Map<String, org.springframework.batch.item.database.Order> batchOrders = new LinkedHashMap<>();

        if (!sortKeys.isEmpty()) {
            sortKeys.forEach((col, dir) -> batchOrders.put(
                    col,
                    dir == Sort.Direction.DESC
                            ? org.springframework.batch.item.database.Order.DESCENDING
                            : org.springframework.batch.item.database.Order.ASCENDING
            ));
        } else {
            log.info("JDBC: No 'sortKeys' provided for {}. Attempting default sort.", targetType);
            findDefaultSortPropertyForJdbc().ifPresentOrElse(pd -> {
                String defaultCol = columnMappings.getOrDefault(
                        pd.getName(), NamingStrategy.DEFAULT.toColumnName(pd.getName()));
                batchOrders.put(defaultCol, org.springframework.batch.item.database.Order.ASCENDING);
                log.info("JDBC: Defaulting sort key to: '{} ASC'", defaultCol);
            }, () -> {
                log.warn("JDBC: No default sort property found; reader will be unsorted or rely on DB default.");
            });
        }
        return batchOrders;
    }

    /**
     * Find default property for sorting: 'id' if present, otherwise first readable property.
     */
    private Optional<PropertyDescriptor> findDefaultSortPropertyForJdbc() {
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(resolvedTargetType);
        return Arrays.stream(pds)
                .filter(pd -> pd.getReadMethod() != null && !"class".equals(pd.getName()))
                .filter(pd -> "id".equalsIgnoreCase(pd.getName()))
                .findFirst()
                .or(() -> Arrays.stream(pds)
                        .filter(pd -> pd.getReadMethod() != null && !"class".equals(pd.getName()))
                        .findFirst());
    }





    // ----------------------------------
    // Repository helper methods
    // ----------------------------------

    /**
     * Validate repository configuration: bean and method must be set.
     */
    @AssertTrue(message = "When readerType is REPOSITORY, serviceBeanName and serviceMethodName must be set.")
    public boolean isRepositoryConfigValid() {
        if (ReaderType.REPOSITORY.equals(readerType)) {
            return StringUtils.hasText(serviceBeanName)
                    && StringUtils.hasText(serviceMethodName);
        }
        return true;
    }

    /**
     * Convert repositorySorts (e.g. "name,DESC") into Spring Data Sort.
     * Given:
         * repositorySorts:
         *   - "lastName,DESC"
         *   - "createdAt"      # no direction => ASC
         *   - ""               # ignored
         *   - "age,DOWN"       # invalid direction => warning, treated as ASC
     * This method produces:
         * Sort.by(
         *   Sort.Order.desc("lastName"),
         *   Sort.Order.asc("createdAt"),
         *   Sort.Order.asc("age")    // with a warning logged
         * );
     */
    public Sort getRepositorySpringSort() {
        if (!ReaderType.REPOSITORY.equals(readerType) || repositorySorts == null || repositorySorts.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String sortProperty : repositorySorts) {
            if (!StringUtils.hasText(sortProperty)) continue;
            String[] parts = sortProperty.split(",");
            if (parts.length == 0 || !StringUtils.hasText(parts[0])) continue;

            String propertyName = parts[0].trim();
            Sort.Direction direction = Sort.Direction.ASC; // Default
            if (parts.length > 1 && StringUtils.hasText(parts[1])) {
                try {
                    direction = Sort.Direction.fromString(parts[1].trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("REPOSITORY: Invalid sort direction '{}' for property '{}'. Defaulting to ASC.", parts[1], propertyName);
                }
            }
            orders.add(new Sort.Order(direction, propertyName));
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }






    // ----------------------------------
    // Internal helper methods
    // ----------------------------------

    /**
     * Detects presence of :named parameters in the whereClause.
     */
    private boolean containsNamedParameters(String clause) {
        if (!ReaderType.JDBC.equals(readerType) || clause == null) return false;
        return clause.matches(".*:[a-zA-Z_][a-zA-Z0-9_]*.*");
    }
}
