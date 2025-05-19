package com.S2M.ArtifactTest.Config.ReadDb;

import com.S2M.ArtifactTest.Config.ReadDb.NamingStrategy;
import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central configuration class for database reader properties.
 * Maps to 'batch.input.database' prefix in application.yml
 * Handles both required and optional properties with validation
 */
@Getter
@Setter
@Validated
@Slf4j
@ConfigurationProperties(prefix = "batch.input.database")
public class DatabaseReaderProperties {

    @NotBlank(message = "targetType must be specified for database reader.")
    private String targetType;

    // Optional: User-provided values take precedence over derived values.
    private String tableName;
    private String selectClause;
    private String whereClause;
    private Map<String, Object> parameterValues = new HashMap<>();
    private Map<String, String> columnMappings = new HashMap<>();

    @NotNull(message = "sortKeys map cannot be null, but can be empty to attempt default derivation.")
    private Map<String, Sort.Direction> sortKeys = new LinkedHashMap<>();

    @Min(value = 1, message = "pageSize must be at least 1.")
    private int pageSize = 50;

    private transient Class<?> resolvedTargetType;

    @PostConstruct
    public void initialize() {
        try {
            this.resolvedTargetType = Class.forName(targetType);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Could not resolve database reader targetType: " + targetType, e);
        }

        if (this.sortKeys == null) this.sortKeys = new LinkedHashMap<>();
        if (this.parameterValues == null) this.parameterValues = new HashMap<>();
        if (this.columnMappings == null) this.columnMappings = new HashMap<>();

        if (StringUtils.hasText(this.whereClause) && containsNamedParameters(this.whereClause) && CollectionUtils.isEmpty(this.parameterValues)) {
            log.warn("The 'whereClause' may require parameters, but 'parameterValues' is empty. Where: {}", this.whereClause);
        }
    }

    private boolean containsNamedParameters(String clause) {
        return clause != null && clause.matches(".*:[a-zA-Z_][a-zA-Z0-9_]*.*");
    }


    //if table Name not provided get it from the class
    public String getActualTableName() {
        if (StringUtils.hasText(tableName)) return tableName;
        if (resolvedTargetType == null) throw new IllegalStateException("targetType not resolved.");
        return NamingStrategy.DEFAULT.toTableName(resolvedTargetType.getSimpleName());
    }

    //if select * not provided
    public String getActualSelectClause() {
        if (StringUtils.hasText(selectClause)) return selectClause;
        if (resolvedTargetType == null) throw new IllegalStateException("targetType not resolved.");

        List<String> columnNames = Arrays.stream(BeanUtils.getPropertyDescriptors(resolvedTargetType))
                .filter(pd -> pd.getReadMethod() != null && !"class".equals(pd.getName()))
                .map(pd -> columnMappings.getOrDefault(pd.getName(), NamingStrategy.DEFAULT.toColumnName(pd.getName())))
                .collect(Collectors.toList());
        if (columnNames.isEmpty()) throw new ConfigurationException("Could not derive select clause for " + targetType);
        return String.join(", ", columnNames);
    }






    public Map<String, org.springframework.batch.item.database.Order> getActualSortOrders() {
        if (resolvedTargetType == null) throw new IllegalStateException("targetType not resolved.");
        Map<String, org.springframework.batch.item.database.Order> batchOrders = new LinkedHashMap<>();

        if (!this.sortKeys.isEmpty()) {
            this.sortKeys.forEach((col, dir) -> batchOrders.put(col, dir == Sort.Direction.DESC ?
                    org.springframework.batch.item.database.Order.DESCENDING :
                    org.springframework.batch.item.database.Order.ASCENDING));
        } else { // Attempt to derive default sort key
            log.warn("No 'sortKeys' provided for {}. Attempting default sort (by 'id' or first property ASC).", targetType);
            findDefaultSortProperty().ifPresentOrElse(
                    pd -> {
                        String defaultSortColumn = columnMappings.getOrDefault(pd.getName(), NamingStrategy.DEFAULT.toColumnName(pd.getName()));
                        batchOrders.put(defaultSortColumn, org.springframework.batch.item.database.Order.ASCENDING);
                        log.info("Defaulting sort key to: '{} ASC'", defaultSortColumn);
                    },
                    () -> { throw new ConfigurationException("No 'sortKeys' provided and no default could be derived for " + targetType); }
            );
        }
        return batchOrders;
    }

    private Optional<PropertyDescriptor> findDefaultSortProperty() {
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(resolvedTargetType);
        return Arrays.stream(pds) // Try 'id' or 'ID'
                .filter(pd -> "id".equalsIgnoreCase(pd.getName()) && pd.getReadMethod() != null && !"class".equals(pd.getName()))
                .findFirst()
                .or(() -> Arrays.stream(pds) // Else, first readable property
                        .filter(pd -> pd.getReadMethod() != null && !"class".equals(pd.getName()))
                        .findFirst());
    }
}
