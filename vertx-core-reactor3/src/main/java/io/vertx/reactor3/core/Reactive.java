package io.vertx.reactor3.core;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.reactor3.core.impl.AsyncResultMono;
import io.vertx.reactor3.core.impl.FluxReadStream;
import io.vertx.reactor3.core.impl.ReadStreamSubscriber;
import io.vertx.reactor3.core.streams.ReadStream;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Reactive {
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
    public static <T> @NonNull Mono<T> toMono(@NonNull Consumer<Handler<AsyncResult<T>>> handler) {
        return AsyncResultMono.toMono(handler);
    }

    /**
     * Adapts a Project Reactor {@code Mono<T>} to a Vert.x {@link Future <T>}.
     * <p>
     * The mono will be immediately subscribed and the returned future will
     * be updated with the result of the mono.
     *
     * @param mono the mono to adapt
     * @return the future
     */
    public static <T> @NonNull Future<T> toFuture(@NonNull Mono<T> mono) {
        io.vertx.core.Promise<T> promise = Promise.promise();
        mono.subscribe(toSubscriber(promise));
        return promise.future();
    }

    /**
     * Adapts an Vert.x {@code Handler<AsyncResult<T>>} to an Project Reactor {@link CoreSubscriber}.
     * <p>
     * The returned observer can be subscribed to an {@link Mono#subscribe()}.
     *
     * @param handler the handler to adapt
     * @return the observer
     */
    public static <T> @NonNull CoreSubscriber<T> toSubscriber(@NonNull Handler<AsyncResult<T>> handler) {
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
                //      error signal received after next should be logged.
                if (completed.compareAndSet(false, true)) {
                    handler.handle(io.vertx.core.Future.failedFuture(error));
                } else {
                    Operators.onErrorDropped(error, currentContext());
                }
            }
        };
    }

    /**
     * Adapts a Project Reactor {@link Flux <T>} to a Vert.x {@link ReadStream <T>}. The returned
     * read stream will be subscribed to the {@link Flux<T>}.<p>
     *
     * @param flux the flux to adapt
     * @return the adapted stream
     */
    public static <T> @NonNull ReadStream<T> toReadStream(@NonNull Flux<T> flux) {
        return ReadStream.newInstance(ReadStreamSubscriber.asReadStream(flux, Function.identity()));
    }

    /**
     * Adapts a Vert.x {@link ReadStream<T>} to an RxJava {@link Flux<T>}. After
     * the stream is adapted to a flowable, the original stream handlers should not be used anymore
     * as they will be used by the flowable adapter.<p>
     *
     * @param stream the stream to adapt
     * @return the adapted observable
     */
    public static <T> @NonNull Flux<T> toFlux(@NonNull ReadStream<T> stream) {
        // XXX Is it possible to generate getDelegate() with generic parameter?
        return FluxReadStream.fromStream((io.vertx.core.streams.ReadStream<T>) stream.getDelegate());
    }

    /**
     * Create a scheduler for a {@link io.vertx.core.Vertx} object, actions are executed on the event loop.
     *
     * @param vertx the vertx object
     * @return the scheduler
     */
    public static @NonNull Scheduler scheduler(@NonNull io.vertx.core.Vertx vertx) {
        return new ContextScheduler(vertx, false);
    }

    /**
     * Create a scheduler for a {@link io.vertx.core.Context}, actions are executed on the event loop of this context.
     *
     * @param context the context object
     * @return the scheduler
     */
    public static @NonNull Scheduler scheduler(@NonNull Context context) {
        return new ContextScheduler(context, false);
    }

    /**
     * Create a scheduler for a {@link io.vertx.core.Vertx} object, actions can be blocking, they are not executed
     * on Vertx event loop.
     *
     * @param vertx the vertx object
     * @return the scheduler
     */
    public static @NonNull Scheduler blockingScheduler(@NonNull io.vertx.core.Vertx vertx) {
        return new ContextScheduler(vertx, true);
    }

    /**
     * Create a scheduler for a {@link io.vertx.core.Vertx} object, actions can be blocking, they are not executed
     * on Vertx event loop.
     *
     * @param vertx the vertx object
     * @param ordered  if true then if when tasks are scheduled several times on the same context, the executions
     *                 for that context will be executed serially, not in parallel. if false then they will be no ordering
     *                 guarantees
     * @return the scheduler
     */
    public static @NonNull Scheduler blockingScheduler(@NonNull Vertx vertx, boolean ordered) {
        return new ContextScheduler(vertx, true, ordered);
    }

    /**
     * Create a scheduler for a {@link io.vertx.core.WorkerExecutor} object, actions are executed on the threads of this executor.
     *
     * @param executor the worker executor object
     * @return the scheduler
     */
    public static @NonNull Scheduler blockingScheduler(@NonNull WorkerExecutor executor) {
        return new ContextScheduler(executor, false);
    }

    private Reactive() {
    }
}
