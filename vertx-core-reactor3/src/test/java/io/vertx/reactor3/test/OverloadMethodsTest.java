package io.vertx.reactor3.test;

import io.vertx.test.core.VertxTestBase;
import org.junit.Ignore;
import org.junit.Test;

import io.vertx.reactor3.codegen.reactor3.MethodWithFuture;
import io.vertx.reactor3.codegen.reactor3.MethodWithFunction;
import reactor.core.publisher.Mono;

import java.util.function.Function;


public class OverloadMethodsTest extends VertxTestBase {

  @Test
  public void testSingleSuccess() {
    Mono<String> mon = Mono.just("foobar");
    assertTrue(MethodWithFuture.isSucceeded(mon));
    assertEquals("foobar", MethodWithFuture.getResult(mon));
  }

  @Test
  public void testSingleFailure() {
    Throwable error = new Throwable();
    Mono<String> mono = Mono.error(error);
    assertTrue(MethodWithFuture.isFailed(mono));
    assertEquals(error, MethodWithFuture.getCause(mono));
  }

  @Test
  public void testFunctionReturningSingleSuccess() {
    Function<String, Mono<Integer>> strLen = s -> Mono.just(s).map(String::length);
    assertTrue(MethodWithFunction.isSucceeded("foobar", strLen));
    assertEquals(6, (int) MethodWithFunction.getResult("foobar", strLen));
  }

  @Test
  public void testFunctionReturningSingleFailure() {
    Throwable error = new Throwable();
    Function<String, Mono<Integer>> strLen = s -> Mono.<String>error(error).map(String::length);
    assertTrue(MethodWithFunction.isFailed("foobar", strLen));
    assertEquals(error, MethodWithFunction.getCause("foobar", strLen));
  }

  @Ignore
  @Test
  public void testFunctionReturningSingleFunctionFailure() {
    RuntimeException error = new RuntimeException();
    Function<String, Mono<Integer>> strLen = s -> {
      throw error;
    };
    assertTrue(MethodWithFunction.isFailed("foobar", strLen));
    assertEquals(error, MethodWithFunction.getCause("foobar", strLen));
  }
}
