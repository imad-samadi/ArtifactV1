package com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions;

/**
 * Custom runtime exception to indicate errors related to
 * application configuration, particularly for the file reading .
 */
public class ConfigurationException extends RuntimeException {

    /**
     * Constructs a new ConfigurationException with the specified detail message.
     * @param message the detail message.
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause of the exception.
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
