package com.S2M.ArtifactTest.Config.ReadFileV2.Core.Validation;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.AbstractFileReaderConfig;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.Exceptions.ReaderConfigValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator; // Jakarta Validator
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component // Make this a Spring-managed service
@RequiredArgsConstructor
public class ConfigValidator {

    private final Validator jakartaValidator; // Injected standard Jakarta Validator

    /**
     * Validates the provided AbstractFileReaderConfig instance.
     * Throws a ReaderConfigValidationException if validation violations are found.
     *
     * @param config The configuration object to validate.
     * @param <T>    The type parameter of the configuration.
     * @throws ReaderConfigValidationException if validation fails.
     * @throws IllegalArgumentException if the config itself is null.
     */
    public <T> void validate(AbstractFileReaderConfig<T> config) {
        if (config == null) {

            throw new IllegalArgumentException("Configuration object to validate cannot be null.");
        }


        @SuppressWarnings({"unchecked", "rawtypes"})
        Set<ConstraintViolation<AbstractFileReaderConfig<T>>> violations =
                (Set) jakartaValidator.validate(config);

        if (!violations.isEmpty()) {
            List<String> formattedErrors = violations.stream()
                    .map(this::formatViolation)
                    .collect(Collectors.toList());
            throw new ReaderConfigValidationException(
                    config.getClass().getSimpleName() + " validation failed",
                    formattedErrors
            );
        }
    }

    private String formatViolation(ConstraintViolation<?> violation) {

        return String.format("Field/Constraint '%s': %s (invalid value: [%s])",
                violation.getPropertyPath(),    // e.g., "resourcePath" or "namesRequiredForDefaultProcessing"
                violation.getMessage(),
                violation.getInvalidValue());
    }
}
