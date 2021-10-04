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

package org.treblereel.injection.cycle;

import io.crysknife.annotation.CircularDependency;
import io.crysknife.client.ManagedInstance;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 9/15/21
 */
@CircularDependency
@ApplicationScoped
public class AdapterManagerImpl implements AdapterManager {

  public AdapterRegistry registry;

  @Inject
  public RegistryFactory registryFactory;

  @Inject
  public AdapterManagerImpl(RegistryFactory registryFactory) {
    this(new AdapterRegistryImpl());
  }

  AdapterManagerImpl(final AdapterRegistry registry) {
    this.registry = registry;
  }

  @Override
  public AdapterRegistry registry() {
    return registry;
  }
}
