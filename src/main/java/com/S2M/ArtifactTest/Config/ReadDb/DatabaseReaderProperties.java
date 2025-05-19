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

@Getter
@Setter
@Validated
@Slf4j
@ConfigurationProperties(prefix = "batch.input.database")
public class DatabaseReaderProperties {

    public enum ReaderType {
        JDBC,
        REPOSITORY
    }

    @NotNull(message = "readerType must be specified.")
    private ReaderType readerType = ReaderType.JDBC;

    @NotBlank(message = "targetType (DTO class for JDBC, Entity class for REPOSITORY) must be specified.")
    private String targetType;

    @Min(value = 1, message = "pageSize must be at least 1.")
    private int pageSize = 50; // Common to both

    private transient Class<?> resolvedTargetType;

    // --- JDBC Specific Properties ---
    private String tableName;
    private String selectClause;
    private String whereClause;
    private Map<String, String> columnMappings = new HashMap<>(); // DTO field to DB column for JDBC
    private Map<String, Sort.Direction> sortKeys = new LinkedHashMap<>(); // DB column to Sort.Direction for JDBC

    // --- REPOSITORY Specific Properties ---
    // serviceBeanName will be used as repositoryBeanName for REPOSITORY type
    private String serviceBeanName; // Bean name of the Spring Data Repository
    // serviceMethodName will be used as repositoryMethodName for REPOSITORY type
    private String serviceMethodName; // Method name in the repository

    // Arguments for the repository method (excluding Pageable, which RepositoryItemReader handles)
    // Order in this list is important and must match the method signature.
    private List<Object> repositoryMethodArguments = new ArrayList<>();

    // Sort for REPOSITORY type. List of strings like "property,DIRECTION", e.g., "lastName,ASC"
    private List<String> repositorySorts = new ArrayList<>();


    // --- Common but handled differently ---
    // For JDBC: used for named parameters in whereClause.
    // For REPOSITORY: if repositoryMethodArguments is empty, values from this map *might* be used (fragile due to order).
    // Prefer repositoryMethodArguments for REPOSITORY.
    private Map<String, Object> parameterValues = new HashMap<>();


    // --- Initialization ---
    @PostConstruct
    public void initialize() {
        try {
            this.resolvedTargetType = Class.forName(targetType);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Could not resolve targetType: " + targetType, e);
        }

        if (ReaderType.JDBC.equals(this.readerType)) {
            if (this.sortKeys == null) this.sortKeys = new LinkedHashMap<>();
            if (this.columnMappings == null) this.columnMappings = new HashMap<>();
            if (StringUtils.hasText(this.whereClause) && containsNamedParameters(this.whereClause) &&
                    (this.parameterValues == null || this.parameterValues.isEmpty())) {
                log.warn("JDBC Reader: 'whereClause' may require parameters, but 'parameterValues' is empty. Where: {}", this.whereClause);
            }
            log.info("JDBC Reader configured for target DTO: {}", targetType);
        } else if (ReaderType.REPOSITORY.equals(this.readerType)) {
            // serviceBeanName and serviceMethodName are validated by @AssertTrue
            log.info("REPOSITORY Reader configured for entity: {}, repository bean (serviceBeanName): '{}', method (serviceMethodName): '{}'",
                    targetType, serviceBeanName, serviceMethodName);
            if (this.repositoryMethodArguments == null) this.repositoryMethodArguments = new ArrayList<>();
            if (this.repositorySorts == null) this.repositorySorts = new ArrayList<>();

            if (!this.repositoryMethodArguments.isEmpty()) {
                log.info("Repository method arguments: {}", this.repositoryMethodArguments);
            } else if (this.parameterValues != null && !this.parameterValues.isEmpty()){
                log.warn("REPOSITORY Reader: 'parameterValues' is set, but 'repositoryMethodArguments' is empty. " +
                        "Using values from 'parameterValues' for repository method arguments is order-dependent and fragile. " +
                        "Prefer using 'repositoryMethodArguments' list for explicit argument order.");
            }
            if (!this.repositorySorts.isEmpty()) {
                log.info("Repository sorts: {}", this.repositorySorts);
            }
        }
        if (this.parameterValues == null) this.parameterValues = new HashMap<>(); // Ensure not null for general use
    }

    // --- Validations ---

    // JDBC Validations (using groups or conditional @AssertTrue)
    @AssertTrue(message = "For JDBC reader, tableName must be provided if selectClause is not a full query.")
    public boolean isTableNameValidForJdbc() {
        if (ReaderType.JDBC.equals(readerType)) {
            // If selectClause is a full query (e.g. "SELECT * FROM foo WHERE ..."), tableName might not be needed by user.
            // This validation is a bit simplistic. A more robust check might see if selectClause contains "FROM ".
            // For now, if selectClause is simple (just columns), tableName is needed.
            boolean selectIsSimple = StringUtils.hasText(selectClause) && !selectClause.toLowerCase().contains(" from ");
            return StringUtils.hasText(tableName) || !selectIsSimple;
        }
        return true; // Pass for non-JDBC
    }

    @AssertTrue(message = "When readerType is REPOSITORY, serviceBeanName (repository bean name) and serviceMethodName (repository method name) must be set.")
    public boolean isRepositoryConfigValid() {
        if (ReaderType.REPOSITORY.equals(readerType)) {
            return StringUtils.hasText(serviceBeanName) && StringUtils.hasText(serviceMethodName);
        }
        return true; // Pass for non-REPOSITORY
    }


    // --- Helper Methods ---

    private boolean containsNamedParameters(String clause) {
        // This helper is JDBC specific for whereClause
        if (ReaderType.JDBC.equals(readerType)) {
            return clause != null && clause.matches(".*:[a-zA-Z_][a-zA-Z0-9_]*.*");
        }
        return false;
    }

    // JDBC Specific: if table Name not provided get it from the class
    public String getActualTableName() {
        if (!ReaderType.JDBC.equals(readerType)) {
            // log.warn("getActualTableName() called for non-JDBC type. Returning null.");
            return null; // Or throw new IllegalStateException("Not applicable for readerType: " + readerType);
        }
        if (StringUtils.hasText(tableName)) return tableName;
        if (resolvedTargetType == null) throw new IllegalStateException("JDBC: targetType not resolved to derive table name.");
        return NamingStrategy.DEFAULT.toTableName(resolvedTargetType.getSimpleName());
    }

    // JDBC Specific: if select * not provided
    public String getActualSelectClause() {
        if (!ReaderType.JDBC.equals(readerType)) {
            // log.warn("getActualSelectClause() called for non-JDBC type. Returning null.");
            return null;
        }
        if (StringUtils.hasText(selectClause)) return selectClause;
        if (resolvedTargetType == null) throw new IllegalStateException("JDBC: targetType not resolved to derive select clause.");

        List<String> columnNames = Arrays.stream(BeanUtils.getPropertyDescriptors(resolvedTargetType))
                .filter(pd -> pd.getReadMethod() != null && !"class".equals(pd.getName()))
                .map(pd -> columnMappings.getOrDefault(pd.getName(), NamingStrategy.DEFAULT.toColumnName(pd.getName())))
                .collect(Collectors.toList());
        if (columnNames.isEmpty()) throw new ConfigurationException("JDBC: Could not derive select clause for DTO " + targetType);
        return String.join(", ", columnNames);
    }

    // JDBC Specific:
    public Map<String, org.springframework.batch.item.database.Order> getActualSortOrders() {
        if (!ReaderType.JDBC.equals(readerType)) {
            // log.warn("getActualSortOrders() called for non-JDBC type. Returning empty map.");
            return Collections.emptyMap();
        }
        if (resolvedTargetType == null) throw new IllegalStateException("JDBC: targetType not resolved for deriving sort orders.");
        Map<String, org.springframework.batch.item.database.Order> batchOrders = new LinkedHashMap<>();

        if (sortKeys != null && !sortKeys.isEmpty()) {
            sortKeys.forEach((col, dir) -> batchOrders.put(col, dir == Sort.Direction.DESC ?
                    org.springframework.batch.item.database.Order.DESCENDING :
                    org.springframework.batch.item.database.Order.ASCENDING));
        } else {
            log.warn("JDBC: No 'sortKeys' provided for {}. Attempting default sort (by 'id' or first property ASC).", targetType);
            findDefaultSortPropertyForJdbc().ifPresentOrElse(
                    pd -> {
                        String defaultSortColumn = columnMappings.getOrDefault(pd.getName(), NamingStrategy.DEFAULT.toColumnName(pd.getName()));
                        batchOrders.put(defaultSortColumn, org.springframework.batch.item.database.Order.ASCENDING);
                        log.info("JDBC: Defaulting sort key to: '{} ASC'", defaultSortColumn);
                    },
                    () -> log.warn("JDBC: No 'sortKeys' provided and no default could be derived for DTO {}. Reader will be unsorted or rely on DB default.", targetType)
                    // Not throwing exception here to allow unsorted reads if user intends it
            );
        }
        return batchOrders;
    }

    // JDBC Specific:
    private Optional<PropertyDescriptor> findDefaultSortPropertyForJdbc() {
        if (!ReaderType.JDBC.equals(readerType) || resolvedTargetType == null) {
            return Optional.empty();
        }
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(resolvedTargetType);
        return Arrays.stream(pds)
                .filter(pd -> "id".equalsIgnoreCase(pd.getName()) && pd.getReadMethod() != null && !"class".equals(pd.getName()))
                .findFirst()
                .or(() -> Arrays.stream(pds)
                        .filter(pd -> pd.getReadMethod() != null && !"class".equals(pd.getName()))
                        .findFirst());
    }

    // REPOSITORY Specific: Helper to parse repositorySorts into Spring Data Sort object
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
}
