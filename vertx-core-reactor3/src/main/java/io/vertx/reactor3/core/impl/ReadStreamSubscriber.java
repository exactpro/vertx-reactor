package io.vertx.reactor3.core.impl;

import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.util.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.function.Function;

/**
 * An RxJava {@code Subscriber} that turns an {@code Observable} into a {@link ReadStream}.
 * <p>
 * The stream implements the {@link #pause()} and {@link #resume()} operation by maintaining
 * a buffer of {@link #BUFFER_SIZE} elements between the {@code Observable} and the {@code ReadStream}.
 * <p>
 * When the subscriber is created it requests {@code 0} elements to activate the subscriber's back-pressure.
 * Setting the handler initially on the {@code ReadStream} triggers a request of {@link #BUFFER_SIZE} elements.
 * When the item buffer is half empty, new elements are requested to fill the buffer back to {@link #BUFFER_SIZE}
 * elements.
 * <p>
 * The {@link #endHandler(Handler<Void>)} is called when the {@code Observable} is completed or has failed and
 * no pending elements, emitted before the completion or failure, are still in the buffer, i.e the handler
 * is not called when the stream is paused.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ReadStreamSubscriber<R, J> implements CoreSubscriber<R>, ReadStream<J> {

    private static final Runnable NOOP_ACTION = () -> { };
    private static final Throwable DONE_SENTINEL = new Throwable();

    public static final int BUFFER_SIZE = 16;

    public static <R, J> ReadStream<J> asReadStream(Flux<R> flux, Function<R, J> adapter) {
        return new ReadStreamSubscriber<>(adapter, flux);
    }

    private final Function<R, J> adapter;
    private final CorePublisher<R> publisher;
    private Handler<Void> endHandler;
    private Handler<Throwable> exceptionHandler;
    private Handler<J> elementHandler;
    private long demand = Long.MAX_VALUE;
    private Throwable completed;
    private final ArrayDeque<R> pending = new ArrayDeque<>();
    private int requested = 0;
    private Subscription subscription;
    private boolean emitting;

    public ReadStreamSubscriber(Function<R, J> adapter, CorePublisher<R> publisher) {
        this.adapter = adapter;
        this.publisher = publisher;
    }

    @Override
    public ReadStream<J> handler(Handler<J> handler) {
        Runnable action;
        synchronized (this) {
            elementHandler = handler;
            if (handler != null) {
                // FIXME What if we're already subscribed (handler change)?
                action = () -> publisher.subscribe(this);
            } else {
                Subscription s = subscription;
                action = s != null ? s::cancel : NOOP_ACTION;
            }
        }
        action.run();
        serializedCheckStatus();
        return this;
    }

    @Override
    public ReadStream<J> pause() {
        synchronized (this) {
            demand = 0L;
        }
        return this;
    }

    @Override
    public ReadStream<J> fetch(long amount) {
        if (amount < 0L) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }
        // TODO Use helper from Project Reactor
        synchronized (this) {
            demand += amount;
            if (demand < 0L) {
                demand = Long.MAX_VALUE;
            }
        }
        serializedCheckStatus();
        return this;
    }

    @Override
    public ReadStream<J> resume() {
        return fetch(Long.MAX_VALUE);
    }

    @Override
    public void onSubscribe(@NonNull Subscription s) {
        synchronized (this) {
            subscription = s;
        }
        serializedCheckStatus();
    }

    /** Ensure checkStatus is never called concurrently. */
    private void serializedCheckStatus() {
        synchronized (this) {
            if (emitting) {
                return;
            }
            emitting = true;
        }
        try {
            checkStatus();
        } finally {
            synchronized (this) {
                emitting = false;
            }
        }
    }

    private void checkStatus() {
        Runnable action = NOOP_ACTION;
        while (true) {
            J adapted;
            Handler<J> handler;
            synchronized (this) {
                if (demand > 0L && (handler = elementHandler) != null && !pending.isEmpty()) {
                    if (demand != Long.MAX_VALUE) {
                        demand--;
                    }
                    requested--;
                    R item = pending.poll();
                    adapted = adapter.apply(item);
                } else {
                    if (completed != null) {
                        if (pending.isEmpty()) {
                            Handler<Throwable> onError;
                            Throwable result;
                            if (completed != DONE_SENTINEL) {
                                onError = exceptionHandler;
                                result = completed;
                                exceptionHandler = null;
                            } else {
                                onError = null;
                                result = null;
                            }
                            Handler<Void> onCompleted = endHandler;
                            endHandler = null;
                            action = () -> {
                                try {
                                    if (onError != null) {
                                        onError.handle(result);
                                    }
                                } finally {
                                    if (onCompleted != null) {
                                        onCompleted.handle(null);
                                    }
                                }
                            };
                        }
                    } else if (elementHandler != null && requested < BUFFER_SIZE / 2) {
                        int request = BUFFER_SIZE - requested;
                        action = () -> subscription.request(request);
                        requested = BUFFER_SIZE;
                    }
                    break;
                }
            }
            handler.handle(adapted);
        }
        action.run();
    }

    @Override
    public ReadStream<J> endHandler(Handler<Void> handler) {
        synchronized (this) {
            if (completed == null || !pending.isEmpty()) {
                endHandler = handler;
            } else {
                if (handler != null) {
                    throw new IllegalStateException();
                }
            }
        }
        return this;
    }

    @Override
    public ReadStream<J> exceptionHandler(Handler<Throwable> handler) {
        synchronized (this) {
            if (completed == null || !pending.isEmpty()) {
                exceptionHandler = handler;
            } else {
                if (handler != null) {
                    throw new IllegalStateException();
                }
            }
        }
        return this;
    }

    @Override
    public void onComplete() {
        onError(DONE_SENTINEL);
    }

    @Override
    public void onError(Throwable e) {
        synchronized (this) {
            if (completed != null) {
                return;
            }
            completed = e;
        }
        serializedCheckStatus();
    }

    @Override
    public void onNext(R item) {
        synchronized (this) {
            pending.add(item);
        }
        serializedCheckStatus();
    }
}