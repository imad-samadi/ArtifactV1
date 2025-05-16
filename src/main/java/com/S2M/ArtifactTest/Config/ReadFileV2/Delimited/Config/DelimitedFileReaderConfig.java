package com.S2M.ArtifactTest.Config.ReadFileV2.Delimited.Config;
import com.S2M.ArtifactTest.Config.ReadFileV2.Core.AbstractFileReaderConfig;
import jakarta.validation.constraints.AssertTrue;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

/**
 * Configuration specific to reading delimited files (e.g., CSV, TSV),
 * with validation for delimiter properties.
 * Extends {@link AbstractFileReaderConfig} to include common file properties and their validations.
 *
 * @param <T> The type of the item that will be read and mapped.
 */
@Getter
@ToString(callSuper = true)
@SuperBuilder

public class DelimitedFileReaderConfig<T> extends AbstractFileReaderConfig<T> {

    /**
     * The delimiter character (or string) used to separate fields in the file.
     */
    @Builder.Default
    private final String delimiter = DelimitedLineTokenizer.DELIMITER_COMMA;

    /**
     * The character used to quote fields containing the delimiter or special characters.
     * Defaults to null (meaning no quote character processing by default, unless the
     * underlying tokenizer has its own default. Spring's DelimitedLineTokenizer
     * defaults to '"' if not explicitly set).
     * If you want to explicitly use the standard double quote, set default to:
     * DelimitedLineTokenizer.DEFAULT_QUOTE_CHARACTER
     */
    @Builder.Default
   private final Character quoteCharacter = null; // Or: DelimitedLineTokenizer.DEFAULT_QUOTE_CHARACTER;


    @AssertTrue(message = "Delimiter must be a single character when specified")
    private boolean isValidDelimiter() {
        if (getCustomLineMapper() != null) return true;
        return delimiter != null && delimiter.length() == 1;
    }

    @AssertTrue(message = "Quote character must be different from delimiter")
    private boolean isQuoteCharValid() {
        if (quoteCharacter == null || delimiter == null) return true;
        return quoteCharacter != delimiter.charAt(0);
    }
}
