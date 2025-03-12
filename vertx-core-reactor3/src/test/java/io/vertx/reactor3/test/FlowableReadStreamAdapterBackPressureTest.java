package io.vertx.reactor3.test;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.lang.rx.test.TestSubscriber;
import io.vertx.reactor3.core.FluxHelper;
import io.vertx.lang.rx.test.ReadStreamAdapterBackPressureTest;
import io.vertx.test.fakestream.FakeStream;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class FlowableReadStreamAdapterBackPressureTest extends ReadStreamAdapterBackPressureTest<Flux<Buffer>> {

    // TODO
    @Override
    protected long defaultMaxBufferSize() {
        return 10;
    }

    @Override
    protected Flux<Buffer> toObservable(ReadStream<Buffer> stream, int maxBufferSize) {
        return FluxHelper.toFlux(io.vertx.reactor3.core.streams.ReadStream.newInstance(stream));
    }

    @Override
    protected Flux<Buffer> toObservable(ReadStream<Buffer> stream) {
        return FluxHelper.toFlux(io.vertx.reactor3.core.streams.ReadStream.newInstance(stream));
    }

    @Override
    protected Flux<Buffer> flatMap(Flux<Buffer> obs, Function<Buffer, Flux<Buffer>> f) {
        return obs.flatMap(f);
    }

    @Override
    protected void subscribe(Flux<Buffer> obs, TestSubscriber<Buffer> sub) {
        TestUtils.subscribe(obs, sub);
    }

    @Override
    protected Flux<Buffer> concat(Flux<Buffer> obs1, Flux<Buffer> obs2) {
        return Flux.concat(obs1, obs2);
    }

    @Test
    public void testSubscribeTwice() {
        FakeStream<Buffer> stream = new FakeStream<>();
        Flux<Buffer> observable = toObservable(stream);
        TestSubscriber<Buffer> subscriber1 = new TestSubscriber<Buffer>().prefetch(0);
        TestSubscriber<Buffer> subscriber2 = new TestSubscriber<Buffer>().prefetch(0);
        subscribe(observable, subscriber1);
        subscribe(observable, subscriber2);
        subscriber2.assertError(err -> {
            assertTrue(err instanceof IllegalStateException);
        });
        subscriber2.assertEmpty();
    }

    @Test
    public void testHandlerIsSetInDoOnSubscribe() {
        AtomicBoolean handlerSet = new AtomicBoolean();
        FakeStream<Buffer> stream = new FakeStream<Buffer>() {
            @Override
            public FakeStream<Buffer> handler(Handler<Buffer> handler) {
                handlerSet.set(true);
                return super.handler(handler);
            }
        };
        Flux<Buffer> observable = toObservable(stream).doOnSubscribe(disposable -> {
            assertTrue(handlerSet.get());
        });
        TestSubscriber<Buffer> subscriber = new TestSubscriber<>();
        subscribe(observable, subscriber);
        subscriber.assertEmpty();
    }

    @Override
    @Test
    @Ignore
    // TODO
    public void testResubscribe() {
        super.testResubscribe();
    }
}
