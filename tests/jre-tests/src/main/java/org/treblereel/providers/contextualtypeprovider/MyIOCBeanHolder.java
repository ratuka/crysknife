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

package org.treblereel.providers.contextualtypeprovider;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 11/5/21
 */
@Singleton
public class MyIOCBeanHolder {


  @Inject
  private MyIOCBean mybean;

  @Inject
  private MyIOCSingletonBean<Integer, Double> myIOCSingletonBean;

  private MyIOCSingletonBean<Integer, Double> myIOCSingletonBean2;

  private MyIOCBean mybean1;


  @Inject
  public MyIOCBeanHolder(MyIOCBean<Integer, Double> bean,
      MyIOCSingletonBean<Integer, Double> bean2) {
    this.mybean1 = bean;
    this.myIOCSingletonBean2 = bean2;
  }

  public MyIOCBean getMybean() {
    return mybean;
  }

  public MyIOCBean getMybean1() {
    return mybean1;
  }

  public MyIOCSingletonBean getMyIOCSingletonBean() {
    return myIOCSingletonBean;
  }

  public MyIOCSingletonBean getMyIOCSingletonBean2() {
    return myIOCSingletonBean2;
  }
}
