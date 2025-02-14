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


package org.treblereel.injection;

import org.junit.Test;
import org.treblereel.AbstractTest;
import org.treblereel.injection.generic.TypeOneGenericAbstractBean;
import org.treblereel.injection.generic.TypeOneGenericBeansHolder;
import org.treblereel.injection.generic.TypeTwoGenericAbstractBean;
import org.treblereel.injection.generic.TypeTwoGenericBeansHolder;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 11/29/21
 */
public class GenericBeanInjectionTest extends AbstractTest {

  @Test
  public void testTypeOne() {
    assertEquals(TypeOneGenericAbstractBean.class, app.beanManager
        .lookupBean(TypeOneGenericBeansHolder.class).getInstance().tGenericAbstractBean.getClass());
  }

  // TODO fix @AspectJ, it intercepts this as TypeOneGenericAbstractBean
  // @Test
  public void testTypeTwo() {
    assertEquals(TypeTwoGenericAbstractBean.class, app.beanManager
        .lookupBean(TypeTwoGenericBeansHolder.class).getInstance().tGenericAbstractBean.getClass());
  }
}
