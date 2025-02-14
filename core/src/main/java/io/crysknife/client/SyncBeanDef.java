/*
 * Copyright © 2021 Treblereel
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

package io.crysknife.client;

import io.crysknife.client.internal.BeanFactory;

import java.util.Optional;

/**
 * Represents a bean definition within the bean manager.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface SyncBeanDef<T> extends InstanceFactory<T>, IOCBeanDef<T> {

  /**
   * Returns a new instance of the bean. Calling this method overrides the underlying scope and
   * instantiates a new instance of the bean.
   *
   * @return a new instance of the bean.
   */
  T newInstance();

  Optional<BeanFactory<T>> getFactory();

}
