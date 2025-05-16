package com.S2M.ArtifactTest.Config.ReadFileV2.Fixedlength.Config;

import com.S2M.ArtifactTest.Config.ReadFileV2.Core.AbstractFileReaderConfig;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


import org.springframework.batch.item.file.transform.Range;

/**
 * Configuration for fixed-length files.
 *
 * @param <T> The target item type.
 */
@Getter
@ToString(callSuper = true)
@SuperBuilder

public class FixedLengthFileReaderConfig<T> extends AbstractFileReaderConfig<T> {



    /**
     * An array of strings defining the character ranges for each field.
     * Each string must be in the format "start-end" (1-based inclusive, e.g., "1-5").
     * This field is mandatory and must contain at least one range definition.
     */
    @NotEmpty(message = "Ranges array (ranges) cannot be null or empty.")
    protected final String[] ranges;




    /**
     * Convenience method to convert the string-based ranges into Spring Batch {@link Range} objects.
     * This method assumes that the 'ranges' field has already passed validation
     * @return An array of {@link Range} objects.
     * @throws IllegalStateException if ranges is null (should be caught by validation).
     * @throws NumberFormatException if parsing fails (should be caught by validation).
     */
    public Range[] toRangeArray() {
        if (this.ranges == null) {
            // This case should ideally be prevented by @NotEmpty validation,
            // but a runtime check here can be a safeguard.
            throw new IllegalStateException("Ranges array cannot be null when attempting to convert to Range[]. " +
                    "Ensure configuration is validated.");
        }
        Range[] batchRanges = new Range[this.ranges.length];
        for (int i = 0; i < this.ranges.length; i++) {
            String[] parts = this.ranges[i].split("-");

            batchRanges[i] = new Range(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
        return batchRanges;
    }



    @AssertTrue(message = "Each range must be in 'start-end' format with start > 0 and start <= end")
    private boolean isEachRangeFormatValid() {
        if (ranges == null) return true; // @NotEmpty handles null case

        for (String range : ranges) {
            if (range == null || !range.matches("^\\d+-\\d+$")) {
                return false;
            }
            String[] parts = range.split("-");
            try {
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                if (start <= 0 || start > end) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "Ranges must be in ascending order without overlaps")
    private boolean areRangesContiguous() {
        if (getCustomLineMapper() != null) return true;
        if (ranges == null || !isEachRangeFormatValid()) return false;

        try {
            Range[] parsedRanges = toRangeArray();
            int previousEnd = 0;
            for (Range range : parsedRanges) {
                if (range.getMin() <= previousEnd) {
                    return false;
                }
                previousEnd = range.getMax();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
