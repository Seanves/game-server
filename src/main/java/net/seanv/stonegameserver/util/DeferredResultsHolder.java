package net.seanv.stonegameserver.util;

import org.springframework.web.context.request.async.DeferredResult;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DeferredResultsHolder<T> {
    private final Map<Integer, DeferredResult<T>> map = new ConcurrentHashMap<>();
    private final T onTimeoutValue;

    private static final long TIMEOUT = 120000; // 2 min

    public DeferredResultsHolder(T onTimeoutValue) {
        this.onTimeoutValue = onTimeoutValue;
    }

    /**
     * Store {@link DeferredResult} for a given key if key is absent,
     * otherwise returns completed with {@code onDuplicateValue}.
     * @return always new {@link DeferredResult}
     */
    public DeferredResult<T> tryPut(int key, T onDuplicateValue) {
        DeferredResult<T> result = new DeferredResult<>(TIMEOUT, onTimeoutValue);

        var existing = map.putIfAbsent(key, result);

        if (existing == null) {
            result.onCompletion(() -> map.remove(key));
        } else {
            result.setResult(onDuplicateValue);
        }

        return result;
    }

    public void complete(int key, T value) {
        DeferredResult<T> result = map.remove(key);
        if (result != null) {
            result.setResult(value);
        }
    }

    public DeferredResult<T> createAndSet(T value) {
        DeferredResult<T> result = new DeferredResult<>();
        result.setResult(value);
        return result;
    }
}
