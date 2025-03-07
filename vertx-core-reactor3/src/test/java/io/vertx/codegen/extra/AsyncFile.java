package io.vertx.codegen.extra;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

@VertxGen
public interface AsyncFile extends ReadStream<Foo>, WriteStream<Foo> {
    @Override
    AsyncFile exceptionHandler(@Nullable Handler<Throwable> handler);

    @Override
    AsyncFile setWriteQueueMaxSize(int maxSize);

    @Override
    AsyncFile drainHandler(@Nullable Handler<Void> handler);

    @Override
    AsyncFile handler(@Nullable Handler<Foo> handler);

    @Override
    AsyncFile pause();

    @Override
    AsyncFile resume();

    @Override
    AsyncFile fetch(long amount);

    @Override
    AsyncFile endHandler(@Nullable Handler<Void> endHandler);
}
