package io.vertx.reactor3.test;

import io.vertx.codegen.testmodel.JsonMapperTCKImpl;
import io.vertx.reactor3.codegen.testmodel.JsonMapperTCK;
import org.junit.Test;

public class JsonMapperTCKTest {

    @Test
    public void testInteger() {
        JsonMapperTCKImpl impl = new JsonMapperTCKImpl(); // Impl has asserts! So i reuse the same
        JsonMapperTCK test = JsonMapperTCK.newInstance(impl);
        impl.methodWithTypeToIntegerParam(
            test.methodWithHandlerAsyncResultTypeToIntegerParam().block()
        );

        impl.methodWithListOfTypeToIntegerParam(
            test.methodWithHandlerAsyncResultListOfTypeToIntegerParam().block()
        );

        impl.methodWithSetOfTypeToIntegerParam(
            test.methodWithHandlerAsyncResultSetOfTypeToIntegerParam().block()
        );

        impl.methodWithMapOfTypeToIntegerParam(
            test.methodWithHandlerAsyncResultMapOfTypeToIntegerParam().block()
        );
    }

    @Test
    public void testString() {
        JsonMapperTCKImpl impl = new JsonMapperTCKImpl(); // Impl has asserts! So i reuse the same
        JsonMapperTCK test = JsonMapperTCK.newInstance(impl);
        impl.methodWithTypeToStringParam(
            test.methodWithHandlerAsyncResultTypeToStringParam().block()
        );

        impl.methodWithListOfTypeToStringParam(
            test.methodWithHandlerAsyncResultListOfTypeToStringParam().block()
        );

        impl.methodWithSetOfTypeToStringParam(
            test.methodWithHandlerAsyncResultSetOfTypeToStringParam().block()
        );

        impl.methodWithMapOfTypeToStringParam(
            test.methodWithHandlerAsyncResultMapOfTypeToStringParam().block()
        );
    }

    @Test
    public void testJsonArray() {
        JsonMapperTCKImpl impl = new JsonMapperTCKImpl(); // Impl has asserts! So I reuse the same
        JsonMapperTCK test = JsonMapperTCK.newInstance(impl);
        impl.methodWithTypeToJsonArrayParam(
            test.methodWithHandlerAsyncResultTypeToJsonArrayParam().block()
        );

        impl.methodWithListOfTypeToJsonArrayParam(
            test.methodWithHandlerAsyncResultListOfTypeToJsonArrayParam().block()
        );

        impl.methodWithSetOfTypeToJsonArrayParam(
            test.methodWithHandlerAsyncResultSetOfTypeToJsonArrayParam().block()
        );

        impl.methodWithMapOfTypeToJsonArrayParam(
            test.methodWithHandlerAsyncResultMapOfTypeToJsonArrayParam().block()
        );
    }

    @Test
    public void testJsonObject() {
        JsonMapperTCKImpl impl = new JsonMapperTCKImpl(); // Impl has asserts! So i reuse the same
        JsonMapperTCK test = JsonMapperTCK.newInstance(impl);
        impl.methodWithTypeToJsonObjectParam(
            test.methodWithHandlerAsyncResultTypeToJsonObjectParam().block()
        );

        impl.methodWithListOfTypeToJsonObjectParam(
            test.methodWithHandlerAsyncResultListOfTypeToJsonObjectParam().block()
        );

        impl.methodWithSetOfTypeToJsonObjectParam(
            test.methodWithHandlerAsyncResultSetOfTypeToJsonObjectParam().block()
        );

        impl.methodWithMapOfTypeToJsonObjectParam(
            test.methodWithHandlerAsyncResultMapOfTypeToJsonObjectParam().block()
        );
    }

    @Test
    public void testEnumCustom() {
        JsonMapperTCKImpl impl = new JsonMapperTCKImpl(); // Impl has asserts! So i reuse the same
        JsonMapperTCK test = JsonMapperTCK.newInstance(impl);
        impl.methodWithCustomEnumToStringParam(
            test.methodWithHandlerAsyncResultCustomEnumToStringParam().block()
        );
        impl.methodWithListOfCustomEnumToStringParam(
            test.methodWithHandlerAsyncResultListOfCustomEnumToStringParam().block()
        );
        impl.methodWithSetOfCustomEnumToStringParam(
            test.methodWithHandlerAsyncResultSetOfCustomEnumToStringParam().block()
        );
        impl.methodWithMapOfCustomEnumToStringParam(
            test.methodWithHandlerAsyncResultMapOfCustomEnumToStringParam().block()
        );

    }
}
