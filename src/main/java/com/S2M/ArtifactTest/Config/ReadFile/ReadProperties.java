package com.S2M.ArtifactTest.Config.ReadFile;

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
@ConfigurationProperties(prefix = "batch.input")
public class ReadProperties {

    public enum FileType { DELIMITED, FIXED_LENGTH, JSON }

    @NotEmpty(message = "filePath must be provided")
    private String filePath;

    @NotNull(message = "type must be specified")
    private FileType type = FileType.DELIMITED;

    private String delimiter = ",";
    private Character quoteCharacter;

    @NotEmpty(message = "names must be provided")
    private String[] names;

    @Pattern(regexp = "^\\d+(-\\d+)?(,\\s*\\d+(-\\d+)?)*$",
            message = "columnRanges format invalid")
    private String columnRanges;

    @NotNull(message = "encoding must be specified")
    private String encoding = StandardCharsets.UTF_8.name();

    @Min(value = 0, message = "linesToSkip cannot be negative")
    private int linesToSkip = 1;

    @NotEmpty(message = "targetType must be specified")
    private String targetType;

    @AssertTrue(message = "delimiter required when type is DELIMITED")
    public boolean isDelimiterValid() {
        return type != FileType.DELIMITED || (delimiter != null && !delimiter.isEmpty());
    }

    @AssertTrue(message = "columnRanges required when type is FIXED_LENGTH")
    public boolean isColumnRangesValid() {
        return type != FileType.FIXED_LENGTH || (columnRanges != null && !columnRanges.trim().isEmpty());
    }

    @AssertTrue(message = "encoding not supported")
    public boolean isEncodingValid() {
        try {
            Charset.forName(encoding);
            return true;
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            return false;
        }
    }
}
