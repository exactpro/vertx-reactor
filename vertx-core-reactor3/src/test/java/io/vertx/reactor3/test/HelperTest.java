package io.vertx.reactor3.test;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.reactor3.core.MonoHelper;
import io.vertx.reactor3.core.FluxHelper;
import io.vertx.reactor3.core.streams.ReadStream;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakestream.FakeStream;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import static java.util.function.Function.identity;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HelperTest extends VertxTestBase {

    @Test
    public void testToFutureSuccess() {
        Mono<String> promise = Mono.just("foobar");
        Future<String> future = MonoHelper.toFuture(promise);
        // TODO Wait for future completion.
        assertTrue(future.succeeded());
        assertEquals("foobar", future.result());
    }

    @Test
    public void testToFutureFailure() {
        Exception err = new Exception();
        Mono<String> mono = Mono.error(err);
        Future<String> future = MonoHelper.toFuture(mono);
        // TODO Wait for future completion.
        assertTrue(future.failed());
        assertSame(err, future.cause());
    }

    @Test
    public void testToMonoSubscriberSuccess() {
        Promise<String> promise = Promise.promise();
        Subscriber<String> subscriber = MonoHelper.toSubscriber(promise);
        Mono<String> s = Mono.just("foobar");
        s.subscribe(subscriber);
        // TODO Wait for completion.
        assertTrue(promise.future().succeeded());
        assertEquals("foobar", promise.future().result());
    }

    @Test
    public void testToMonoSubscriberFailure() {
        Promise<String> promise = Promise.promise();
        Subscriber<String> subscriber = MonoHelper.toSubscriber(promise);
        RuntimeException cause = new RuntimeException();
        Mono<String> mono = Mono.error(cause);
        mono.subscribe(subscriber);
        // TODO Wait for completion.
        assertTrue(promise.future().failed());
        assertSame(cause, promise.future().cause());
    }

    @Test
    public void testToMonoSubscriberEmpty() {
        Promise<String> promise = Promise.promise();
        Subscriber<String> subscriber = MonoHelper.toSubscriber(promise);
        Mono<String> mono = Mono.empty();
        mono.subscribe(subscriber);
        assertTrue(promise.future().succeeded());
        assertNull(promise.future().result());
    }

    @Test
    public void testToFluxAssemblyHook() {
        String hookKey = "9wxCVE0OWwLR";
        FakeStream<String> stream = new FakeStream<>();
        try {
            final Flux<Object> justMe = Flux.just("me");
            Hooks.onEachOperator(hookKey, p -> justMe);
            Flux<String> flowable = FluxHelper.toFlux(ReadStream.newInstance(stream));
            assertSame(flowable, justMe);
            Flux<String> flowableFn = FluxHelper.toFlux(ReadStream.newInstance(stream), identity());
            assertSame(flowableFn, justMe);
//            Flux<String> flowableSize = FluxHelper.toFlux(stream, 1);
//            assertSame(flowableSize, justMe);
        } finally {
            Hooks.resetOnEachOperator(hookKey);
        }
    }
}
