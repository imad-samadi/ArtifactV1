package com.S2M.ArtifactTest.Config.ReadFileV2.Core.Exceptions;

public class ReaderConfigurationException extends RuntimeException{

    public ReaderConfigurationException(String message) {
        super(message);
    }

    public ReaderConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
