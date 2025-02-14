/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.crysknife.ui.validation.client.dynamic;

import org.gwtproject.regexp.shared.MatchResult;
import org.gwtproject.regexp.shared.RegExp;

import java.util.Map;

import javax.validation.Constraint;

public class DynamicValidatorUtil {

  private DynamicValidatorUtil() {};

  private static final RegExp messageParamsExp = RegExp.compile("{([A-Za-z]+)}");

  /**
   * Interpolates a constraint violation message.
   * 
   * @param parameters A map of values for all properties for the {@link Constraint} associated with
   *        this validator.
   * @param template the message template.
   * @return the interpolated message, or an empty String if template is null.
   */
  public static String interpolateMessage(Map<String, Object> parameters, String template) {
    if (template == null) {
      return "";
    }

    MatchResult result;
    String message = template;

    while ((result = messageParamsExp.exec(message)) != null) {
      final String name = result.getGroup(1);
      final Object value = parameters.get(name);
      final String strValue = (value != null) ? value.toString() : "";
      message = message.replaceAll("{" + name + "}", strValue);
    }

    return message;
  }
}
