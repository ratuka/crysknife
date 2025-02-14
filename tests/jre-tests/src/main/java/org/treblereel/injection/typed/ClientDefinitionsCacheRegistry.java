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

package org.treblereel.injection.typed;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.HashMap;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 10/9/21
 */
@ApplicationScoped
public class ClientDefinitionsCacheRegistry {

  private final DefaultDefinitionsCacheRegistry definitionsCacheRegistry;

  // CDI Proxy.
  public ClientDefinitionsCacheRegistry() {
    this.definitionsCacheRegistry = null;
  }

  @Inject
  public ClientDefinitionsCacheRegistry(
      final DefaultDefinitionsCacheRegistry definitionsCacheRegistry) {
    this.definitionsCacheRegistry = definitionsCacheRegistry;
  }

  @PostConstruct
  public void init() {
    definitionsCacheRegistry.useStorage(HashMap::new);
  }

  @Produces
  @ApplicationScoped
  public DefinitionsCacheRegistry getRegistry() {
    return definitionsCacheRegistry;
  }
}

