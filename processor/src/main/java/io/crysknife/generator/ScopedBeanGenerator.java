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
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import io.crysknife.annotation.Generator;
import io.crysknife.client.BeanManager;
import io.crysknife.client.InstanceFactory;
import io.crysknife.client.Reflect;
import io.crysknife.client.SyncBeanDef;
import io.crysknife.client.internal.BeanFactory;
import io.crysknife.client.internal.proxy.Interceptor;
import io.crysknife.client.internal.proxy.OnFieldAccessed;
import io.crysknife.definition.BeanDefinition;
import io.crysknife.definition.InjectableVariableDefinition;
import io.crysknife.definition.InjectionParameterDefinition;
import io.crysknife.definition.ProducesBeanDefinition;
import io.crysknife.exception.GenerationException;
import io.crysknife.generator.api.ClassBuilder;
import io.crysknife.generator.context.ExecutionEnv;
import io.crysknife.generator.context.IOCContext;
import io.crysknife.logger.TreeLogger;
import io.crysknife.util.Utils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Provider;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.javaparser.ast.expr.UnaryExpr.Operator.LOGICAL_COMPLEMENT;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 3/3/19
 */
public abstract class ScopedBeanGenerator<T> extends BeanIOCGenerator<BeanDefinition> {

  protected NameExpr instance;

  private PostConstructGenerator postConstructGenerator =
      new PostConstructGenerator(logger, iocContext);

  private PreDestroyGenerator preDestroyGenerator = new PreDestroyGenerator(logger, iocContext);

  public ScopedBeanGenerator(TreeLogger treeLogger, IOCContext iocContext) {
    super(treeLogger, iocContext);
  }

  @Override
  public void generate(ClassBuilder clazz, BeanDefinition beanDefinition) {
    initClassBuilder(clazz, beanDefinition);
    generateDependantFields(clazz, beanDefinition);
    generateInterceptorFieldDeclaration(clazz);
    generateNewInstanceMethodBuilder(clazz);
    generateInitInstanceMethodBuilder(clazz, beanDefinition);
    generateInstanceGetMethodBuilder(clazz, beanDefinition);
    generateDependantFieldDeclaration(clazz, beanDefinition);
    generateInstanceGetFieldDecorators(clazz, beanDefinition);
    generateInstanceGetMethodDecorators(clazz, beanDefinition);
    generateInstanceGetMethodReturn(clazz, beanDefinition);
    processPostConstructAnnotation(clazz, beanDefinition);
    processPreDestroyAnnotation(clazz, beanDefinition);
    write(clazz, beanDefinition);
  }

  private void generateDependantFields(ClassBuilder classBuilder, BeanDefinition definition) {
    Set<InjectionParameterDefinition> params = definition.getConstructorParams();
    Iterator<InjectionParameterDefinition> injectionPointDefinitionIterator = params.iterator();
    while (injectionPointDefinitionIterator.hasNext()) {
      InjectableVariableDefinition argument = injectionPointDefinitionIterator.next();
      generateFactoryFieldDeclaration(classBuilder, definition, argument, "constructor");
    }

    definition.getFields().forEach(
        field -> generateFactoryFieldDeclaration(classBuilder, definition, field, "field"));

  }

  protected void generateFactoryFieldDeclaration(ClassBuilder classBuilder,
      BeanDefinition definition, InjectableVariableDefinition fieldPoint, String kind) {
    String varName = "_" + kind + "_" + fieldPoint.getVariableElement().getSimpleName().toString();
    String typeQualifiedName = generationUtils.getActualQualifiedBeanName(fieldPoint);
    ClassOrInterfaceType supplier =
        new ClassOrInterfaceType().setName(Supplier.class.getSimpleName());

    ClassOrInterfaceType type = new ClassOrInterfaceType();
    type.setName(InstanceFactory.class.getSimpleName());
    type.setTypeArguments(new ClassOrInterfaceType().setName(typeQualifiedName));
    supplier.setTypeArguments(type);


    Expression beanCall;
    if (fieldPoint.getImplementation().isPresent()
        && fieldPoint.getImplementation().get().getIocGenerator().isPresent()) {
      beanCall = fieldPoint.getImplementation().get().getIocGenerator().get()
          .generateBeanLookupCall(classBuilder, fieldPoint);
    } else if (fieldPoint.getGenerator().isPresent()) {
      beanCall = fieldPoint.getGenerator().get().generateBeanLookupCall(classBuilder, fieldPoint);
    } else {
      beanCall = generateBeanLookupCall(classBuilder, fieldPoint);
    }

    if (beanCall == null) {
      throw new GenerationException();
    }

    LambdaExpr lambda = new LambdaExpr().setEnclosingParameters(true);
    lambda.setBody(new ExpressionStmt(beanCall));

    classBuilder.addFieldWithInitializer(supplier, varName, lambda, Modifier.Keyword.PRIVATE);
  }

  protected void generateInitInstanceMethodBuilder(ClassBuilder classBuilder,
      BeanDefinition beanDefinition) {
    classBuilder.addInitInstanceMethod().getParameters()
        .add(new Parameter(
            new ClassOrInterfaceType().setName(Utils.getSimpleClassName(beanDefinition.getType())),
            "instance"));
  }

  public void initClassBuilder(ClassBuilder clazz, BeanDefinition beanDefinition) {
    String pkg = Utils.getPackageName(MoreTypes.asTypeElement(beanDefinition.getType()));
    TypeElement asTypeElement = MoreTypes.asTypeElement(beanDefinition.getType());

    StringBuffer sb = new StringBuffer();
    if (asTypeElement.getEnclosingElement().getKind().isClass()) {
      sb.append(MoreElements.asType(asTypeElement.getEnclosingElement()).getSimpleName());
      sb.append("_");
    }
    sb.append(MoreTypes.asTypeElement(beanDefinition.getType()).getSimpleName());
    sb.append("_Factory");

    String classFactoryName = sb.toString();

    clazz.getClassCompilationUnit().setPackageDeclaration(pkg);
    clazz.getClassCompilationUnit().addImport(BeanFactory.class);
    clazz.getClassCompilationUnit().addImport(SyncBeanDef.class);
    clazz.getClassCompilationUnit().addImport(InstanceFactory.class);
    clazz.getClassCompilationUnit().addImport(Provider.class);
    clazz.getClassCompilationUnit().addImport(OnFieldAccessed.class);
    clazz.getClassCompilationUnit().addImport(Reflect.class);
    clazz.getClassCompilationUnit().addImport(Supplier.class);
    clazz.getClassCompilationUnit().addImport(BeanManager.class);
    clazz.getClassCompilationUnit().addImport(Dependent.class);
    clazz.setClassName(classFactoryName);

    ClassOrInterfaceType factory = new ClassOrInterfaceType();
    factory.setName(BeanFactory.class.getSimpleName());
    factory.setTypeArguments(
        new ClassOrInterfaceType().setName(Utils.getSimpleClassName(beanDefinition.getType())));
    clazz.getExtendedTypes().add(factory);
  }

  private void generateInterceptorFieldDeclaration(ClassBuilder clazz) {
    if (iocContext.getGenerationContext().getExecutionEnv().equals(ExecutionEnv.J2CL)) {
      clazz.getClassCompilationUnit().addImport(Interceptor.class);
      clazz.addField(Interceptor.class.getSimpleName(), "interceptor", Modifier.Keyword.PRIVATE);
    }
  }

  public void generateNewInstanceMethodBuilder(ClassBuilder classBuilder) {
    MethodDeclaration getMethodDeclaration =
        classBuilder.addMethod("createInstance", Modifier.Keyword.PUBLIC);

    getMethodDeclaration.addAnnotation(Override.class);
    getMethodDeclaration.setType(Utils.getSimpleClassName(classBuilder.beanDefinition.getType()));
    classBuilder.setGetMethodDeclaration(getMethodDeclaration);
  }

  public void generateInstanceGetMethodBuilder(ClassBuilder classBuilder,
      BeanDefinition beanDefinition) {
    String clazzName = Utils.getSimpleClassName(classBuilder.beanDefinition.getType());

    MethodDeclaration getMethodDeclaration =
        classBuilder.addMethod("getInstance", Modifier.Keyword.PUBLIC);

    getMethodDeclaration.addAnnotation(Override.class);
    getMethodDeclaration.setType(clazzName);

    getMethodDeclaration.getBody().ifPresent(body -> {

      IfStmt ifStmt = new IfStmt().setCondition(new UnaryExpr(
          new MethodCallExpr(new MethodCallExpr(new NameExpr("beanDef"), "getScope"), "equals")
              .addArgument(new FieldAccessExpr(new NameExpr("Dependent"), "class")),
          LOGICAL_COMPLEMENT));

      body.addAndGetStatement(ifStmt);
      BlockStmt blockStmt = new BlockStmt();

      blockStmt.addAndGetStatement(new IfStmt()
          .setCondition(new BinaryExpr(new NameExpr("instance"), new NullLiteralExpr(),
              BinaryExpr.Operator.NOT_EQUALS))
          .setThenStmt(
              new ReturnStmt(new CastExpr().setType(new ClassOrInterfaceType().setName(clazzName))
                  .setExpression(new NameExpr("instance")))));
      ifStmt.setThenStmt(blockStmt);

      body.addAndGetStatement(new AssignExpr()
          .setTarget(new VariableDeclarationExpr(new ClassOrInterfaceType().setName(
              Utils.getSimpleClassName(classBuilder.beanDefinition.getType())), "instance"))
          .setValue(new MethodCallExpr("createInstanceInternal")));

      body.addAndGetStatement(new MethodCallExpr("initInstance").addArgument("instance"));
      body.addAndGetStatement(new ReturnStmt(new NameExpr("instance")));
    });
  }

  public void generateDependantFieldDeclaration(ClassBuilder classBuilder,
      BeanDefinition beanDefinition) {

    ConstructorDeclaration constructorDeclaration =
        classBuilder.addConstructorDeclaration(Modifier.Keyword.PUBLIC);
    constructorDeclaration.addAndGetParameter(BeanManager.class, "beanManager");

    constructorDeclaration.getBody()
        .addAndGetStatement(new MethodCallExpr("super").addArgument("beanManager"));

    if (!iocContext.getGenerationContext().getExecutionEnv().equals(ExecutionEnv.JRE)) {
      beanDefinition.getFields().forEach(fieldPoint -> {
        Expression expr =
            getFieldAccessorExpression(classBuilder, beanDefinition, fieldPoint, "field");
        classBuilder.getGetMethodDeclaration().getBody().get().addStatement(expr);
      });
    }

    beanDefinition.getDecorators().stream()
        .sorted(
            Comparator.comparingInt(o -> o.getClass().getAnnotation(Generator.class).priority()))
        .forEach(gen -> gen.generate(classBuilder, beanDefinition));
  }

  protected Expression getFieldAccessorExpression(ClassBuilder classBuilder,
      BeanDefinition beanDefinition, InjectableVariableDefinition fieldPoint, String kind) {

    String varName = "_" + kind + "_" + fieldPoint.getVariableElement().getSimpleName().toString();

    if (fieldPoint.getBeanDefinition() instanceof ProducesBeanDefinition) {
      throw new Error(fieldPoint.getVariableElement().getSimpleName().toString());
    }


    if (kind.equals("constructor")) {
      return new MethodCallExpr(
          new MethodCallExpr(new FieldAccessExpr(new ThisExpr(), varName), "get"), "getInstance");
    }

    FieldAccessExpr fieldAccessExpr = new FieldAccessExpr(new ThisExpr(), "interceptor");
    MethodCallExpr reflect =
        new MethodCallExpr(new NameExpr(Reflect.class.getSimpleName()), "objectProperty")
            .addArgument(
                new StringLiteralExpr(Utils.getJsFieldName(fieldPoint.getVariableElement())))
            .addArgument(new FieldAccessExpr(new ThisExpr(), "instance"));

    LambdaExpr lambda = new LambdaExpr();
    lambda.setEnclosingParameters(true);
    lambda.setBody(new ExpressionStmt(
        new MethodCallExpr(new FieldAccessExpr(new ThisExpr(), varName), "get")));

    ObjectCreationExpr onFieldAccessedCreationExpr = new ObjectCreationExpr();
    onFieldAccessedCreationExpr.setType(OnFieldAccessed.class.getSimpleName());
    onFieldAccessedCreationExpr.addArgument(lambda);

    return new MethodCallExpr(fieldAccessExpr, "addGetPropertyInterceptor").addArgument(reflect)
        .addArgument(onFieldAccessedCreationExpr);
  }

  private void generateInstanceGetFieldDecorators(ClassBuilder clazz,
      BeanDefinition beanDefinition) {

    Set<InjectableVariableDefinition> points = new HashSet<>(beanDefinition.getFields());
    points.addAll(beanDefinition.getConstructorParams());

    points.forEach(point -> {
      point.getDecorators().stream()
          .sorted(
              Comparator.comparingInt(o -> o.getClass().getAnnotation(Generator.class).priority()))
          .forEach(generator -> generator.generate(clazz, point));
    });
  }

  private void generateInstanceGetMethodDecorators(ClassBuilder clazz,
      BeanDefinition beanDefinition) {

    beanDefinition.getMethods().stream().forEach(method -> {
      method.getDecorators().stream()
          .sorted(
              Comparator.comparingInt(o -> o.getClass().getAnnotation(Generator.class).priority()))
          .forEach(decorator -> {
            if (decorator instanceof ProducesGenerator) {
              // TODO Produces
            } else {
              decorator.generate(clazz, method);
            }
          });
    });
  }

  public void generateInstanceGetMethodReturn(ClassBuilder classBuilder,
      BeanDefinition beanDefinition) {
    String clazzName = Utils.getSimpleClassName(classBuilder.beanDefinition.getType());
    classBuilder.getGetMethodDeclaration().getBody().get().addStatement(
        new ReturnStmt(new CastExpr().setType(new ClassOrInterfaceType().setName(clazzName))
            .setExpression(new NameExpr("instance"))));
  }

  private void processPostConstructAnnotation(ClassBuilder classBuilder,
      BeanDefinition beanDefinition) {
    LinkedList<ExecutableElement> postConstructs = Utils
        .getAllMethodsIn(iocContext.getGenerationContext().getElements(),
            MoreTypes.asTypeElement(beanDefinition.getType()))
        .stream().filter(elm -> elm.getAnnotation(PostConstruct.class) != null)
        .collect(Collectors.toCollection(LinkedList::new));

    Iterator<ExecutableElement> elm = postConstructs.descendingIterator();
    while (elm.hasNext()) {
      postConstructGenerator.generate(beanDefinition.getType(),
          classBuilder.getInitInstanceMethod().getBody().get(), elm.next());
    }
  }

  protected void processPreDestroyAnnotation(ClassBuilder classBuilder,
      BeanDefinition beanDefinition) {
    preDestroyGenerator.generate(beanDefinition, classBuilder);
  }

  protected Expression generateInstanceInitializer(ClassBuilder classBuilder,
      BeanDefinition definition) {
    instance = new NameExpr("instance");
    Expression instanceFieldAssignExpr =
        generateInstanceInitializerNewObjectExpr(classBuilder, definition);
    return new AssignExpr().setTarget(instance).setValue(instanceFieldAssignExpr);
  }

  protected Expression generateInstanceInitializerNewObjectExpr(ClassBuilder classBuilder,
      BeanDefinition definition) {
    ObjectCreationExpr newInstance = generateNewInstanceCreationExpr(definition);
    Set<InjectionParameterDefinition> params = definition.getConstructorParams();
    Iterator<InjectionParameterDefinition> injectionPointDefinitionIterator = params.iterator();
    while (injectionPointDefinitionIterator.hasNext()) {
      InjectableVariableDefinition argument = injectionPointDefinitionIterator.next();
      newInstance.addArgument(
          getFieldAccessorExpression(classBuilder, definition, argument, "constructor"));
    }

    Expression instanceFieldAssignExpr;

    if (iocContext.getGenerationContext().getExecutionEnv().equals(ExecutionEnv.J2CL)) {
      FieldAccessExpr interceptor = new FieldAccessExpr(new ThisExpr(), "interceptor");

      ObjectCreationExpr interceptorCreationExpr = new ObjectCreationExpr();
      interceptorCreationExpr.setType(Interceptor.class.getSimpleName());
      interceptorCreationExpr.addArgument(newInstance);

      classBuilder.getGetMethodDeclaration().getBody().get().addAndGetStatement(
          new AssignExpr().setTarget(interceptor).setValue(interceptorCreationExpr));

      instanceFieldAssignExpr = new MethodCallExpr(interceptor, "getProxy");
    } else {
      instanceFieldAssignExpr = newInstance;
    }
    return instanceFieldAssignExpr;
  }

  protected ObjectCreationExpr generateNewInstanceCreationExpr(BeanDefinition definition) {
    ObjectCreationExpr newInstance = new ObjectCreationExpr();
    return newInstance.setType(Utils.getSimpleClassName(definition.getType()));
  }


}
