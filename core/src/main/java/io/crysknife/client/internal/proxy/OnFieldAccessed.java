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
package io.crysknife.client.internal.proxy;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import elemental2.core.Reflect;
import io.crysknife.client.InstanceFactory;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 1/1/20
 */
public final class OnFieldAccessed implements BiFunction<Object, String, Object> {

  private final Supplier<InstanceFactory> supplier;

  public OnFieldAccessed(Supplier<InstanceFactory> supplier) {
    this.supplier = supplier;
  }

  @Override
  public Object apply(Object o, String propertyKey) {
    if (Reflect.get(o, propertyKey) == null) {
      Reflect.set(o, propertyKey, supplier.get().getInstance());
    }
    return Reflect.get(o, propertyKey);
  }
}
