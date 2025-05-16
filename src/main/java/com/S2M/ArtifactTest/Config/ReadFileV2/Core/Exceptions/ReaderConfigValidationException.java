package com.S2M.ArtifactTest.Config.ReadFileV2.Core.Exceptions;

import java.util.Collections;
import java.util.List;

public class ReaderConfigValidationException extends ReaderConfigurationException{

    private final List<String> validationErrors;

    public ReaderConfigValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public List<String> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }

   //This method is from class Throwable
    @Override
    public String getMessage() {
        return super.getMessage() + "\nValidation errors:\n- " +
                String.join("\n- ", validationErrors);
    }
}
