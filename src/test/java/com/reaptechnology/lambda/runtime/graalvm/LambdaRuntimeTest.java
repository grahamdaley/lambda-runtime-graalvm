/**
 * Copyright [2020] FormKiQ Inc. Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License
 * at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reaptechnology.lambda.runtime.graalvm;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.action.ExpectationResponseCallback;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;

/** Unit tests for {@link LambdaRuntime}. */
public class LambdaRuntimeTest {

  /** Lambda Request Id. */
  private static final String REQUEST_ID = "testrequestid";

  /** Server Port. */
  private static final int SERVER_PORT = 8001;

  /** Server Host. */
  private static final String SERVER_HOST = "localhost";

  /** {@link ClientAndServer}. */
  private static ClientAndServer mockServer;

  /** {@link InvocationResponseHandler}. */
  private static InvocationResponseHandler invocationResponseHandler =
      new InvocationResponseHandler();

  /** {@link InvocationNextHandler}. */
  private static InvocationNextHandler invocationNextHandler = new InvocationNextHandler();

  /** before class. */
  @BeforeClass
  public static void beforeClass() {
    mockServer = startClientAndServer(SERVER_PORT);

    add("GET", "/2018-06-01/runtime/invocation/next", invocationNextHandler);
    add(
        "POST",
        "/2018-06-01/runtime/invocation/" + REQUEST_ID + "/response",
        invocationResponseHandler);
    add("POST", "/2018-06-01/runtime/init/error", invocationResponseHandler);
  }

  /** After Class. */
  @AfterClass
  public static void stopServer() {
    mockServer.stop();
  }

  /**
   * Add Url to Mock server.
   *
   * @param method {@link String}
   * @param path {@link String}
   * @param response {@link ExpectationResponseCallback}
   */
  private static void add(
      final String method, final String path, final ExpectationResponseCallback response) {
    mockServer.when(request().withMethod(method).withPath(path)).respond(response);
  }

  /**
   * Create Lambda Environment.
   *
   * @return {@link Map}
   */
  private Map<String, String> createEnv() {
    Map<String, String> env = new HashMap<>();
    env.put("AWS_LAMBDA_RUNTIME_API", SERVER_HOST + ":" + SERVER_PORT);
    env.put("SINGLE_LOOP", "true");
    return env;
  }

  /** before. */
  @Before
  public void before() {
    invocationNextHandler.setResponseContent("test");
  }

  /**
   * Test Main invoke lambda.
   *
   * @throws Exception Exception
   */
  @Test
  public void testInvokeWithStringStringHandler() throws Exception {
    // given
    Map<String, String> env = createEnv();

    // when
    LambdaRuntime.invoke(
        new TestRequestInputStringStringHandler(), env, String.class, String.class);

    // then
    assertEquals("this is a test string", invocationResponseHandler.getResponse());
  }

  /**
   * Test invoke Lambda with {@link TestRequestInputMapVoidHandler}.
   *
   * @throws Exception Exception
   */
  @Test
  public void testInvokeWithMapVoidHandler() throws Exception {
    // given
    invocationNextHandler.setResponseContent("{\"data\":\"test\"}");
    Map<String, String> env = createEnv();

    // when
    LambdaRuntime.invoke(new TestRequestInputMapVoidHandler(), env, Map.class, Void.class);

    // then
    String expected = "";
    assertEquals(expected, invocationResponseHandler.getResponse());
  }

  /**
   * Test invoke Lambda with {@link TestRequestInputStringIntHandler}.
   *
   * @throws Exception Exception
   */
  @Test
  public void testInvokeWithStringIntHandler() throws Exception {
    // given
    Map<String, String> env = createEnv();

    // when
    LambdaRuntime.invoke(new TestRequestInputStringIntHandler(), env, String.class, Integer.class);

    // then
    String expected = "98";
    assertEquals(expected, invocationResponseHandler.getResponse());
  }

  /**
   * Test invoke Lambda with {@link TestRequestInputStringMapHandler}.
   *
   * @throws Exception Exception
   */
  @Test
  public void testInvokeWithStringMapHandler() throws Exception {
    // given
    Map<String, String> env = createEnv();

    // when
    LambdaRuntime.invoke(new TestRequestInputStringMapHandler(), env, String.class, Map.class);

    // then
    String expected = "{\"test\":\"123\"}";
    assertEquals(expected, invocationResponseHandler.getResponse());
  }
}
