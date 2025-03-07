package io.vertx.reactor3.core;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.impl.WorkerExecutorInternal;
import io.vertx.core.json.JsonObject;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

// TODO Implement shutdown
// TODO Implement Scannable
public class ContextScheduler implements Scheduler {

    private final @NonNull Vertx vertx;
    private final boolean blocking;
    private final boolean ordered;
    private final @Nullable Context context;
    private final @Nullable WorkerExecutor workerExecutor;

    private static final Scheduler.Worker TERMINATED = new TerminatedWorker();

    private volatile @Nullable Scheduler.Worker defaultWorker = null;
    private static final AtomicReferenceFieldUpdater<ContextScheduler, Scheduler.Worker> DEFAULT_WORKER
        = AtomicReferenceFieldUpdater.newUpdater(ContextScheduler.class, Scheduler.Worker.class, "defaultWorker");

    public ContextScheduler(@NonNull Context context, boolean blocking) {
        this(context, blocking, true);
    }

    public ContextScheduler(@NonNull Context context, boolean blocking, boolean ordered) {
        this.vertx = context.owner();
        this.context = context;
        this.blocking = blocking;
        this.ordered = ordered;
        this.workerExecutor = null;
    }

    public ContextScheduler(@NonNull Vertx vertx, boolean blocking) {
        this(vertx, blocking, true);
    }

    public ContextScheduler(@NonNull Vertx vertx, boolean blocking, boolean ordered) {
        this.vertx = vertx;
        this.context = null;
        this.blocking = blocking;
        this.ordered = ordered;
        this.workerExecutor = null;
    }

    public ContextScheduler(@NonNull WorkerExecutor workerExecutor) {
        this(workerExecutor, true);
    }

    public ContextScheduler(@NonNull WorkerExecutor workerExecutor, boolean ordered) {
        Objects.requireNonNull(workerExecutor, "workerExecutor is null");
        this.vertx = ((WorkerExecutorInternal) workerExecutor).vertx();
        this.context = null;
        this.workerExecutor = workerExecutor;
        this.blocking = true;
        this.ordered = ordered;
    }

    @Override
    public @NonNull Disposable schedule(@NonNull Runnable task) {
        return getDefaultWorker().schedule(task);
    }

    @Override
    public @NonNull Disposable schedule(@NonNull Runnable task, long delay, @NonNull TimeUnit unit) {
        return getDefaultWorker().schedule(task, delay, unit);
    }

    @Override
    public @NonNull Disposable schedulePeriodically(
        @NonNull Runnable task, long initialDelay, long period, @NonNull TimeUnit unit
    ) {
        return getDefaultWorker().schedulePeriodically(task, initialDelay, period, unit);
    }

    @Override
    public @NonNull ContextWorker createWorker() {
        return new ContextWorker();
    }

    @Override
    public boolean isDisposed() {
        return defaultWorker == TERMINATED;
    }

    private Scheduler.Worker getDefaultWorker() {
        Scheduler.Worker contextWorker = null;
        for (;;) {
            Scheduler.Worker worker = defaultWorker;
            if (worker == TERMINATED) {
                throw new IllegalStateException(); // TODO
            }
            if (worker != null) {
                return worker;
            }
            if (contextWorker == null) {
                contextWorker = new ContextWorker();
            }
            if (DEFAULT_WORKER.compareAndSet(this, null, contextWorker)) {
                return contextWorker;
            }
        }
    }

    private static class TerminatedWorker implements Scheduler.Worker {
        @Override
        public @NonNull Disposable schedule(@NonNull Runnable task) {
            throw Exceptions.failWithRejected();
        }

        @Override
        public @NonNull Disposable schedule(@NonNull Runnable task, long delay, @NonNull TimeUnit unit) {
            throw Exceptions.failWithRejected();
        }

        @Override
        public @NonNull Disposable schedulePeriodically(
            @NonNull Runnable task, long initialDelay, long period, @NonNull TimeUnit unit
        ) {
            throw Exceptions.failWithRejected();
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isDisposed() {
            return true;
        }
    }

    private static final Object DUMB = new JsonObject();

    public class ContextWorker implements Scheduler.Worker {

        private final ConcurrentHashMap<TimedAction, Object> actions = new ConcurrentHashMap<>();
        private final AtomicBoolean cancelled = new AtomicBoolean();

        public int countActions() {
            return actions.size();
        }

        @Override
        public @NonNull Disposable schedule(@NonNull Runnable action) {
            return schedule(action, 0, TimeUnit.MILLISECONDS);
        }

        @Override
        public @NonNull Disposable schedule(@NonNull Runnable action, long delayTime, @NonNull TimeUnit unit) {
            if (cancelled.get()) {
                throw Exceptions.failWithRejected();
            }
            action = Schedulers.onSchedule(action);
            long delayMillis = unit.toMillis(delayTime);
            TimedAction timed = new TimedAction(action, 0);
            actions.put(timed, DUMB);
            timed.schedule(delayMillis);
            return timed;
        }

        @Override
        public @NonNull Disposable schedulePeriodically(
            @NonNull Runnable action, long initialDelay, long period, @NonNull TimeUnit unit
        ) {
            if (cancelled.get()) {
                throw Exceptions.failWithRejected();
            }
            action = Schedulers.onSchedule(action);
            long delayMillis = unit.toMillis(initialDelay);
            TimedAction timed = new TimedAction(action, unit.toMillis(period));
            actions.put(timed, DUMB);
            timed.schedule(delayMillis);
            return timed;
        }

        @Override
        public void dispose() {
            if (cancelled.compareAndSet(false, true)) {
                actions.keySet().forEach(TimedAction::dispose);
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled.get();
        }

        class TimedAction implements Disposable {

            private long id;
            private final Runnable action;
            private final long periodMillis;
            private boolean disposed;

            TimedAction(Runnable action, long periodMillis) {
                this.disposed = false;
                this.action = action;
                this.periodMillis = periodMillis;
            }

            private synchronized void schedule(long delayMillis) {
                if (delayMillis > 0) {
                    id = vertx.setTimer(delayMillis, this::execute);
                } else {
                    id = -1;
                    execute(null);
                }
            }

            private void execute(Object o) {
                if (workerExecutor != null) {
                    workerExecutor.executeBlocking(Executors.callable(this::run), ordered);
                } else {
                    Context ctx = context != null ? context : vertx.getOrCreateContext();
                    if (blocking) {
                        ctx.executeBlocking(Executors.callable(this::run), ordered);
                    } else {
                        ctx.runOnContext(x -> run());
                    }
                }
            }

            private void run() {
                synchronized (TimedAction.this) {
                    if (disposed) {
                        return;
                    }
                }
                action.run();
                synchronized (TimedAction.this) {
                    if (!disposed) {
                        if (periodMillis > 0) {
                            schedule(periodMillis);
                        } else {
                            disposed = true;
                            actions.remove(this);
                        }
                    }
                }
            }

            @Override
            public synchronized void dispose() {
                if (!disposed) {
                    actions.remove(this);
                    if (id > 0) {
                        vertx.cancelTimer(id);
                    }
                    disposed = true;
                }
            }

            @Override
            public synchronized boolean isDisposed() {
                return disposed;
            }
        }
    }
}