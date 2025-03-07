package io.vertx.reactor3.test;

import io.vertx.codegen.testmodel.AnyJavaTypeTCKImpl;
import io.vertx.reactor3.codegen.testmodel.AnyJavaTypeTCK;
import org.junit.Test;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class AnyJavaTypeTCKTest {

  final AnyJavaTypeTCK obj = new AnyJavaTypeTCK(new AnyJavaTypeTCKImpl());

  @Test
  public void testHandlersWithAsyncResult() {
    List<Socket> socketsRxList = obj.methodWithHandlerAsyncResultListOfJavaTypeParam().block();

    Set<Socket> socketSetRx = obj.methodWithHandlerAsyncResultSetOfJavaTypeParam().block();

    Map<String, Socket> stringSocketMapRx = obj.methodWithHandlerAsyncResultMapOfJavaTypeParam().block();

    assertNotNull(socketsRxList);
    for (Socket socket : socketsRxList) {
      assertFalse(socket.isConnected());
    }

    assertNotNull(socketSetRx);
    for (Socket socket : socketSetRx) {
      assertFalse(socket.isConnected());
    }

    assertNotNull(stringSocketMapRx);
    for (Map.Entry<String, Socket> entry : stringSocketMapRx.entrySet()) {
      assertEquals("1", entry.getKey());
      assertFalse(entry.getValue().isConnected());
    }

    assertEquals(1, socketsRxList.size());
    assertEquals(1, socketSetRx.size());
    assertEquals(1, stringSocketMapRx.size());
  }
}
