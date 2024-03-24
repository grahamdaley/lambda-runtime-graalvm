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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/** Wrapper for the AWS Lambda Runtime. */
public class LambdaRuntime {

  /** Lambda Version. */
  private static final String LAMBDA_VERSION_DATE = "2018-06-01";

  /** Lambda Runtime URL. */
  private static final String LAMBDA_RUNTIME_URL_TEMPLATE =
      "http://{0}/{1}/runtime/invocation/next";

  /** Lambda Runtime Invocation URL. */
  private static final String LAMBDA_INVOCATION_URL_TEMPLATE =
      "http://{0}/{1}/runtime/invocation/{2}/response";

  /** Lambda Error Inocation Url. */
  private static final String LAMBDA_ERROR_URL_TEMPLATE =
      "http://{0}/{1}/runtime/invocation/{2}/error";

  /** Error Response Template. */
  private static final String ERROR_RESPONSE_TEMPLATE =
      "'{'\"errorMessage\":\"{0}\",\"errorType\":\"{1}\"'}'";

  /**
   * Main entry point for statically-linked handler.
   *
   * @param handler {@link RequestHandler}
   * @param inClass {@link Class}
   * @param outClass {@link Class}
   * @throws IOException IOException
   */
  @SuppressWarnings("rawtypes")
  public static void invoke(
      final RequestHandler handler, final Class<?> inClass, final Class<?> outClass)
      throws IOException {
    invoke(handler, envToMap(), inClass, outClass);
  }

  /**
   * Alternative entry point with handler and environment variables.
   *
   * @param handler {@link RequestHandler}
   * @param env {@link Map}
   * @param inClass {@link Class}
   * @param outClass {@link Class}
   * @throws IOException IOException
   */
  @SuppressWarnings("rawtypes")
  public static void invoke(
      final RequestHandler handler,
      final Map<String, String> env,
      final Class<?> inClass,
      final Class<?> outClass)
      throws IOException {
    String runtimeApi = env.get("AWS_LAMBDA_RUNTIME_API");

    String runtimeUrl =
        runtimeApi != null
            ? MessageFormat.format(LAMBDA_RUNTIME_URL_TEMPLATE, runtimeApi, LAMBDA_VERSION_DATE)
            : null;

    // Main event loop
    do {
      // Get next Lambda Event
      Context context = null;
      String eventBody = null;
      String requestId = UUID.randomUUID().toString();

      if (runtimeUrl != null) {
        HttpResponse event = HttpClient.get(runtimeUrl);
        requestId = event.getHeaderValue("Lambda-Runtime-Aws-Request-Id");

        String xamazTraceId = event.getHeaderValue("Lambda-Runtime-Trace-Id");
        if (xamazTraceId != null) {
          System.setProperty("com.amazonaws.xray.traceHeader", xamazTraceId);
        }

        context = new LambdaContext(requestId);
        eventBody = event.getBody();
      }

      try {
        String result = invokeLambdaRequestHandler(handler, context, eventBody, inClass, outClass);
        if (runtimeApi != null) {

          // Post the results of Handler Invocation
          String invocationUrl =
              MessageFormat.format(
                  LAMBDA_INVOCATION_URL_TEMPLATE, runtimeApi, LAMBDA_VERSION_DATE, requestId);
          HttpClient.post(invocationUrl, result);
        } else {
          context.getLogger().log(result);
        }
      } catch (Exception e) {
        handleInvocationException(env, requestId, e, context);
      }

    } while (!"true".equals(env.getOrDefault("SINGLE_LOOP", "false")));
  }

  /**
   * Invoke Lambda method.
   *
   * @param handler {@link Object}
   * @param context {@link Context}
   * @param payload {@link String}
   * @param inClass {@link Class}
   * @param outClass {@link Class}
   * @return {@link String}
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static String invokeLambdaRequestHandler(
      final RequestHandler handler,
      final Context context,
      final String payload,
      final Class<?> inClass,
      final Class<?> outClass) {
    Gson gson = new GsonBuilder().create();
    Object input = gson.fromJson(payload, inClass);
    Object value = handler.handleRequest(input, context);

    String val = "";
    if (value != null) {
      if (String.class.equals(outClass)) {
        val = value.toString();
      } else {
        val = gson.toJson(value);
      }
    }

    return val;
  }

  /**
   * Pull environment variables and properties into a single map.
   *
   * @return {@link Map}
   */
  private static Map<String, String> envToMap() {
    Map<String, String> env = new HashMap<>(System.getenv());

    for (Entry<Object, Object> e : System.getProperties().entrySet()) {
      env.put(e.getKey().toString(), e.getValue().toString());
    }

    if (!env.containsKey("AWS_LAMBDA_RUNTIME_API")) {
      env.put("SINGLE_LOOP", "true");
    }

    return env;
  }

  /**
   * Handle Lambda Invocation Errors.
   *
   * @param env {@link Map}
   * @param requestId {@link String}
   * @param ex {@link Exception}
   * @param context {@link Context}
   */
  private static void handleInvocationException(
      final Map<String, String> env,
      final String requestId,
      final Exception ex,
      final Context context) {
    ex.printStackTrace();

    String runtimeApi = env.get("AWS_LAMBDA_RUNTIME_API");

    if (runtimeApi != null) {
      String initErrorUrl =
          MessageFormat.format(
              LAMBDA_ERROR_URL_TEMPLATE, runtimeApi, LAMBDA_VERSION_DATE, requestId);

      String error =
          MessageFormat.format(ERROR_RESPONSE_TEMPLATE, "Invocation Error", "RuntimeError");

      try {
        HttpClient.post(initErrorUrl, error);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
