package io.vertx.reactor3.core;

import io.vertx.reactor3.core.impl.AsyncResultMono;
import io.vertx.reactor3.core.impl.MonoUnmarshaller;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class MonoHelper {

    private static final CoreSubscriber<?> NULL_SUBSCRIBER = new CoreSubscriber<Object>() {
        @Override
        public void onSubscribe(@NonNull Subscription s) {
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(@NonNull Object o) {
        }

        @Override
        public void onError(@NonNull Throwable e) {
        }

        @Override
        public void onComplete() {
        }
    };

    /**
     * @return a {@code CoreSubscriber} that does nothing
     */
    public static <T> CoreSubscriber<T> nullObserver() {
        return (CoreSubscriber<T>) NULL_SUBSCRIBER;
    }

    /**
     * Returns a {@link Mono} that, when subscribed, uses the provided {@code handler} to adapt a callback-based asynchronous method.
     * <p>
     * For example:
     * <pre> {@code
     * io.vertx.core.Vertx vertx = Vertx.vertx();
     * Maybe<String> blockingMethodResult = MaybeHelper.toMaybe(handler -> vertx.<String>executeBlocking(fut -> fut.complete(invokeBlocking()), handler));
     * }</pre>
     * <p>
     * This is useful when using RxJava without the Vert.x Rxified API or your own asynchronous methods.
     *
     * @param handler the code executed when the returned {@link Mono} is subscribed
     */
    public static <T> Mono<T> toMono(Consumer<Handler<AsyncResult<T>>> handler) {
        return AsyncResultMono.toMono(handler);
    }

    /**
     * Adapts an Vert.x {@code Handler<AsyncResult<T>>} to an Project Reactor {@link CoreSubscriber}.
     * <p>
     * The returned observer can be subscribed to an {@link Mono#subscribe()}.
     *
     * @param handler the handler to adapt
     * @return the observer
     */
    public static <T> CoreSubscriber<T> toSubscriber(Handler<AsyncResult<T>> handler) {
        AtomicBoolean completed = new AtomicBoolean();
        return new CoreSubscriber<T>() {
            @Override
            public void onSubscribe(@NonNull Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onComplete() {
                if (completed.compareAndSet(false, true)) {
                    handler.handle(io.vertx.core.Future.succeededFuture());
                }
            }

            @Override
            public void onNext(@NonNull T item) {
                if (completed.compareAndSet(false, true)) {
                    handler.handle(io.vertx.core.Future.succeededFuture(item));
                }
            }

            @Override
            public void onError(Throwable error) {
                if (completed.compareAndSet(false, true)) {
                    handler.handle(io.vertx.core.Future.failedFuture(error));
                }
            }
        };
    }

    /**
     * Adapts an Project Reactor {@code Maybe<T>} to a Vert.x {@link Future<T>}.
     * <p>
     * The mono will be immediately subscribed and the returned future will
     * be updated with the result of the single.
     *
     * @param mono the single to adapt
     * @return the future
     */
    public static <T> Future<T> toFuture(Mono<T> mono) {
        Promise<T> promise = Promise.promise();
        mono.subscribe(promise::complete, promise::fail, promise::complete);
        return promise.future();
    }

    /**
     * Like {@link MonoHelper#toFuture(Mono)} but with an {@code adapter} of the result.
     */
    public static <T, U> Future<U> toFuture(Mono<T> mono, Function<T, U> adapter) {
        return toFuture(mono.map(adapter));
    }

    public static <T> Function<Mono<Buffer>, Mono<T>> unmarshaller(Class<T> mappedType) {
        return new MonoUnmarshaller<>(Function.identity(), mappedType);
    }

    public static <T> Function<Mono<Buffer>, Mono<T>> unmarshaller(TypeReference<T> mappedTypeRef) {
        return new MonoUnmarshaller<>(Function.identity(), mappedTypeRef);
    }

    public static <T> Function<Mono<Buffer>, Mono<T>> unmarshaller(Class<T> mappedType, ObjectCodec mapper) {
        return new MonoUnmarshaller<>(Function.identity(), mappedType, mapper);
    }

    public static <T> Function<Mono<Buffer>, Mono<T>> unmarshaller(
        TypeReference<T> mappedTypeRef, ObjectCodec mapper
    ) {
        return new MonoUnmarshaller<>(Function.identity(), mappedTypeRef, mapper);
    }
}