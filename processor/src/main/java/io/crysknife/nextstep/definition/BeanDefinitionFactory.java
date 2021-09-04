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

package io.crysknife.nextstep.definition;

import io.crysknife.exception.UnableToCompleteException;
import io.crysknife.generator.context.IOCContext;
import io.crysknife.logger.PrintWriterTreeLogger;
import io.crysknife.nextstep.ConstructorInjectionPointProcessor;
import io.crysknife.nextstep.FieldProcessor;

import javax.lang.model.type.TypeMirror;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 9/3/21
 */
public class BeanDefinitionFactory {

  private final IOCContext context;
  private final PrintWriterTreeLogger logger;
  private final FieldProcessor fieldProcessor;
  private final ConstructorInjectionPointProcessor constructorInjectionPointProcessor;

  public BeanDefinitionFactory(IOCContext context, PrintWriterTreeLogger logger) {
    this.context = context;
    this.logger = logger;
    this.fieldProcessor = new FieldProcessor(context, logger);
    this.constructorInjectionPointProcessor =
        new ConstructorInjectionPointProcessor(context, logger);
  }

  public BeanDefinition of(TypeMirror type) throws UnableToCompleteException {
    validateBean(type);
    BeanDefinition bean = new BeanDefinition(type);

    fieldProcessor.process(bean);
    constructorInjectionPointProcessor.process(bean);

    return bean;
  }

  private void validateBean(TypeMirror type) {

  }
}
