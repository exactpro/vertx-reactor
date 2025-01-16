package io.vertx.reactor3.test;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.testmodel.NullableTCKImpl;
import io.vertx.codegen.testmodel.TestInterfaceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.reactor3.codegen.reactor3.MethodWithMultiCallback;
import io.vertx.reactor3.codegen.reactor3.MethodWithNullableTypeVariableParamByVoidArg;
import io.vertx.reactor3.codegen.testmodel.NullableTCK;
import io.vertx.reactor3.codegen.testmodel.TestInterface;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ApiTest {

    @Test
    public void testSingle() {
        TestInterface obj = new TestInterface(new TestInterfaceImpl());
        Mono<String> fut = obj.methodWithHandlerAsyncResultString(false);
        StepVerifier.create(fut)
            .expectNext("quux!")
            .verifyComplete();
    }

    @Test
    public void testCompletable() {
        TestInterface obj = new TestInterface(new TestInterfaceImpl());
        Mono<Void> failure = obj.methodWithHandlerAsyncResultVoid(true);
        StepVerifier.create(failure)
            .verifyError();

        Mono<Void> success = obj.methodWithHandlerAsyncResultVoid(false);
        StepVerifier.create(success)
            .verifyComplete();
    }

    @Test
    public void testMaybe() {
        NullableTCK obj = new NullableTCK(new NullableTCKImpl());

        Mono<String> maybeNotNull = obj.methodWithNullableStringHandlerAsyncResult(true);
        StepVerifier.create(maybeNotNull)
            .expectNext("the_string_value")
            .verifyComplete();

        maybeNotNull = obj.methodWithNullableStringHandlerAsyncResult(false);
        StepVerifier.create(maybeNotNull)
            .verifyComplete();
    }

    @Test
    public void testMultiCompletions() {
        MethodWithMultiCallback objectMethodWithMultiCompletable = MethodWithMultiCallback.newInstance(
            new io.vertx.codegen.reactor3.MethodWithMultiCallback() {
                @Override
                public void multiCompletable(Handler<AsyncResult<Void>> handler) {
                    handler.handle(Future.succeededFuture());
                    handler.handle(Future.succeededFuture());
                }

                @Override
                public void multiMaybe(Handler<AsyncResult<@Nullable String>> handler) {
                    handler.handle(Future.succeededFuture());
                    handler.handle(Future.succeededFuture("foo"));
                }

                @Override
                public void multiSingle(Handler<AsyncResult<String>> handler) {
                    handler.handle(Future.succeededFuture("foo"));
                    handler.handle(Future.succeededFuture("foo"));
                }
            }
        );
        StepVerifier.create(objectMethodWithMultiCompletable.multiCompletable())
            .verifyComplete();
        StepVerifier.create(objectMethodWithMultiCompletable.multiMaybe())
            .verifyComplete();
        StepVerifier.create(objectMethodWithMultiCompletable.multiSingle())
            .expectNext("foo")
            .verifyComplete();
    }

    @Test
    public void testNullableTypeVariableParamByVoidArg() {
        MethodWithNullableTypeVariableParamByVoidArg abc = MethodWithNullableTypeVariableParamByVoidArg.newInstance(
            handler -> handler.handle(Future.succeededFuture())
        );
        StepVerifier.create(abc.doSomethingWithMaybeResult())
            .verifyComplete();
    }
}
