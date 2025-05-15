package com.S2M.ArtifactTest.Config.ReadFileV2.Config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.batch.item.file.LineMapper;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;

/**
 * Abstract base configuration for all file readers.
 * This defines common file-level properties and enforces basic validation.
 *
 * @param <T> The item type to be read.
 */

@Getter
@ToString
@SuperBuilder
public abstract class AbstractFileReaderConfig<T> {

    /**
     * Path to the input resource (e.g., "classpath:data.csv", "file:/path/to/file.txt").
     * This field is mandatory.
     */
    @NotEmpty(message = "Resource path (resourcePath) cannot be null or empty.")
    protected final String resourcePath;

    /**
     * Target class for each record (e.g., MyDto.class).
     */
    protected final Class<T> itemType;

    /**
     * Names of the fields/columns in the file, used for mapping to itemType properties.
     */
    protected final String[] names;

    /**
     * Number of lines to skip at the beginning of the file (e.g., for a header row).
     * Must be a non-negative integer. Defaults to 0.
     */
    @Min(value = 0, message = "Lines to skip (linesToSkip) must be greater than or equal to 0.")
    @Builder.Default
    protected final int linesToSkip = 1;


    /**
     * When using a custom LineMapper:
     * - itemType and names are ignored
     * - File format-specific settings (like delimiter) are not used
     */
    @Builder.Default // Default to null, meaning use default logic
    protected final LineMapper<T> customLineMapper = null;


    @AssertTrue(message = "Either customLineMapper or (itemType and names) must be provided")
    protected boolean isValidConfigCombination() {
        if (customLineMapper != null) {
            return true;
        }
        return itemType != null && names != null && names.length > 0;
    }

    @AssertTrue(message = "names must not contain empty values")
    protected boolean isValidNames() {
        if (names == null) return true;
        return Arrays.stream(names).noneMatch(StringUtils::isEmpty);
    }



}
