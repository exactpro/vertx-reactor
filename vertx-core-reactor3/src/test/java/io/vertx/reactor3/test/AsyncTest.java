package io.vertx.reactor3.test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.reactor3.codegen.reactor3.MethodWithAsync;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class AsyncTest {

    @Test
    @Ignore("This test is not valid for Reactor")
    public void testSingle() {
        List<Handler<AsyncResult<String>>> handlers = Collections.synchronizedList(new ArrayList<>());
        Mono<String> single = MethodWithAsync.singleMethod(handlers::add);
        // In case of Reactor the next assert will be eventually but not immediately true
        assertEquals(1, handlers.size());
        AtomicReference<String> val1 = new AtomicReference<>();
        single.subscribe(val1::set);
        assertEquals(1, handlers.size());
        assertNull(val1.get());
        handlers.get(0).handle(Future.succeededFuture("expected"));
        assertEquals("expected", val1.get());
        AtomicReference<String> val2 = new AtomicReference<>();
        single.subscribe(val2::set);
        assertEquals(1, handlers.size());
        assertEquals("expected", val2.get());
    }

    @Test
    @Ignore("This test is not valid for Reactor")
    public void testLazySingle() {
        List<Handler<AsyncResult<String>>> handlers = Collections.synchronizedList(new ArrayList<>());
        Mono<String> single = MethodWithAsync.rxSingleMethod(handlers::add);
        assertEquals(0, handlers.size());
        AtomicReference<String> val1 = new AtomicReference<>();
        single.subscribe(val1::set);
        assertEquals(1, handlers.size());
        assertNull(val1.get());
        handlers.get(0).handle(Future.succeededFuture("expected1"));
        assertEquals("expected1", val1.get());
        AtomicReference<String> val2 = new AtomicReference<>();
        single.subscribe(val2::set);
        assertEquals(2, handlers.size());
        assertNull(val2.get());
        handlers.get(1).handle(Future.succeededFuture("expected2"));
        assertEquals("expected2", val2.get());
    }
}
