package io.vertx.reactor3.test;

import io.vertx.core.Future;
import io.vertx.reactor3.codegen.reactor3.MethodWithFuture;
import org.junit.Test;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class FutureTest {

  @Test
  public void testFuture() {
    io.vertx.codegen.reactor3.MethodWithFuture delegate = new io.vertx.codegen.reactor3.MethodWithFuture() {
    };
    Future<MethodWithFuture> ret = MethodWithFuture.withVertxGen(Mono.just(new MethodWithFuture(delegate)));
    // TODO await
    assertTrue(ret.succeeded());
    assertSame(ret.result().getDelegate(), delegate);
  }
}
