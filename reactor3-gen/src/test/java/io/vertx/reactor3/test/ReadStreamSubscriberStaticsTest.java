package io.vertx.reactor3.test;

import io.vertx.core.streams.ReadStream;
import io.vertx.lang.rx.test.ReadStreamSubscriberStaticsTestBase;
import io.vertx.reactor3.impl.ReadStreamSubscriber;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReadStreamSubscriberStaticsTest extends ReadStreamSubscriberStaticsTestBase<Integer, Flux<Integer>> {

  @Override
  public Flux<Integer> emptyFlowable() {
    return Flux.empty();
  }

  @Override
  public List<Integer> generateData(int count) {
    return IntStream.range(0, count).boxed().collect(Collectors.toList());
  }

  @Override
  public Flux<Integer> flowable(Iterable<Integer> source) {
    return Flux.fromIterable(source);
  }

  @Override
  public ReadStream<Integer> asReadStream(Flux<Integer> flowable) {
    return ReadStreamSubscriber.asReadStream(flowable, Function.identity());
  }

  @Override
  public Flux<Integer> emptyExceptionFlowable(String errorMessage) {
    return Flux.error(new RuntimeException(errorMessage));
  }

  @Override
  public Flux<Integer> exceptionAfterDataFlowable(String errorMessage, Iterable<Integer> source) {
    return flowable(source).concatWith(emptyExceptionFlowable(errorMessage));
  }
}
