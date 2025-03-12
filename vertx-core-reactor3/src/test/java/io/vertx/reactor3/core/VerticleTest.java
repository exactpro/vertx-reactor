package io.vertx.reactor3.core;

import io.vertx.test.core.VertxTestBase;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

public class VerticleTest extends VertxTestBase {

    private static final RuntimeException err = new RuntimeException();
    private static final AtomicInteger startedCount = new AtomicInteger();
    private static final AtomicInteger stoppedCount = new AtomicInteger();

    private Vertx vertx;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        vertx = new Vertx(super.vertx);
        startedCount.set(0);
        stoppedCount.set(0);
    }

    public static class StartVerticle extends AbstractVerticle {
        @Override
        public @NonNull Mono<Void> reactiveStart() {
            return Mono.empty();
        }
    }

    public static class StartVerticleWithFailure extends AbstractVerticle {
        @Override
        public @NonNull Mono<Void> reactiveStart() {
            throw err;
        }
    }

    public static class StartVerticleWithError extends AbstractVerticle {
        @Override
        public @NonNull Mono<Void> reactiveStart() {
            return Mono.error(err);
        }
    }

    public static class StartVerticleSynchronously extends AbstractVerticle {
        @Override
        public void start() {
            startedCount.incrementAndGet();
        }
    }

    @Test
    public void testStart() {
        vertx.deployVerticle(StartVerticle.class.getName())
            .subscribe(
                id -> vertx.undeploy(id).subscribe(x -> fail(), this::fail, this::testComplete),
                this::fail
            );
        await();
    }

    @Test
    public void testStartWithFailure() {
        vertx.deployVerticle(StartVerticleWithFailure.class.getName())
            .subscribe(
                id -> fail(),
                t -> {
                    assertSame(t, err);
                    testComplete();
                }
            );
        await();
    }

    @Test
    public void testStartWithError() {
        vertx.deployVerticle(StartVerticleWithError.class.getName())
            .subscribe(
                id -> fail(),
                t -> {
                    assertSame(t, err);
                    testComplete();
                }
            );
        await();
    }

    @Test
    public void testStartSynchronously() {
        vertx.deployVerticle(StartVerticleSynchronously.class.getName())
            .subscribe(
                id -> {
                    assertEquals(1, startedCount.get());
                    vertx.undeploy(id).subscribe(x -> fail(), this::fail, this::testComplete);
                },
                this::fail
            );
        await();
    }

    public static class StopVerticle extends AbstractVerticle {
        @Override
        public @NonNull Mono<Void> reactiveStop() {
            return Mono.empty();
        }
    }

    public static class StopVerticleWithFailure extends AbstractVerticle {
        @Override
        public @NonNull Mono<Void> reactiveStop() {
            throw err;
        }
    }

    public static class StopVerticleWithError extends AbstractVerticle {
        @Override
        public @NonNull Mono<Void> reactiveStop() {
            return Mono.error(err);
        }
    }

    public static class StopVerticleSynchronously extends AbstractVerticle {
        @Override
        public void stop() {
            stoppedCount.incrementAndGet();
        }
    }

    @Test
    public void testStop() {
        vertx.deployVerticle(StopVerticle.class.getName())
            .subscribe(
                id -> vertx.undeploy(id).subscribe(x -> fail(), this::fail, this::testComplete),
                this::fail
            );
        await();
    }

    @Test
    public void testStopWithFailure() {
        vertx.deployVerticle(StopVerticleWithFailure.class.getName())
            .subscribe(
                id -> vertx.undeploy(id).subscribe(
                    x -> fail(),
                    t -> {
                        assertSame(t, err);
                        testComplete();
                    },
                    this::fail
                ),
                this::fail
            );
        await();
    }

    @Test
    public void testStopWithError() {
        vertx.deployVerticle(StopVerticleWithError.class.getName())
            .subscribe(
                id -> vertx.undeploy(id).subscribe(
                    x -> fail(),
                    t -> {
                        assertSame(t, err);
                        testComplete();
                    },
                    this::fail
                ),
                this::fail
            );
        await();
    }

    @Test
    public void testStopSynchronously() {
        vertx.deployVerticle(StopVerticleSynchronously.class.getName())
            .subscribe(
                id -> vertx.undeploy(id).subscribe(
                    x -> fail(),
                    this::fail,
                    () -> {
                        assertEquals(1, stoppedCount.get());
                        testComplete();
                    }
                ),
                this::fail
            );
        await();
    }
}