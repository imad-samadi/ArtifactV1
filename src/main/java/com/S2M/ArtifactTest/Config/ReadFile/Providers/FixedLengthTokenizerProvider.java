package com.S2M.ArtifactTest.Config.ReadFile.Providers;

import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import com.S2M.ArtifactTest.Config.ReadFile.SPI.TokenizerProvider;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link TokenizerProvider} implementation for creating {@link FixedLengthTokenizer}.
 * This provider is used when the configured file type is {@link ReadProperties.FileType#FIXED_LENGTH}.
 */
@Component
public class FixedLengthTokenizerProvider implements TokenizerProvider {

    /**
     * Checks if this provider supports the given properties, specifically if the
     * file type is FIXED_LENGTH.
     * @param properties The configuration properties.
     * @return true if file type is FIXED_LENGTH, false otherwise.
     */
    @Override
    public boolean supports(ReadProperties properties) {
        return ReadProperties.FileType.FIXED_LENGTH.equals(properties.getType());
    }

    /**
     * Creates and configures a {@link FixedLengthTokenizer} based on the provided properties.
     * Parses column ranges and sets column names.
     * @param properties The configuration properties.
     * @return A configured {@link FixedLengthTokenizer}.
     * @throws ConfigurationException if column ranges are invalid or not provided for FIXED_LENGTH type.
     */
    @Override
    public LineTokenizer createTokenizer(ReadProperties properties) {
        // Column ranges are validated by ReadProperties for non-emptiness if type is FIXED_LENGTH.
        // The regex pattern also validates the format to some extent.
        // Additional validation for logical correctness of ranges (e.g., min <= max) could be added here or in parseRanges.
        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setColumns(parseRanges(properties.getColumnRanges()));
        tokenizer.setNames(properties.getNames()); // Names are mandatory as per ReadProperties

        return tokenizer;
    }

    /**
     * Parses a comma-separated string of column ranges (e.g., "1-5,6-10,11")
     * into an array of {@link Range} objects.
     * Supports single position ranges (e.g., "11" becomes Range(11,11)).
     * @param rangesString The string representation of column ranges.
     * @return An array of {@link Range} objects.
     * @throws ConfigurationException if the range string format is invalid or contains non-numeric values.
     */
    private Range[] parseRanges(String rangesString) {
        if (!StringUtils.hasText(rangesString)) {
            // This case should ideally be prevented by ReadProperties validation for FIXED_LENGTH type
            return new Range[0];
        }

        String[] rangePairs = rangesString.split(",");
        List<Range> ranges = new ArrayList<>();
        for (String pair : rangePairs) {
            String[] minMax = pair.trim().split("-");
            try {
                if (minMax.length == 2) {
                    int min = Integer.parseInt(minMax[0].trim());
                    int max = Integer.parseInt(minMax[1].trim());
                    if (min > max) {
                        throw new ConfigurationException("Invalid range: " + pair + ". Min value cannot be greater than max value.");
                    }
                    ranges.add(new Range(min, max));
                } else if (minMax.length == 1) {
                    int pos = Integer.parseInt(minMax[0].trim());
                    ranges.add(new Range(pos, pos)); // Single position range
                } else {
                    throw new ConfigurationException("Invalid range format in: '" + pair + "'. Expected format 'min-max' or 'pos'.");
                }
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Invalid number found in range string: '" + pair + "'. Ranges must be numeric.", e);
            }
        }
        return ranges.toArray(new Range[0]);
    }
}
