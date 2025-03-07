package io.vertx.reactor3.core.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.publisher.*;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Function;

public class AsyncResultMono<T> extends Mono<T> implements Scannable {

    public static <T> Mono<T> toMono(Consumer<Handler<AsyncResult<T>>> subscriptionConsumer) {
        if (subscriptionConsumer == null) {
            return Mono.error(new NullPointerException());
        }
        return onAssembly(new AsyncResultMono<>(subscriptionConsumer));
    }

    public static <T, R> Mono<R> toMono(Future<T> future, Function<T, R> mapping) {
        if (future == null) {
            return Mono.error(new NullPointerException());
        }
        return onAssembly(new AsyncResultMono<T>(future::onComplete)).map(mapping);
    }

    private final Consumer<Handler<AsyncResult<T>>> subscriptionConsumer;

    private AsyncResultMono(@NonNull Consumer<Handler<AsyncResult<T>>> subscriptionConsumer) {
        this.subscriptionConsumer = subscriptionConsumer;
    }

    @Override
    public void subscribe(@NonNull CoreSubscriber<? super T> actual) {
        // TODO Replicate Reactor's code?
//        CoreSubscriber<? super T> wrapped = Operators.restoreContextOnSubscriberIfAutoCPEnabled(this, actual);
        CoreSubscriber<? super T> wrapped = actual;
        wrapped.onSubscribe(new MonoFutureSubscription<>(wrapped, subscriptionConsumer));
    }

    @Override
    public @Nullable Object scanUnsafe(@NonNull Scannable.Attr key) {
        if (key == Scannable.Attr.RUN_STYLE) {
            return Scannable.Attr.RunStyle.ASYNC;
        }
        return null;
    }

    static class MonoFutureSubscription<T> implements Scannable, Subscription, Handler<AsyncResult<T>> {
        final CoreSubscriber<? super T> actual;
        final Consumer<Handler<AsyncResult<T>>> subscriptionConsumer;

        volatile int requestedOnce;
        @SuppressWarnings("rawtypes")
        static final AtomicIntegerFieldUpdater<MonoFutureSubscription> REQUESTED_ONCE
            = AtomicIntegerFieldUpdater.newUpdater(MonoFutureSubscription.class, "requestedOnce");

        volatile boolean cancelled;

        volatile Throwable error;
        @SuppressWarnings("rawtypes")
        static final AtomicReferenceFieldUpdater<MonoFutureSubscription, Throwable> ERROR
            = AtomicReferenceFieldUpdater.newUpdater(MonoFutureSubscription.class, Throwable.class, "error");

        MonoFutureSubscription(CoreSubscriber<? super T> actual, Consumer<Handler<AsyncResult<T>>> subscriptionConsumer) {
            this.actual = actual;
            this.subscriptionConsumer = subscriptionConsumer;
        }

        @Override
        public void handle(AsyncResult<T> ar) {
            final CoreSubscriber<? super T> actual = this.actual;

            if (this.cancelled) {
                // nobody is interested in the Mono anymore, don't risk dropping errors
                final Context ctx = actual.currentContext();
                if (ar.succeeded()) {
                    // we discard any potential value and ignore Future cancellations
                    Operators.onDiscard(ar.result(), ctx);
                } else {
                    Throwable e = ar.cause();
                    if (!(e instanceof CancellationException)) {
                        // we make sure we keep _some_ track of a Future failure AFTER the Mono cancellation
                        Operators.onErrorDropped(e, ctx);
                    }
                }
                return;
            }

            // TODO Prevent firing events after 1st handle() invocation.
            try {
                if (ar.failed()) {
                    Throwable e = ar.cause();
//                    if (e instanceof CompletionException) {
//                        xxx(e.getCause());
//                    }
                    signalError(e);
                }
                else {
                    T value = ar.result();
                    if (value != null) {
                        actual.onNext(value);
                    }
                    actual.onComplete();
                }
            } catch (Throwable e1) {
                Operators.onErrorDropped(e1, actual.currentContext());
//                throw Exceptions.bubble(e1);
            }
        }

        @Override
        public void request(long n) {
            if (this.cancelled) {
                return;
            }
            if (this.requestedOnce == 1 || !REQUESTED_ONCE.compareAndSet(this, 0 , 1)) {
                return;
            }
            try {
                subscriptionConsumer.accept(this);
            } catch (Throwable t) {
                signalError(t);
            }
        }

        @Override
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public @Nullable Object scanUnsafe(@NonNull Attr key){
            if (key == Attr.ACTUAL) {
                return this.actual;
            }
            return null;
        }

        private void signalError(Throwable t) {
            if (ERROR.compareAndSet(this, null, t)) {
                actual.onError(t);
            } else {
                Operators.onErrorDropped(t, actual.currentContext());
            }
        }
    }
}