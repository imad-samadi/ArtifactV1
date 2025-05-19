package com.S2M.ArtifactTest.Config.ReadFile.Util;
import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import com.S2M.ArtifactTest.Config.ReadFile.ReadProperties;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class with static factory methods for creating LineTokenizer instances.
 * This class is not meant to be instantiated.
 */
public final class TokenizerFactories {

    // Private constructor to prevent instantiation
    private TokenizerFactories() {
        throw new IllegalStateException("Utility class cannot be instantiated.");
    }

    public static LineTokenizer getLineTokenizer(ReadProperties props) {

        if (ReadProperties.FileType.DELIMITED.equals(props.getType())) {
            return createDelimitedTokenizer(props);
        }else if (ReadProperties.FileType.FIXED_LENGTH.equals(props.getType())) {
            return createFixedLengthTokenizer(props);
        }else {

            throw new ConfigurationException("Unsupported file type for default tokenizers: " + props.getType() +
                    ". Provide a custom LineTokenizer bean or ensure a FileReaderProvider handles this type.");
        }
    }

    /**
     * Creates a DelimitedLineTokenizer based on the provided properties.
     */
    public static LineTokenizer createDelimitedTokenizer(ReadProperties props) {

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(props.getDelimiter());
        tokenizer.setNames(props.getNames());
        if (props.getQuoteCharacter() != null) {
            tokenizer.setQuoteCharacter(props.getQuoteCharacter());
        }
        return tokenizer;
    }

    /**
     * Creates a FixedLengthTokenizer based on the provided properties.
     */
    public static LineTokenizer createFixedLengthTokenizer(ReadProperties props) {


        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setColumns(parseRanges(props.getColumnRanges()));
        tokenizer.setNames(props.getNames());
        return tokenizer;
    }

    /**
     * Helper method to parse column ranges for FixedLengthTokenizer.
     */
    private static Range[] parseRanges(String rangesString) {
        if (!StringUtils.hasText(rangesString)) {
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
                    if (min <= 0 || max <= 0) {
                        throw new ConfigurationException("Range values must be positive: '" + pair + "'.");
                    }
                    if (min > max) {
                        throw new ConfigurationException("Invalid range: min value cannot be greater than max value in '" + pair + "'.");
                    }
                    ranges.add(new Range(min, max));
                } else if (minMax.length == 1) {
                    int pos = Integer.parseInt(minMax[0].trim());
                    if (pos <= 0) {
                        throw new ConfigurationException("Range position must be positive: '" + pair + "'.");
                    }
                    ranges.add(new Range(pos, pos));
                } else {
                    throw new ConfigurationException("Invalid range format in: '" + pair + "'. Expected 'min-max' or 'pos'.");
                }
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Invalid number found in range string: '" + pair + "'. Ranges must be numeric.", e);
            }
        }
        return ranges.toArray(new Range[0]);
    }
}
