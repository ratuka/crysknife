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

import com.github.javaparser.ast.stmt.BlockStmt;
import io.crysknife.exception.GenerationException;
import io.crysknife.exception.UnableToCompleteException;
import io.crysknife.generator.context.IOCContext;
import io.crysknife.logger.TreeLogger;
import io.crysknife.util.GenerationUtils;
import io.crysknife.validation.PostConstructValidator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 3/3/19
 */
public class PostConstructGenerator {

  private PostConstructValidator validator;
  private GenerationUtils utils;

  public PostConstructGenerator(TreeLogger treeLogger, IOCContext iocContext) {
    this.validator = new PostConstructValidator(iocContext);
    this.utils = new GenerationUtils(iocContext);
  }

  public void generate(TypeMirror parent, BlockStmt body, ExecutableElement postConstruct) {
    try {
      validator.validate(postConstruct);
    } catch (UnableToCompleteException e) {
      throw new GenerationException(e);
    }
    body.addAndGetStatement(utils.generateMethodCall(parent, postConstruct));
  }

}
