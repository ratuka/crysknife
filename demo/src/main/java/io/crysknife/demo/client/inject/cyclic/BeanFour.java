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

package io.crysknife.demo.client.inject.cyclic;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.crysknife.client.BeanManager;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 6/20/19
 */
@Singleton
public class BeanFour {

    //@Inject
    BeanOne beanOne;

    @Inject
    BeanManager beanManager;


    @PostConstruct
    public void init() {
        beanOne = beanManager.<BeanOne>lookupBean(BeanOne.class).getInstance();
        beanOne.say();
    }
}
