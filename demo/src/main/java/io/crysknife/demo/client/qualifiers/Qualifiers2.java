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

package io.crysknife.demo.client.qualifiers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import elemental2.dom.HTMLDivElement;
import io.crysknife.client.IsElement;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 3/16/20
 */
@ApplicationScoped
//@Page
public class Qualifiers2 implements IsElement<HTMLDivElement> {

    @Inject
    @QualifierOne
    public QualifierBean qualifierBeanOne;

    @Inject
    @QualifierTwo
    public QualifierBean qualifierBeanTwo;

    @Inject
    @Default
    public QualifierBean qualifierBeanDefault;

    @Override
    public HTMLDivElement getElement() {
        return null;
    }
}
