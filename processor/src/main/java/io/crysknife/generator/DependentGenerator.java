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

package io.crysknife.generator;

import io.crysknife.annotation.Generator;
import io.crysknife.definition.BeanDefinition;
import io.crysknife.generator.api.ClassBuilder;
import io.crysknife.generator.context.IOCContext;
import io.crysknife.logger.TreeLogger;

import javax.enterprise.context.Dependent;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 3/2/19
 */
@Generator(priority = 1)
public class DependentGenerator extends ScopedBeanGenerator {

  public DependentGenerator(TreeLogger treeLogger, IOCContext iocContext) {
    super(treeLogger, iocContext);
  }

  @Override
  public void register() {
    iocContext.register(Dependent.class, WiringElementType.BEAN, this);
  }

  @Override
  public void generateInstanceGetMethodBuilder(ClassBuilder builder,
      BeanDefinition beanDefinition) {
    super.generateInstanceGetMethodBuilder(builder, beanDefinition);
    builder.getGetMethodDeclaration().getBody().get()
        .addAndGetStatement(generateInstanceInitializer(builder, beanDefinition));
  }
}
