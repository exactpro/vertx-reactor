package io.vertx.reactor3.core.impl;

import io.vertx.core.streams.ReadStream;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Operators;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;

public class FluxReadStream<T> extends Flux<T> implements Scannable {
    private final ReadStream<T> stream;

    volatile int once;
    @SuppressWarnings("rawtypes")
    static final AtomicIntegerFieldUpdater<FluxReadStream> ONCE
        = AtomicIntegerFieldUpdater.newUpdater(FluxReadStream.class, "once");

    public static <T> Flux<T> fromStream(@NonNull ReadStream<T> stream) {
        return onAssembly(new FluxReadStream<>(stream));
    }

    private FluxReadStream(@NonNull ReadStream<T> stream) {
        stream.pause();
        this.stream = stream;
    }

    @Override
    public void subscribe(@NonNull CoreSubscriber<? super T> actual) {
        if (once == 0 && ONCE.compareAndSet(this, 0, 1)) {
            // TODO Replicate Reactor's code?
//            CoreSubscriber<? super T> wrapped = Operators.restoreContextOnSubscriberIfAutoCPEnabled(this, actual);
            CoreSubscriber<? super T> wrapped = actual;
            wrapped.onSubscribe(new ReadStreamSubscription<>(wrapped, stream));
        } else {
            Operators.error(actual, new IllegalStateException("FluxReadStream allows only one Subscriber"));
        }
    }

    @Override
    public @Nullable Object scanUnsafe(@NonNull Scannable.Attr key) {
        if (key == Scannable.Attr.RUN_STYLE) {
            return Scannable.Attr.RunStyle.ASYNC;
        }
        return null;
    }

    private static class ReadStreamSubscription<T> implements Scannable, Subscription {
        private final CoreSubscriber<? super T> actual;
        private final ReadStream<T> stream;

        volatile int done = 0;
        @SuppressWarnings("rawtypes")
        static final AtomicIntegerFieldUpdater<ReadStreamSubscription> DONE =
            AtomicIntegerFieldUpdater.newUpdater(ReadStreamSubscription.class, "done");

        volatile boolean cancelled = false;

        private final AtomicReference<Subscription> current = new AtomicReference<>();

        ReadStreamSubscription(CoreSubscriber<? super T> actual, ReadStream<T> stream) {
            this.actual = actual;
            this.stream = stream;

            stream.endHandler(this::onEnd);
            stream.exceptionHandler(this::onException);
            stream.handler(this::onNext);
        }

        void onNext(T t) {
            if (cancelled) {
                Operators.onNextDropped(t, actual.currentContext());
                return;
            }
            actual.onNext(t);
        }

        void onException(Throwable t) {
            if (cancelled) {
                Operators.onErrorDropped(t, actual.currentContext());
                return;
            }
            release();
            actual.onError(t);
        }

        void onEnd(Void v) {
            if (cancelled) {
                return;
            }
            release();
            actual.onComplete();
        }

        @Override
        public void request(long n) {
            if (cancelled) {
                return;
            }
            stream.fetch(n);
        }

        @Override
        public void cancel() {
            cancelled = true;
            release();
        }

        private void release() {
            if (done == 0 && DONE.compareAndSet(this, 0, 1)) {
                try {
                    stream.exceptionHandler(null);
                    stream.endHandler(null);
                    stream.handler(null);
                } catch (Exception ignore) {
//                    } finally {
//                        try {
//                            stream.resume();
//                        } catch (Exception ignore) {
//                        }
//                    }
                }
            }
        }

        @Override
        public @Nullable Object scanUnsafe(@NonNull Attr key) {
            if (key == Attr.ACTUAL) {
                return this.actual;
            }
            return null;
        }
    }
}