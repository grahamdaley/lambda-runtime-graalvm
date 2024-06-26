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

/** Test {@link RequestHandler}, input {@link String} return {@link Integer}. */
public class TestRequestInputStringIntHandler implements RequestHandler<String, Integer> {
  /**
   * An int to return.
   *
   * @see #handleRequest(String, Context)
   * @see #handleRequest(String, Context)
   */
  public static final int AN_INT = 98;

  @Override
  public Integer handleRequest(final String input, final Context context) {
    return AN_INT;
  }
}
