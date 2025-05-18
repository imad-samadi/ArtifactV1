package com.S2M.ArtifactTest.Config.ReadDb;

import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseReaderValidationConfig {

    @Bean
    public Validator databaseReaderValidator() {
        return new DatabaseReaderValidator();
    }

    @RequiredArgsConstructor
    public static class DatabaseReaderValidator implements Validator {

        private final DatabaseReaderProperties properties;

        @Override
        public void validate(Object target) {
            DatabaseReaderProperties props = (DatabaseReaderProperties) target;

            validateSortKeys(props);
            validateColumnMappings(props);
        }

        private void validateSortKeys(DatabaseReaderProperties props) {
            Set<String> validColumns = props.getColumnMappings().values().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            props.getSortKeys().keySet().forEach(column -> {
                if (!validColumns.contains(column.toLowerCase())) {
                    throw new ValidationException("Invalid sort column: " + column);
                }
            });
        }

        private void validateColumnMappings(DatabaseReaderProperties props) {
            Class<?> targetType = props.getResolvedTargetType();
            Set<String> beanProperties = Arrays.stream(targetType.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

            props.getColumnMappings().keySet().forEach(property -> {
                if (!beanProperties.contains(property)) {
                    throw new ValidationException(
                            "Invalid property in column mappings: " + property
                    );
                }
            });
        }
    }
}
