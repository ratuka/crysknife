/*
 * Copyright © 2020 Treblereel
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

package io.crysknife.validation;

import com.google.auto.common.MoreElements;
import io.crysknife.exception.UnableToCompleteException;
import io.crysknife.generator.context.IOCContext;

import javax.lang.model.element.ExecutableElement;
import java.util.HashSet;
import java.util.Set;

public abstract class Validator<T> {

  private final IOCContext context;

  private Set<Check<T>> checks = new HashSet<>();

  public Validator(IOCContext context) {
    this.context = context;
  }

  protected Validator addCheck(Check<T> check) {
    checks.add(check.setContext(context));
    return this;
  }

  public void validate(T elm) throws UnableToCompleteException {
    for (Check check : checks) {
      check.check(elm);
    }
  }

}
