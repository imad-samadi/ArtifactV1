package art.artifact_batch.Config.Reader.ReadFile;

import art.artifact_batch.Config.Enums.FileType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Configuration properties for reading flat files.
 * This class is populated from application.yml or application.properties
 * under the prefix "batch.input".
 * It uses Jakarta Bean Validation (JSR 380) annotations for validating the properties.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Validated
@ConfigurationProperties(prefix = "batch.input.file")
public class ReadProperties {

    

    @NotEmpty(message = "File path (batch.input.filePath) must be provided and cannot be empty.")
    private String filePath;


    @NotNull(message = "File FileType (batch.input.FileType) must be specified.")
    private FileType fileType = FileType.DELIMITED;


    private String delimiter = ",";


    private Character quoteCharacter;


    private String[] names ;


    @Pattern(regexp = "^$|^\\d+(-\\d+)?(,\\s*\\d+(-\\d+)?)*$",
            message = "Column ranges (batch.input.columnRanges) format is invalid. Expected e.g., '1-5,6-10,11' or empty.")
    private String columnRanges;


    @NotNull(message = "Encoding (batch.input.encoding) must be specified.")
    private String encoding = StandardCharsets.UTF_8.name();


    @Min(value = 0, message = "Lines to skip (batch.input.linesToSkip) cannot be negative.")
    private int linesToSkip = 0;


    @NotEmpty(message = "Target class name (batch.input.targetFileType) must be specified and cannot be empty.")
    private String targetModel;




    @AssertTrue(message = "Delimiter (batch.input.delimiter) is required and cannot be empty when file FileType is DELIMITED.")
    public boolean isDelimiterValidForDelimitedFileType() {
        if (fileType == FileType.DELIMITED) {
            return delimiter != null && !delimiter.isEmpty();
        }
        return true; // Validation passes if not DELIMITED FileType
    }


    @AssertTrue(message = "Column ranges (batch.input.columnRanges) are required and cannot be empty when file FileType is FIXED_LENGTH.")
    public boolean isColumnRangesValidForFixedLengthFileType() {
        if (fileType == FileType.FIXED_LENGTH) {
            return columnRanges != null && !columnRanges.trim().isEmpty();
        }
        return true; // Validation passes if not FIXED_LENGTH FileType
    }


    @AssertTrue(message = "Field names (batch.input.names) are required and cannot be empty when file FileType is DELIMITED or FIXED_LENGTH.")
    public boolean isNamesRequiredForFlatFiles() {
        if (fileType == FileType.DELIMITED || fileType == FileType.FIXED_LENGTH) {
            // The @NotNull on 'names' ensures it's not null. This checks for emptiness.
            return names.length > 0;
        }
        return true; // Validation passes for JSON FileType (names can be empty array)
    }


    @AssertTrue(message = "Invalid or unsupported character encoding specified in 'batch.input.encoding'.")
    public boolean isValidEncoding() {

        if (encoding.trim().isEmpty()) { // Should not happen if @NotEmpty is used on encoding (if desired)
            return false;
        }
        try {
            Charset.forName(encoding);
            return true;
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            return false; // The message from @AssertTrue will be used by the validation framework
        }
    }
}
