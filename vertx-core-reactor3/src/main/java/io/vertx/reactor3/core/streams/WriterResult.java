package io.vertx.reactor3.core.streams;

import reactor.util.annotation.Nullable;

public class WriterResult<T> {
    private final @Nullable T correlationMetadata;

    private final @Nullable Throwable error;

    public WriterResult(@Nullable T correlationMetadata, @Nullable Throwable error) {
        this.correlationMetadata = correlationMetadata;
        this.error = error;
    }

    public @Nullable T getCorrelationMetadata() {
        return correlationMetadata;
    }

    public @Nullable Throwable getError() {
        return error;
    }
}
