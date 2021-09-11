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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.auto.common.MoreTypes;
import io.crysknife.annotation.Application;
import io.crysknife.annotation.Generator;
import io.crysknife.client.BeanManager;
import io.crysknife.client.Interceptor;
import io.crysknife.client.Reflect;
import io.crysknife.client.internal.Factory;
import io.crysknife.client.internal.OnFieldAccessed;
import io.crysknife.definition.BeanDefinition;
import io.crysknife.definition.Definition;
import io.crysknife.definition.InjectionPointDefinition;
import io.crysknife.generator.api.ClassBuilder;
import io.crysknife.generator.context.GenerationContext;
import io.crysknife.generator.context.IOCContext;
import io.crysknife.util.Utils;

import javax.enterprise.inject.Instance;
import javax.inject.Provider;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 4/5/19
 */
@Generator(priority = 100000)
public class BootstrapperGenerator extends ScopedBeanGenerator {

  private String BOOTSTRAP_EXTENSION = "Bootstrap";

  public BootstrapperGenerator(IOCContext iocContext) {
    super(iocContext);
  }

  @Override
  public void register() {
    iocContext.register(Application.class, WiringElementType.BEAN, this);
  }

  @Override
  public void generate(ClassBuilder clazz, Definition definition) {
    super.generate(clazz, definition);
  }

  @Override
  public void initClassBuilder(ClassBuilder clazz, BeanDefinition beanDefinition) {
    String pkg = Utils.getPackageName(MoreTypes.asTypeElement(beanDefinition.getType()));

    clazz.getClassCompilationUnit().setPackageDeclaration(pkg);

    if (!iocContext.getGenerationContext().isGwt2()) {
      clazz.getClassCompilationUnit().addImport(OnFieldAccessed.class);
      clazz.getClassCompilationUnit().addImport(Reflect.class);
      clazz.getClassCompilationUnit().addImport(Instance.class);
      clazz.getClassCompilationUnit().addImport(Factory.class);
      clazz.getClassCompilationUnit().addImport(Supplier.class);
      clazz.getClassCompilationUnit().addImport(Provider.class);
      clazz.getClassCompilationUnit().addImport(BeanManager.class);
    }

    clazz.setClassName(MoreTypes.asTypeElement(beanDefinition.getType()).getSimpleName().toString()
        + BOOTSTRAP_EXTENSION);

    clazz.addField(MoreTypes.asTypeElement(beanDefinition.getType()).getQualifiedName().toString(),
        "instance", Modifier.Keyword.PRIVATE);

    clazz.addFieldWithInitializer(BeanManager.class.getSimpleName(), "beanManager",
        new MethodCallExpr(new NameExpr(BeanManager.class.getCanonicalName() + "Impl"), "get"),
        Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);

    beanDefinition.getFields().forEach(field -> {
      generateFactoryFieldDeclaration(clazz, field);
    });

  }

  protected void generateFactoryFieldDeclaration(ClassBuilder classBuilder,
      InjectionPointDefinition fieldPoint) {

    String varName = "_field_" + fieldPoint.getVariableElement().getSimpleName().toString();
    ClassOrInterfaceType supplier =
        new ClassOrInterfaceType().setName(Supplier.class.getSimpleName());

    ClassOrInterfaceType type = new ClassOrInterfaceType();
    type.setName(Instance.class.getSimpleName());
    type.setTypeArguments(new ClassOrInterfaceType().setName(MoreTypes
        .asTypeElement(fieldPoint.getVariableElement().asType()).getQualifiedName().toString()));
    supplier.setTypeArguments(type);

    Expression beanCall = null;

    if (fieldPoint.getImplementation().isPresent()
        && fieldPoint.getImplementation().get().getIocGenerator().isPresent()) {
      beanCall = fieldPoint.getImplementation().get().getIocGenerator().get()
          .generateBeanLookupCall(classBuilder, fieldPoint);
    } else if (fieldPoint.getGenerator() != null) {
      beanCall = fieldPoint.getGenerator().generateBeanLookupCall(classBuilder, fieldPoint);
    }

    LambdaExpr lambda = new LambdaExpr();
    lambda.setEnclosingParameters(true);
    lambda.setBody(new ExpressionStmt(beanCall));

    classBuilder.addFieldWithInitializer(supplier, varName, lambda, Modifier.Keyword.PRIVATE);

  }

  @Override
  public void generateInstanceGetMethodBuilder(ClassBuilder classBuilder,
      BeanDefinition beanDefinition) {
    classBuilder.addConstructorDeclaration();

    MethodDeclaration getMethodDeclaration = classBuilder.addMethod("initialize");
    classBuilder.setGetMethodDeclaration(getMethodDeclaration);

    if (!iocContext.getGenerationContext().isGwt2() && !iocContext.getGenerationContext().isJre()) {
      ObjectCreationExpr interceptorCreationExpr = new ObjectCreationExpr();
      interceptorCreationExpr.setType(Interceptor.class.getSimpleName());
      interceptorCreationExpr.addArgument(new NameExpr("instance"));

      classBuilder.getGetMethodDeclaration().getBody().get().addAndGetStatement(new AssignExpr()
          .setTarget(new NameExpr("interceptor")).setValue(interceptorCreationExpr));

      classBuilder.getGetMethodDeclaration().getBody().get()
          .addAndGetStatement(new AssignExpr().setTarget(new NameExpr("instance"))
              .setValue(new MethodCallExpr(new NameExpr("interceptor"), "getProxy")));
    }

    if (!iocContext.getGenerationContext().isJre()) {
      for (InjectionPointDefinition fieldPoint : beanDefinition.getFields()) {
        classBuilder.getGetMethodDeclaration().getBody().get().addStatement(
            getFieldAccessorExpression(classBuilder, beanDefinition, fieldPoint, "field"));
      }
    }

  }

  @Override
  public void generateDependantFieldDeclaration(ClassBuilder classBuilder,
      BeanDefinition beanDefinition) {
    classBuilder.addConstructorDeclaration();
    Parameter arg = new Parameter();
    arg.setName("application");
    arg.setType(MoreTypes.asTypeElement(beanDefinition.getType()).getSimpleName().toString());

    classBuilder.addParametersToConstructor(arg);

    /*
     * beanDefinition.getFieldInjectionPoints().forEach(fieldPoint -> iocContext.getBeans()
     * .get(fieldPoint.getType()).generateBeanCall(iocContext, classBuilder, fieldPoint));
     */

    AssignExpr assign = new AssignExpr().setTarget(new FieldAccessExpr(new ThisExpr(), "instance"))
        .setValue(new NameExpr("application"));
    classBuilder.addStatementToConstructor(assign);
  }

  @Override
  public void generateInstanceGetMethodReturn(ClassBuilder classBuilder,
      BeanDefinition beanDefinition) {

  }

  /*
   * protected void generateFactoryFieldDeclaration(ClassBuilder classBuilder, BeanDefinition
   * beanDefinition) { String varName = Utils.toVariableName(beanDefinition.getQualifiedName());
   * ClassOrInterfaceType type = new ClassOrInterfaceType();
   * type.setName(Instance.class.getCanonicalName()); type.setTypeArguments(new
   * ClassOrInterfaceType().setName(beanDefinition.getQualifiedName()));
   *
   * classBuilder.addField(type, varName, Modifier.Keyword.FINAL, Modifier.Keyword.PRIVATE); }
   */

  @Override
  public void write(ClassBuilder clazz, BeanDefinition beanDefinition, GenerationContext context) {
    try {
      String fileName = Utils.getQualifiedName(MoreTypes.asElement(beanDefinition.getType()))
          + BOOTSTRAP_EXTENSION;
      String source = clazz.toSourceCode();
      build(fileName, source, context);
    } catch (IOException e1) {
      // throw new GenerationException(e1);
    }
  }
}
