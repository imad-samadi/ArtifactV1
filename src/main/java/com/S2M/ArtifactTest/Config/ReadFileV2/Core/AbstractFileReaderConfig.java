package com.S2M.ArtifactTest.Config.ReadFileV2.Core;

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

    @NotEmpty(message = "Resource path (resourcePath) cannot be null or empty.")
    protected final String resourcePath;

    /**
     * Target class for records. Essential if a default FieldSetMapper strategy is employed
     * that relies on this type, like BeanWrapperFieldSetMapper).
     */
    protected final Class<T> itemType;

    /**
     * Field names. Essential if a default Tokenizer strategy is employed
     */
    protected final String[] names;

    @Min(value = 0, message = "Lines to skip (linesToSkip) must be greater than or equal to 0.")
    @Builder.Default
    protected final int linesToSkip = 0;

    @Builder.Default
    protected final LineMapper<T> customLineMapper = null;

    @AssertTrue(message = "'itemType' must be provided when a customLineMapper is not used, as it's generally needed for default mapping strategies.")
    private boolean isItemTypeAvailableForDefaultProcessing() {
        if (getCustomLineMapper() != null) {
            return true;
        }
        return getItemType() != null;
    }

    // Note: Validation for 'names' being present for default tokenizers
    // will now reside within the default TokenizerProvider implementations.

    @AssertTrue(message = "If 'names' array is provided, its elements must not be null or empty strings.")
    protected boolean areNamesElementsValidIfProvided() {
        if (getNames() == null) {
            return true;
        }
        for (String name : getNames()) {
            if (name == null || name.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }


}
