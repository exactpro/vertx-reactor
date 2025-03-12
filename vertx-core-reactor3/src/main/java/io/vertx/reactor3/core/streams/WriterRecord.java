package io.vertx.reactor3.core.streams;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.util.Objects;

public class WriterRecord<T, C> {
    private final @Nullable C correlationMetadata;

    private final @NonNull T value;

    public WriterRecord(@NonNull T value, @Nullable C correlationMetadata) {
        this.value = Objects.requireNonNull(value, "value cannot be null");
        this.correlationMetadata = correlationMetadata;
    }

    public @Nullable C correlationMetadata() {
        return correlationMetadata;
    }

    public @NonNull T value() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        WriterRecord<?, ?> that = (WriterRecord<?, ?>) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
