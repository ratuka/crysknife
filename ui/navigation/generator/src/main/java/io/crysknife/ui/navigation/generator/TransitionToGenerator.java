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

package io.crysknife.ui.navigation.generator;

import javax.inject.Inject;

import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.auto.common.MoreTypes;
import io.crysknife.annotation.Generator;
import io.crysknife.client.internal.InstanceImpl;
import io.crysknife.definition.InjectableVariableDefinition;
import io.crysknife.generator.ScopedBeanGenerator;
import io.crysknife.generator.WiringElementType;
import io.crysknife.generator.api.ClassBuilder;
import io.crysknife.generator.context.IOCContext;
import io.crysknife.definition.Definition;
import io.crysknife.logger.TreeLogger;
import io.crysknife.ui.navigation.client.local.Navigation;
import io.crysknife.ui.navigation.client.local.TransitionTo;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 3/3/20
 */
@Generator
public class TransitionToGenerator extends ScopedBeanGenerator {

  public static final String TRANSITION_TO_FACTORY = "TransitionTo_Factory";

  public TransitionToGenerator(TreeLogger treeLogger, IOCContext iocContext) {
    super(treeLogger, iocContext);
  }

  @Override
  public void register() {
    iocContext.register(Inject.class, TransitionTo.class, WiringElementType.BEAN, this);
  }

  @Override
  public void generate(ClassBuilder clazz, Definition beanDefinition) {

  }

  @Override
  public Expression generateBeanLookupCall(ClassBuilder clazz,
      InjectableVariableDefinition fieldPoint) {
    clazz.getClassCompilationUnit().addImport(TransitionTo.class);
    clazz.getClassCompilationUnit().addImport(Navigation.class);
    clazz.getClassCompilationUnit().addImport(InstanceImpl.class);

    return new ObjectCreationExpr().setType(InstanceImpl.class)
        .addArgument(new ObjectCreationExpr().setType(TransitionTo.class)
            .addArgument(MoreTypes.asDeclared(fieldPoint.getVariableElement().asType())
                .getTypeArguments().get(0).toString() + ".class")
            .addArgument(
                new CastExpr(new ClassOrInterfaceType().setName(Navigation.class.getSimpleName()),
                    new MethodCallExpr(new MethodCallExpr(new NameExpr("beanManager"), "lookupBean")
                        .addArgument(Navigation.class.getSimpleName() + ".class"),
                        "getInstance"))));
  }
}
