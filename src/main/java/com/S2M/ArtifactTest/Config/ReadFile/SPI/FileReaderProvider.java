package com.S2M.ArtifactTest.Config.ReadFile.SPI;

import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader; // Needs to be accessible
import org.springframework.util.Assert;

import java.io.IOException;
public interface FileReaderProvider {



    /**
     * Creates an ItemReader.
     * @param props The configuration properties.
     * @param targetType The class to which records will be mapped.
     * @param <T> The type of the target object.
     * @return A configured ItemReader.
     */
    <T> ItemReader<T> createReader(ReadProperties props, Class<T> targetType);


    /**
     * Default helper method to resolve and validate a resource.
     *
     * @param resourceLoader The ResourceLoader to use for resolving the path.
     * @param filePath       The path to the file resource.
     * @return The resolved and validated Resource.
     * @throws ConfigurationException if the resource does not exist or cannot be accessed.
     */
    default Resource resolveAndValidateResource(ResourceLoader resourceLoader, String filePath) { // providerName removed


        Resource resource = resourceLoader.getResource(filePath);

        if (!resource.exists()) {
            String resolvedPathInfo = "";
            try {
                if (resource.isFile() && resource.getFile() != null) {
                    resolvedPathInfo = ". Resolved absolute path: " + resource.getFile().getAbsolutePath();
                } else if (resource.getURI() != null) {
                    resolvedPathInfo = ". Resolved URI: " + resource.getURI().toString();
                }
            } catch (IOException ignored) { /* Ignored */ }

            throw new ConfigurationException(
                    "Input file not found at path: " + filePath + resolvedPathInfo

            );
        }
        return resource;
    }
}
