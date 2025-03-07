package io.vertx.codegen.reactor3;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface MethodWithFuture {

    static <T> boolean isSucceeded(Future<T> future) {
        awaitCompletion(future);
        return future.succeeded();
    }

    static <T> boolean isFailed(Future<T> future) {
        awaitCompletion(future);
        return future.failed();
    }

    static <T> boolean isComplete(Future<T> future) {
        return future.isComplete();
    }

    static <T> T getResult(Future<T> future) {
        awaitCompletion(future);
        return future.result();
    }

    static <T> Throwable getCause(Future<T> future) {
        awaitCompletion(future);
        return future.cause();
    }

    static Future<MethodWithFuture> withVertxGen(Future<MethodWithFuture> future) {
        return future;
    }

    @GenIgnore
    static void awaitCompletion(Future<?> future) {
        try {
            future.toCompletionStage().toCompletableFuture().join();
        } catch (CancellationException | CompletionException ex) {
            // Completion result supposed to be checked via Future methods.
        }
    }
}
