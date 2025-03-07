package io.vertx.reactor3.test;

import io.vertx.reactor3.codegen.reactor3.MethodWithCompletable;
import io.vertx.reactor3.codegen.reactor3.MethodWithMaybeString;
import io.vertx.reactor3.codegen.reactor3.MethodWithSingleString;
import org.junit.Assert;
import org.junit.Test;
import reactor.test.StepVerifier;

public class AsyncResultAdapterTest {
    @Test
    public void testSingleReportingSubscribeUncheckedException() {
        RuntimeException cause = new RuntimeException();
        MethodWithSingleString meth = new MethodWithSingleString(handler -> {
            throw cause;
        });
        StepVerifier.create(meth.doSomethingWithResult())
            .verifyErrorSatisfies(err -> Assert.assertSame(cause, err));
    }

    @Test
    public void testMaybeReportingSubscribeUncheckedException() {
        RuntimeException cause = new RuntimeException();
        MethodWithMaybeString meth = new MethodWithMaybeString(handler -> {
            throw cause;
        });
        StepVerifier.create(meth.doSomethingWithMaybeResult())
            .verifyErrorSatisfies(err -> Assert.assertSame(cause, err));
    }

    @Test
    public void testCompletableReportingSubscribeUncheckedException() {
        RuntimeException cause = new RuntimeException();
        MethodWithCompletable meth = new MethodWithCompletable(handler -> {
            throw cause;
        });
        StepVerifier.create(meth.doSomethingWithResult())
            .verifyErrorSatisfies(err -> Assert.assertSame(cause, err));
    }
}
