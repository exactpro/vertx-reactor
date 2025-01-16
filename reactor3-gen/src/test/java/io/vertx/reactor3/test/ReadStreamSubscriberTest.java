package io.vertx.reactor3.test;

import io.vertx.reactor3.impl.ReadStreamSubscriber;
import io.vertx.lang.rx.test.ReadStreamSubscriberTestBase;
import reactor.core.publisher.Sinks;

import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ReadStreamSubscriberTest extends ReadStreamSubscriberTestBase {
    @Override
    public long bufferSize() {
        return ReadStreamSubscriber.BUFFER_SIZE;
    }

    @Override
    protected Sender sender() {
        return new Sender() {
            private final Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

            private boolean cancelled;

            {
                stream = new ReadStreamSubscriber<>(
                    Function.identity(),
                    sink.asFlux()
                        .doOnRequest(r -> requested += r)
                        .doOnCancel(() -> cancelled = true)
                );
            }

            @Override
            protected void emit() {
                sink.tryEmitNext("" + seq++);
            }

            @Override
            protected void complete() {
                sink.tryEmitComplete();
            }

            @Override
            protected void fail(Throwable cause) {
                sink.tryEmitError(cause);
            }

            @Override
            protected boolean isUnsubscribed() {
                return cancelled;
            }
        };
    }
}
