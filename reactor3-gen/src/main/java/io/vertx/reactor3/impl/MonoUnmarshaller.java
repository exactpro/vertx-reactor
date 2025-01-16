package io.vertx.reactor3.impl;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.buffer.Buffer;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class MonoUnmarshaller<T, B> implements Function<Mono<B>, Mono<T>> {
    private final Function<B, Buffer> unwrap;
    private final Function<Buffer, T> unmarshall;

    public MonoUnmarshaller(Function<B, Buffer> unwrap, Class<T> mappedType) {
        this(unwrap, requireNonNull(mappedType, "mappedType cannot be null"), null, null);
    }

    public MonoUnmarshaller(Function<B, Buffer> unwrap, TypeReference<T> mappedTypeRef) {
        this(unwrap, null, requireNonNull(mappedTypeRef, "mappedTypeRef cannot be null"), null);
    }

    public MonoUnmarshaller(Function<B, Buffer> unwrap, Class<T> mappedType, ObjectCodec mapper) {
        this(unwrap, requireNonNull(mappedType, "mappedType cannot be null"), null, mapper);
    }

    public MonoUnmarshaller(Function<B, Buffer> unwrap, TypeReference<T> mappedTypeRef, ObjectCodec mapper) {
        this(unwrap, null, requireNonNull(mappedTypeRef, "mappedTypeRef cannot be null"), mapper);
    }

    private MonoUnmarshaller(
        Function<B, Buffer> unwrap, Class<T> mappedType, TypeReference<T> mappedTypeRef, ObjectCodec mapper
    ) {
        this.unwrap = requireNonNull(unwrap, "unwrap cannot be null");
        this.unmarshall = FluxUnmarshaller.unmarshallerFor(mappedType, mappedTypeRef, mapper);
    }

    @Override
    public Mono<T> apply(@NonNull Mono<B> upstream) {
        return upstream.map(unwrap).mapNotNull(this.unmarshall);
    }
}