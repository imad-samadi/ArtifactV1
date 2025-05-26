package art.artifact_batch.Config.Writer.WriteFile;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Validated
@ConfigurationProperties(prefix = "batch.output.file")
public class WriteProperties {
    public enum FileType { DELIMITED, JSON }

    @NotEmpty(message = "Output file path (batch.output.filePath) must be provided and cannot be empty.")
    private String filePath;

    @NotNull(message = "File type (batch.output.type) must be specified.")
    private FileType fileType = FileType.DELIMITED;

    @NotEmpty(message = "Target class name must be provided for reflection")
    private String targetModel;

    private String delimiter = ",";

    private Character quoteCharacter;

    private String encoding = StandardCharsets.UTF_8.name();

    private boolean append = false;

    private boolean transactional = true;

    private String header;

    private String footer;

    private String[] fieldNames;



    @AssertTrue(message = "Delimiter (batch.output.delimiter) is required and cannot be empty when file type is DELIMITED.")
    public boolean isDelimiterValidForDelimitedType() {
        if (fileType == FileType.DELIMITED) {
            return delimiter != null && !delimiter.isEmpty();
        }
        return true;
    }

    @AssertTrue(message = "Invalid or unsupported character encoding specified in 'batch.output.encoding'.")
    public boolean isValidEncoding() {
        try {
            Charset.forName(encoding);
            return true;
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            return false;
        }
    }

    @PostConstruct
    public void initFieldNamesIfEmpty() {
        if (fieldNames == null || fieldNames.length == 0) {
            try {
                Class<?> clazz = Class.forName(targetModel);
                Field[] fields = clazz.getDeclaredFields();
                fieldNames = Arrays.stream(fields)
                        .map(Field::getName)
                        .toArray(String[]::new);
                if (header == null || header.isEmpty()) {
                    header = String.join(delimiter, fieldNames);
                }

            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Cannot load class for reflection: " + targetModel, e);
            }
        }
    }
}
