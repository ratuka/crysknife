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

package org.treblereel;

import elemental2.core.Global;
import elemental2.dom.DomGlobal;
import org.junit.Before;
import org.junit.Test;
import org.treblereel.injection.applicationscoped.SimpleBeanApplicationScoped;
import org.treblereel.injection.qualifiers.QualifierBeanOne;
import org.treblereel.injection.qualifiers.QualifierBeanTwo;
import org.treblereel.injection.qualifiers.QualifierConstructorInjection;

import com.google.j2cl.junit.apt.J2clTestInput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 9/10/19
 */
@J2clTestInput(SimpleBeanTest.class)
public class SimpleBeanTest {

  @Before
  public void init() {
    DomGlobal.console.log("BEFORE");

  }

  @Test
  public void testAppSimpleBean() {
    App app = new App();
    app.onModuleLoad();
    //new AppBootstrap(app).initialize();

    assertTrue(true);
    DomGlobal.console.log("testAppSimpleBean 1 " + Global.JSON.stringify(app.getSimpleBeanApplicationScoped()));
    DomGlobal.console.log("testAppSimpleBean 2 " + Global.JSON.stringify(app));

    assertNotNull(app.getSimpleBeanApplicationScoped());
/*    assertEquals(SimpleBeanApplicationScoped.class.getSimpleName(),
        app.getSimpleBeanApplicationScoped().getName());

    assertNotNull(app.getQualifierConstructorInjection());
    assertEquals(QualifierConstructorInjection.class.getSimpleName(),
        app.getQualifierConstructorInjection().getClass().getSimpleName());
    assertEquals(QualifierBeanOne.class,
        app.getQualifierConstructorInjection().qualifierBeanOne.getClass());
    assertEquals(QualifierBeanTwo.class,
        app.getQualifierConstructorInjection().qualifierBeanTwo.getClass());*/
  }
}
