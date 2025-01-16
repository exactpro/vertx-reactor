package io.vertx.reactor3;

import io.vertx.reactor3.impl.FluxReadStream;
import io.vertx.reactor3.impl.FluxUnmarshaller;
import io.vertx.reactor3.impl.ReadStreamSubscriber;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.Function;

public class FluxHelper {

    /**
     * Adapts a Project Reactor {@link Flux<T>} to a Vert.x {@link ReadStream<T>}. The returned
     * read stream will be subscribed to the {@link Flux<T>}.<p>
     *
     * @param flux the flux to adapt
     * @return the adapted stream
     */
    public static <T> ReadStream<T> toReadStream(Flux<T> flux) {
        return ReadStreamSubscriber.asReadStream(flux, Function.identity());
    }

    /**
     * Like {@link #toFlux(ReadStream)} but with a {@code mapping} function
     */
    public static <T, U> Flux<U> toFlux(ReadStream<T> stream, Function<T, U> mapping) {
        return FluxReadStream.fromStream(stream).map(mapping);
    }

    /**
     * Adapts a Vert.x {@link ReadStream<T>} to an RxJava {@link Flux<T>}. After
     * the stream is adapted to a flowable, the original stream handlers should not be used anymore
     * as they will be used by the flowable adapter.<p>
     *
     * @param stream the stream to adapt
     * @return the adapted observable
     */
    public static <T> Flux<T> toFlux(ReadStream<T> stream) {
        return FluxReadStream.fromStream(stream);
    }

    public static <T, U> Flux<T> write(WriteStream<T> writeStream, Publisher<U> publisher, Function<? super U, ? extends T> mapper, int concurrency) {
        return Flux.from(publisher).map(mapper).flatMap(
            t -> MonoHelper
                .<Void>toMono(handler -> writeStream.write(t, handler))
                .thenReturn(t),
            concurrency
        );
    }

    public static <T> Flux<T> write(WriteStream<T> writeStream, Publisher<? extends T> publisher, int concurrency) {
        return Flux.from(publisher).flatMap(
            t -> MonoHelper
                .<Void>toMono(handler -> writeStream.write(t, handler))
                .thenReturn(t),
            concurrency
        );
    }

    public static <T> Function<Flux<Buffer>, Flux<T>> unmarshaller(Class<T> mappedType) {
        return new FluxUnmarshaller<>(Function.identity(), mappedType);
    }

    public static <T> Function<Flux<Buffer>, Flux<T>> unmarshaller(TypeReference<T> mappedTypeRef) {
        return new FluxUnmarshaller<>(Function.identity(), mappedTypeRef);
    }

    public static <T> Function<Flux<Buffer>, Flux<T>> unmarshaller(Class<T> mappedType, ObjectCodec mapper) {
        return new FluxUnmarshaller<>(Function.identity(), mappedType, mapper);
    }

    public static <T> Function<Flux<Buffer>, Flux<T>> unmarshaller(TypeReference<T> mappedTypeRef, ObjectCodec mapper) {
        return new FluxUnmarshaller<>(Function.identity(), mappedTypeRef, mapper);
    }
}