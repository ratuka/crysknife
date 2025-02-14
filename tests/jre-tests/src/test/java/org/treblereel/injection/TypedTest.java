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

package org.treblereel.injection;

import org.junit.Test;
import org.treblereel.AbstractTest;
import org.treblereel.injection.qualifiers.typed.AbstractCanvasHandler;
import org.treblereel.injection.qualifiers.typed.ApplicationCommandManager;
import org.treblereel.injection.qualifiers.typed.RegistryAwareCommandManager;
import org.treblereel.injection.qualifiers.typed.SessionCommandManager;
import org.treblereel.injection.typed.DefaultDefinitionsCacheRegistry;
import org.treblereel.injection.typed.DefinitionUtils;
import org.treblereel.injection.typed.DefinitionsCacheRegistry;
import org.treblereel.injection.typed.SimpleDefinitionsCacheRegistry;
import org.treblereel.injection.typed.case2.BeanOne;
import org.treblereel.injection.typed.case2.BeanTwo;
import org.treblereel.injection.typed.case2.Iface;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 8/25/21
 */
public class TypedTest extends AbstractTest {

  @Test
  public void testQualifierFieldInjectionBean() {
    SessionCommandManager<AbstractCanvasHandler> sessionCommandManager =
        app.qualifierFieldInjection.morphNodeToolboxAction.sessionCommandManager;

    assertEquals(ApplicationCommandManager.class, sessionCommandManager.getClass());

    assertEquals(RegistryAwareCommandManager.class,
        ((ApplicationCommandManager) sessionCommandManager).commandManagerInstances.getClass());
  }

  @Test
  public void testDefaultDefinitionsCacheRegistry() {
    assertEquals(RegistryAwareCommandManager.class,
        app.beanManager.lookupBean(RegistryAwareCommandManager.class).getInstance().getClass());
  }

  @Test
  public void testDefaultAndTyped() {
    assertEquals(BeanTwo.class, app.beanManager.lookupBean(Iface.class).getInstance().getClass());
    assertEquals(BeanOne.class, app.beanManager.lookupBean(BeanOne.class).getInstance().getClass());
  }

}
