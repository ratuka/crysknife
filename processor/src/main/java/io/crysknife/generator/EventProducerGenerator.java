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

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import io.crysknife.annotation.Generator;
import io.crysknife.client.BeanManager;
import io.crysknife.client.internal.AbstractEventFactory;
import io.crysknife.client.internal.InstanceImpl;
import io.crysknife.definition.BeanDefinition;
import io.crysknife.definition.Definition;
import io.crysknife.definition.InjectableVariableDefinition;
import io.crysknife.generator.api.ClassBuilder;
import io.crysknife.generator.context.IOCContext;
import io.crysknife.logger.TreeLogger;
import io.crysknife.util.GenerationUtils;
import io.crysknife.util.Utils;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.BiConsumer;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 3/31/19
 */
@Generator(priority = 999)
public class EventProducerGenerator extends ScopedBeanGenerator {

  private GenerationUtils generationUtils;

  public EventProducerGenerator(TreeLogger treeLogger, IOCContext iocContext) {
    super(treeLogger, iocContext);
    generationUtils = new GenerationUtils(iocContext);
  }

  @Override
  public void register() {
    iocContext.register(Inject.class, Event.class, WiringElementType.FIELD_TYPE, this);
  }

  @Override
  public void generate(ClassBuilder clazz, Definition definition) {
    if (definition instanceof BeanDefinition) {
      BeanDefinition beanDefinition = (BeanDefinition) definition;
      initClassBuilder(clazz, beanDefinition);
      generateBeanGetMethod(clazz, beanDefinition);
      write(clazz, beanDefinition);
    }
  }

  @Override
  public Expression generateBeanLookupCall(ClassBuilder classBuilder,
      InjectableVariableDefinition fieldPoint) {
    classBuilder.getClassCompilationUnit().addImport("javax.enterprise.event.Event_Factory");
    classBuilder.getClassCompilationUnit().addImport(InstanceImpl.class.getCanonicalName());
    MoreTypes.asDeclared(fieldPoint.getVariableElement().asType()).getTypeArguments();

    return new ObjectCreationExpr().setType(InstanceImpl.class)
        .addArgument(new NameExpr("Event_Factory.get().get(" + MoreTypes
            .asDeclared(fieldPoint.getVariableElement().asType()).getTypeArguments().get(0)
            + ".class)"));
  }

  @Override
  public void initClassBuilder(ClassBuilder clazz, BeanDefinition beanDefinition) {
    clazz.getClassCompilationUnit().setPackageDeclaration(beanDefinition.getPackageName());
    clazz.setClassName(
        MoreTypes.asTypeElement(beanDefinition.getType()).getSimpleName().toString() + "_Factory");
    clazz.getClassCompilationUnit().addImport("io.crysknife.client.internal.AbstractEventFactory");

    ClassOrInterfaceType factory = new ClassOrInterfaceType();
    factory.setName("AbstractEventFactory");
    clazz.getExtendedTypes().add(factory);
  }

  public void generateBeanGetMethod(ClassBuilder classBuilder, BeanDefinition beanDefinition) {
    addSubscribers(classBuilder);

    MethodDeclaration getMethodDeclaration =
        classBuilder.addMethod("get", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
    getMethodDeclaration.setType("Event_Factory");
    classBuilder.setGetMethodDeclaration(getMethodDeclaration);

    BlockStmt body = classBuilder.getGetMethodDeclaration().getBody().get();
    IfStmt ifStmt = new IfStmt().setCondition(new BinaryExpr(new NameExpr("instance"),
        new NullLiteralExpr(), BinaryExpr.Operator.EQUALS));
    ObjectCreationExpr newInstance = new ObjectCreationExpr();
    newInstance.setType(new ClassOrInterfaceType().setName("Event_Factory"));

    ifStmt.setThenStmt(new BlockStmt().addAndGetStatement(
        new AssignExpr().setTarget(new NameExpr("instance")).setValue(newInstance)));
    body.addAndGetStatement(ifStmt);
    body.addAndGetStatement(new ReturnStmt(new NameExpr("instance")));
    classBuilder.addField("javax.enterprise.event.Event_Factory", "instance",
        Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
  }

  private void addSubscribers(ClassBuilder classBuilder) {
    ConstructorDeclaration constructorDeclaration =
        classBuilder.addConstructorDeclaration(Modifier.Keyword.PRIVATE);
    constructorDeclaration.getBody().addAndGetStatement(new MethodCallExpr("super").addArgument(
        new MethodCallExpr(new NameExpr(BeanManager.class.getCanonicalName() + "Impl"), "get")));
  }
}
