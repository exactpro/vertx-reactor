package io.vertx.reactor3.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.jackson.JacksonFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class FluxUnmarshaller<T, B> implements Function<Flux<B>, Flux<T>> {
    private final Function<B, Buffer> unwrap;
    private final Function<Buffer, T> unmarshall;

    public FluxUnmarshaller(Function<B, Buffer> unwrap, Class<T> mappedType) {
        this(unwrap, requireNonNull(mappedType, "mappedType cannot be null"), null, null);
    }

    public FluxUnmarshaller(Function<B, Buffer> unwrap, TypeReference<T> mappedTypeRef) {
        this(unwrap, null, requireNonNull(mappedTypeRef, "mappedTypeRef cannot be null"), null);
    }

    public FluxUnmarshaller(Function<B, Buffer> unwrap, Class<T> mappedType, ObjectCodec mapper) {
        this(unwrap, requireNonNull(mappedType, "mappedType cannot be null"), null, mapper);
    }

    public FluxUnmarshaller(Function<B, Buffer> unwrap, TypeReference<T> mappedTypeRef, ObjectCodec mapper) {
        this(unwrap, null, requireNonNull(mappedTypeRef, "mappedTypeRef cannot be null"), mapper);
    }

    private FluxUnmarshaller(
        Function<B, Buffer> unwrap, Class<T> mappedType, TypeReference<T> mappedTypeRef, ObjectCodec mapper
    ) {
        this.unwrap = requireNonNull(unwrap, "unwrap cannot be null");
        this.unmarshall = unmarshallerFor(mappedType, mappedTypeRef, mapper);
    }

    @Override
    public Flux<T> apply(@reactor.util.annotation.NonNull Flux<B> upstream) {
        return upstream
            .map(unwrap)
            .collect(Buffer::buffer, Buffer::appendBuffer)
            .mapNotNull(this.unmarshall)
            .flux();
    }

    static <T> Function<Buffer, T> unmarshallerFor(
        Class<T> mappedType, TypeReference<T> mappedTypeRef, ObjectCodec mapper
    ) {
        if (mappedType != null) {
            return new TypeUnmarshaller<>(mappedType, mapper);
        } else {
            return new TypeRefUnmarshaller<>(mappedTypeRef, mapper);
        }
    }

    private static class TypeUnmarshaller<T> implements Function<Buffer, T> {
        private final Class<T> mappedType;

        private final ObjectCodec mapper;

        public TypeUnmarshaller(Class<T> mappedType, ObjectCodec mapper) {
            this.mappedType = mappedType;
            this.mapper = mapper;
        }

        @Override
        public T apply(Buffer buffer) {
            if (buffer.length() > 0) {
                try {
                    if (mapper == null) {
                        return Json.CODEC.fromBuffer(buffer, mappedType);
                    } else {
                        JsonParser parser = mapper.getFactory().createParser(buffer.getBytes());
                        return mapper.readValue(parser, mappedType);
                    }
                } catch (Exception ex) {
                    throw Exceptions.propagate(ex);
                }
            } else {
                return null;
            }
        }
    }

    private static class TypeRefUnmarshaller<T> implements Function<Buffer, T> {
        private final TypeReference<T> mappedTypeRef;

        private final ObjectCodec mapper;

        public TypeRefUnmarshaller(TypeReference<T> mappedTypeRef, ObjectCodec mapper) {
            this.mappedTypeRef = mappedTypeRef;
            this.mapper = mapper;
        }

        @Override
        public T apply(Buffer buffer) {
            if (buffer.length() > 0) {
                try {
                    if (mapper == null) {
                        return JacksonFactory.CODEC.fromBuffer(buffer, mappedTypeRef);
                    } else {
                        JsonParser parser = mapper.getFactory().createParser(buffer.getBytes());
                        return mapper.readValue(parser, mappedTypeRef);
                    }
                } catch (Exception ex) {
                    throw Exceptions.propagate(ex);
                }
            } else {
                return null;
            }
        }
    }
}