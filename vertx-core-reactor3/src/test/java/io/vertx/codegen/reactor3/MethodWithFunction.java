package io.vertx.codegen.reactor3;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

import java.util.function.Function;

import static io.vertx.codegen.reactor3.MethodWithFuture.awaitCompletion;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface MethodWithFunction {

    static <T, R> boolean isSucceeded(T t, Function<T, Future<R>> future) {
        Future<R> fut = future.apply(t);
        awaitCompletion(fut);
        return fut.succeeded();
    }

    static <T, R> boolean isFailed(T t, Function<T, Future<R>> future) {
        Future<R> fut = future.apply(t);
        awaitCompletion(fut);
        return fut.failed();
    }

    static <T, R> boolean isComplete(T t, Function<T, Future<R>> future) {
        Future<R> fut = future.apply(t);
        return fut.isComplete();
    }

    static <T, R> R getResult(T t, Function<T, Future<R>> future) {
        Future<R> fut = future.apply(t);
        awaitCompletion(fut);
        return fut.result();
    }

    static <T, R> Throwable getCause(T t, Function<T, Future<R>> future) {
        Future<R> fut = future.apply(t);
        awaitCompletion(fut);
        return fut.cause();
    }
}
