package io.vertx.reactor3.core;

import io.vertx.reactor3.core.impl.FluxReadStream;
import io.vertx.reactor3.core.impl.ReadStreamSubscriber;
import io.vertx.reactor3.core.streams.ReadStream;
import io.vertx.reactor3.core.streams.WriteStream;
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
        return ReadStream.newInstance(ReadStreamSubscriber.asReadStream(flux, Function.identity()));
    }

    /**
     * Like {@link #toFlux(ReadStream)} but with a {@code mapping} function
     */
    public static <T, U> Flux<U> toFlux(ReadStream<T> stream, Function<T, U> mapping) {
        // TODO Is it possible to generate getDelegate() with generic parameter?
        return FluxReadStream.fromStream((io.vertx.core.streams.ReadStream<T>) stream.getDelegate()).map(mapping);
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
        return FluxReadStream.fromStream((io.vertx.core.streams.ReadStream<T>) stream.getDelegate());
    }

    public static <T, U> Flux<U> write(
        WriteStream<U> writeStream, Publisher<? extends U> publisher, Function<? super U, ? extends T> mapper, int concurrency
    ) {
        io.vertx.core.streams.WriteStream<T> delegateStream = writeStream.getDelegate();
        return Flux.from(publisher).flatMap(
            t -> MonoHelper
                .<Void>toMono(handler -> delegateStream.write(mapper.apply(t), handler))
                .thenReturn(t),
            concurrency
        );
    }

    public static <T> Flux<T> write(WriteStream<T> writeStream, Publisher<? extends T> publisher, int concurrency) {
        return write(writeStream, publisher, Function.identity(), concurrency);
    }

//    public static <T> Function<Flux<Buffer>, Flux<T>> unmarshaller(Class<T> mappedType) {
//        return new FluxUnmarshaller<>(Function.identity(), mappedType);
//    }
//
//    public static <T> Function<Flux<Buffer>, Flux<T>> unmarshaller(TypeReference<T> mappedTypeRef) {
//        return new FluxUnmarshaller<>(Function.identity(), mappedTypeRef);
//    }
//
//    public static <T> Function<Flux<Buffer>, Flux<T>> unmarshaller(Class<T> mappedType, ObjectCodec mapper) {
//        return new FluxUnmarshaller<>(Function.identity(), mappedType, mapper);
//    }
//
//    public static <T> Function<Flux<Buffer>, Flux<T>> unmarshaller(TypeReference<T> mappedTypeRef, ObjectCodec mapper) {
//        return new FluxUnmarshaller<>(Function.identity(), mappedTypeRef, mapper);
//    }
}