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


    protected final String[] ranges;







    @AssertTrue(message = "Each range string in 'ranges' (if provided) must be in 'start-end' format, with start > 0 and start <= end.")
    private boolean isEachRangeFormatValidIfProvided() {
        if (getRanges() == null || getRanges().length == 0) {
            return true;
        }
        for (String rangeStr : getRanges()) {
            if (rangeStr == null || !rangeStr.matches("^\\d+-\\d+$")) return false;
            String[] parts = rangeStr.split("-");
            try {
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                if (start <= 0 || start > end) return false;
            } catch (NumberFormatException e) { return false; }
        }
        return true;
    }

    public Range[] toRangeArray() {
        if (getRanges() == null) {
            // This will be caught by DefaultFixedLengthTokenizerProvider if ranges are needed
            throw new IllegalStateException("Ranges array cannot be null when attempting to convert.");
        }
        Range[] batchRanges = new Range[getRanges().length];
        for (int i = 0; i < getRanges().length; i++) {
            String[] parts = getRanges()[i].split("-");
            batchRanges[i] = new Range(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
        return batchRanges;
    }


}
