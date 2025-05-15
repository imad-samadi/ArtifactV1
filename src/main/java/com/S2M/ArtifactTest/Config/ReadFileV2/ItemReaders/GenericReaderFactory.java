package com.S2M.ArtifactTest.Config.ReadFileV2.ItemReaders;

import com.S2M.ArtifactTest.Config.ReadFileV2.Config.AbstractFileReaderConfig;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
@Component
public class GenericReaderFactory {

    private final List<ItemReaderBuilder<?, ?>> builders;


    // Assuming ItemReaderBuilder interface and implementations are in the same package or imported
    public GenericReaderFactory(List<ItemReaderBuilder<?, ?>> builders) {
        this.builders = builders != null ? List.copyOf(builders) : List.of();
    }

    @SuppressWarnings("unchecked")
    public <T> ItemReader<T> createReader(AbstractFileReaderConfig<T> config) {
        if (config == null) {
            // Or delegate null check to the specific builder's validate method
            throw new IllegalArgumentException("Configuration cannot be null.");
        }

        Optional<ItemReaderBuilder<?, ?>> foundBuilder = builders.stream()
                .filter(builder -> builder.supports(config))
                .findFirst();

        if (foundBuilder.isPresent()) {

            ItemReaderBuilder<T, AbstractFileReaderConfig<T>> specificBuilder =
                    (ItemReaderBuilder<T, AbstractFileReaderConfig<T>>) foundBuilder.get();


            return specificBuilder.build(config);
        } else {
            throw new IllegalArgumentException("No suitable ItemReaderBuilder found for config type: " +
                    config.getClass().getName());
        }
    }
}
