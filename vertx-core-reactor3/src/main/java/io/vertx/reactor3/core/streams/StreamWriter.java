package io.vertx.reactor3.core.streams;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.Objects;

public class StreamWriter<T> {
    private final boolean stopOnErrors;

    private final int concurrency;

    private final @NonNull WriteStream<T> stream;

    public StreamWriter(@NonNull WriteStream<T> stream) {
        // TODO replace 256 with constant
        this(Objects.requireNonNull(stream, "stream cannot be null"), 256, false);
    }

    private StreamWriter(@NonNull WriteStream<T> stream, int concurrency, boolean stopOnErrors) {
        this.stream = stream;
        this.concurrency = concurrency;
        this.stopOnErrors = stopOnErrors;
    }

    public @NonNull StreamWriter<T> concurrency(int concurrency) {
        if (concurrency <= 0) {
            throw new IllegalArgumentException("concurrency should be greater than 0 but was " + concurrency);
        }
        return new StreamWriter<>(stream, concurrency, stopOnErrors);
    }

    public @NonNull StreamWriter<T> stopOnErrors(boolean stopOnErrors) {
        return new StreamWriter<>(stream, concurrency, stopOnErrors);
    }

    public <C> @NonNull Flux<WriterResult<C>> write(@NonNull Publisher<WriterRecord<? extends T, C>> publisher) {
        return Flux.from(publisher).flatMap(
            stopOnErrors ? this::writeRecord : this::writeRecordReturningError,
            concurrency
        );
    }

    public static <T> @NonNull Mono<Void> write(
        @NonNull Publisher<? extends T> publisher, @NonNull WriteStream<T> stream
    ) {
        return new StreamWriter<>(stream).stopOnErrors(true)
            .write(
                Flux.from(publisher).map(v -> new WriterRecord<T, Void>(v, null))
            )
            .then();
    }

    private <C> Mono<WriterResult<C>> writeRecord(WriterRecord<? extends T, C> record) {
        return stream.rxWrite(record.value())
            .thenReturn(new WriterResult<>(record.correlationMetadata(), null));
    }

    private <C> Mono<WriterResult<C>> writeRecordReturningError(WriterRecord<? extends T, C> record) {
        return writeRecord(record)
            .onErrorResume(error -> Mono.just(new WriterResult<>(record.correlationMetadata(), error)));
    }
}
