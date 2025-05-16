package com.S2M.ArtifactTest.Config.ReadFileV2.Core.SPI;

import com.S2M.ArtifactTest.Config.ReadFileV2.Core.AbstractFileReaderConfig;
import org.springframework.batch.item.file.transform.LineTokenizer;

public interface TokenizerProvider {

    boolean supports(AbstractFileReaderConfig<?> config);
    LineTokenizer createTokenizer(AbstractFileReaderConfig<?> config);
}
