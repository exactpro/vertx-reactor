package io.vertx.reactor3.test;

import io.vertx.lang.rx.test.TestSubscriber;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class TestUtils {
  public static <T>  void subscribe(Flux<T> obs, TestSubscriber<T> sub) {
    obs.subscribe(new Subscriber<T>() {
      volatile boolean unsubscribed;

      @Override
      public void onSubscribe(Subscription s) {
        sub.onSubscribe(new TestSubscriber.Subscription() {
          @Override
          public void fetch(long val) {
            if (val > 0) {
              s.request(val);
            }
          }

          @Override
          public void unsubscribe() {
            unsubscribed = true;
            s.cancel();
          }

          @Override
          public boolean isUnsubscribed() {
            return unsubscribed;
          }
        });

      }

      @Override
      public void onNext(T buffer) {
        sub.onNext(buffer);
      }

      @Override
      public void onError(Throwable t) {
        unsubscribed = true;
        sub.onError(t);
      }

      @Override
      public void onComplete() {
        unsubscribed = true;
        sub.onCompleted();
      }
    });
  }

  public static <T> void subscribe(Mono<T> obs, TestSubscriber<T> sub) {
    obs.subscribe(sub::onNext, sub::onError, sub::onCompleted);
  }
}
