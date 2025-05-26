package art.artifact_batch.Config.Listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Component
public class PerThreadCountingWriteListener implements ItemWriteListener<Object> {

    // Tracks total written items per thread
    private final Map<String, LongAdder> counts = new ConcurrentHashMap<>();



    @Override
    public void afterWrite(Chunk<? extends Object> items) {
        String thread = Thread.currentThread().getName();
        LongAdder adder = counts.computeIfAbsent(thread, t -> new LongAdder());
        adder.add(items.size());
        long total = adder.sum();
        log.info("Thread [{}] wrote {} items in this chunk, cumulative total = {}",
                thread, items.size(), total);
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends Object> items) {
        String thread = Thread.currentThread().getName();
        LongAdder adder = counts.get(thread);
        long total = (adder != null) ? adder.sum() : 0;
        log.error("Thread [{}] failed to write {} items in this chunk (cumulative = {}). Error: {}",
                thread, items.size(), total, exception.getMessage());
    }
}
