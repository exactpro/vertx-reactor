package io.vertx.reactor3.core;

import io.vertx.core.*;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public abstract class AbstractVerticle extends io.vertx.core.AbstractVerticle {

    // Shadows the AbstractVerticle#vertx field
    protected io.vertx.reactor3.core.Vertx vertx;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.vertx = new io.vertx.reactor3.core.Vertx(vertx);
    }

    @Override
    public final void start(Promise<Void> startFuture) {
        Mono<Void> mono = reactiveStart();
        mono.subscribe(null, startFuture::fail, startFuture::complete);
    }

    /**
     * Override to return a {@code Mono} that will complete the deployment of this verticle.
     * <p/>
     * Chain with super.reactiveStart() to execute start logic from the base class.
     *
     * @return mono that completes on the deployment of this verticle.
     */
    public @NonNull Mono<Void> reactiveStart() {
        return Reactive.toMono(handler -> {
            Promise<Void> startPromise = Promise.promise();
            startPromise.future().onComplete(handler);
            try {
                super.start(startPromise);
            } catch (Throwable error) {
                handler.handle(Future.failedFuture(error));
            }
        });
    }

    @Override
    public final void stop(Promise<Void> stopFuture) {
        Mono<Void> mono = reactiveStop();
        mono.subscribe(null, stopFuture::fail, stopFuture::complete);
    }

    /**
     * Override to return a {@code Mono} that will complete the undeployment of this verticle.
     * <p/>
     * Chain with super.reactiveStop() to execute stop logic from the base class.
     *
     * @return mono that completes on the undeployment of this verticle.
     */
    public @NonNull Mono<Void> reactiveStop() {
        return Reactive.toMono(handler -> {
            Promise<Void> stopPromise = Promise.promise();
            stopPromise.future().onComplete(handler);
            try {
                super.stop(stopPromise);
            } catch (Throwable error) {
                handler.handle(Future.failedFuture(error));
            }
        });
    }
}