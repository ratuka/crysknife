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

package io.crysknife.util;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.auto.common.MoreTypes;
import io.crysknife.client.Reflect;
import io.crysknife.client.internal.InstanceImpl;
import io.crysknife.definition.BeanDefinition;
import io.crysknife.definition.InjectableVariableDefinition;
import io.crysknife.generator.api.ClassBuilder;
import io.crysknife.generator.context.ExecutionEnv;
import io.crysknife.generator.context.IOCContext;
import jsinterop.base.Js;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 8/19/21
 */
public class GenerationUtils {

  private final IOCContext context;
  private final TypeMirror qualifier;

  public GenerationUtils(IOCContext context) {
    this.context = context;
    qualifier = context.getGenerationContext().getElements()
        .getTypeElement(Qualifier.class.getCanonicalName()).asType();

  }

  public String getActualQualifiedBeanName(InjectableVariableDefinition fieldPoint) {
    String typeQualifiedName;
    if (!MoreTypes.asTypeElement(fieldPoint.getVariableElement().asType()).getTypeParameters()
        .isEmpty()) {
      List<? extends TypeParameterElement> params =
          MoreTypes.asTypeElement(fieldPoint.getVariableElement().asType()).getTypeParameters();


      typeQualifiedName = context.getGenerationContext().getTypes()
          .erasure(fieldPoint.getVariableElement().asType()).toString();
      if (fieldPoint.getImplementation().isPresent()) {
        if (params.get(0).getKind().equals(ElementKind.TYPE_PARAMETER)) {
          typeQualifiedName = fieldPoint.getImplementation().get().getQualifiedName();
        }
      }
    } else if (context.getGenerationContext().getTypes().isSameType(
        fieldPoint.getVariableElement().asType(),
        fieldPoint.getImplementation().orElse(fieldPoint.getBeanDefinition()).getType())) {
      typeQualifiedName = fieldPoint.getVariableElement().asType().toString();
    } else {
      if (fieldPoint.getImplementation().isPresent()) {
        typeQualifiedName = fieldPoint.getImplementation().get().getQualifiedName();
      } else {
        typeQualifiedName = fieldPoint.getVariableElement().asType().toString();
      }
    }
    return typeQualifiedName;
  }


  public MethodCallExpr getFieldAccessCallExpr(BeanDefinition beanDefinition,
      VariableElement field) {
    if (context.getGenerationContext().getExecutionEnv().equals(ExecutionEnv.GWT2)) {
      return new MethodCallExpr(new NameExpr(beanDefinition.getType() + "Info"),
          field.getSimpleName().toString()).addArgument("instance");
    }

    return new MethodCallExpr(
        new MethodCallExpr(new NameExpr(Js.class.getSimpleName()), "asPropertyMap")
            .addArgument("instance"),
        "get").addArgument(
            new MethodCallExpr(new NameExpr(Reflect.class.getSimpleName()), "objectProperty")
                .addArgument(new StringLiteralExpr(Utils.getJsFieldName(field)))
                .addArgument("instance"));
  }

  public Expression beanManagerLookupBeanCall(InjectableVariableDefinition fieldPoint) {
    MethodCallExpr callForProducer = new MethodCallExpr(new NameExpr("beanManager"), "lookupBean")
        .addArgument(new FieldAccessExpr(new NameExpr(MoreTypes
            .asTypeElement(fieldPoint.getVariableElement().asType()).getQualifiedName().toString()),
            "class"));

    maybeAddQualifiers(context, callForProducer, fieldPoint);
    return callForProducer;
  }

  public void maybeAddQualifiers(IOCContext context, MethodCallExpr call,
      InjectableVariableDefinition field) {

    String annotationName = null;

    if (field.getVariableElement().getAnnotation(Named.class) != null) {
      annotationName = Named.class.getCanonicalName();
    } else if (isQualifier(field) != null) {
      annotationName = isQualifier(field);
    }

    if (annotationName != null) {
      ObjectCreationExpr annotation = new ObjectCreationExpr();
      annotation.setType(new ClassOrInterfaceType().setName(annotationName));
      NodeList<BodyDeclaration<?>> anonymousClassBody = new NodeList<>();

      MethodDeclaration annotationType = new MethodDeclaration();
      annotationType.setModifiers(Modifier.Keyword.PUBLIC);
      annotationType.setName("annotationType");
      annotationType.setType(
          new ClassOrInterfaceType().setName("Class<? extends java.lang.annotation.Annotation>"));
      annotationType.getBody().get()
          .addAndGetStatement(new ReturnStmt(new NameExpr(annotationName + ".class")));
      anonymousClassBody.add(annotationType);

      if (field.getVariableElement().getAnnotation(Named.class) != null) {
        MethodDeclaration value = new MethodDeclaration();
        value.setModifiers(Modifier.Keyword.PUBLIC);
        value.setName("value");
        value.setType(new ClassOrInterfaceType().setName("String"));
        value.getBody().get().addAndGetStatement(new ReturnStmt(
            new StringLiteralExpr(field.getVariableElement().getAnnotation(Named.class).value())));
        anonymousClassBody.add(value);
      }

      annotation.setAnonymousClassBody(anonymousClassBody);

      call.addArgument(annotation);


    }
  }


  public String isQualifier(InjectableVariableDefinition field) {
    for (AnnotationMirror ann : field.getVariableElement().getAnnotationMirrors()) {
      for (AnnotationMirror e : context.getGenerationContext().getProcessingEnvironment()
          .getElementUtils()
          .getAllAnnotationMirrors(MoreTypes.asElement(ann.getAnnotationType()))) {
        boolean same =
            context.getGenerationContext().getTypes().isSameType(e.getAnnotationType(), qualifier);
        if (same) {
          return ann.getAnnotationType().toString();
        }
      }
    }
    return null;
  }

  public Expression wrapCallInstanceImpl(ClassBuilder classBuilder, Expression call) {
    classBuilder.getClassCompilationUnit().addImport(InstanceImpl.class);
    LambdaExpr lambda = new LambdaExpr();
    lambda.setEnclosingParameters(true);
    lambda.setBody(new ExpressionStmt(call));

    return new ObjectCreationExpr().setType(InstanceImpl.class).addArgument(call);
  }

  public Statement generateMethodCall(ExecutableElement method, Expression... args) {
    if (method.getModifiers().contains(javax.lang.model.element.Modifier.PRIVATE)) {
      if (context.getGenerationContext().getExecutionEnv().equals(ExecutionEnv.JRE)) {
        return generatePrivateJREMethodCall(method, args);
      }

      if (context.getGenerationContext().getExecutionEnv().equals(ExecutionEnv.J2CL)) {
        return generatePrivateJ2CLMethodCall(method, args);
      }

      throw new Error("Private method calls aren't supported for GWT2");
    } else {
      FieldAccessExpr instance = new FieldAccessExpr(new ThisExpr(), "instance");
      MethodCallExpr result = new MethodCallExpr(instance, method.getSimpleName().toString());
      for (Expression arg : args) {
        result.addArgument(
            new MethodCallExpr(new NameExpr(Js.class.getCanonicalName()), "uncheckedCast")
                .addArgument(arg));
      }
      return new ExpressionStmt(result);
    }
  }

  private Statement generatePrivateJ2CLMethodCall(ExecutableElement method, Expression[] args) {
    String valueName = Utils.getJsMethodName(method);

    MethodCallExpr bind = new MethodCallExpr(new EnclosedExpr(new CastExpr(
        new ClassOrInterfaceType().setName("elemental2.core.Function"),
        new MethodCallExpr(new NameExpr("elemental2.core.Reflect"), "get").addArgument("instance")
            .addArgument(
                new MethodCallExpr(new NameExpr("io.crysknife.client.Reflect"), "objectProperty")
                    .addArgument(new StringLiteralExpr(valueName)).addArgument("instance")))),
        "bind").addArgument("instance");

    for (Expression arg : args) {
      bind.addArgument(arg);
    }

    return new ExpressionStmt(new MethodCallExpr(bind, "call"));
  }

  private TryStmt generatePrivateJREMethodCall(ExecutableElement method, Expression[] args) {
    ThrowStmt throwStmt = new ThrowStmt(new ObjectCreationExpr()
        .setType(new ClassOrInterfaceType().setName("Error")).addArgument("e"));

    TryStmt ts = new TryStmt();
    BlockStmt blockStmt = new BlockStmt();


    MethodCallExpr getDeclaredMethod =
        new MethodCallExpr(new NameExpr(MethodUtils.class.getCanonicalName()), "getMatchingMethod");
    getDeclaredMethod.addArgument(method.getEnclosingElement().toString() + ".class");
    getDeclaredMethod.addArgument(new StringLiteralExpr(method.getSimpleName().toString()));

    method.getParameters().forEach(param -> {
      getDeclaredMethod.addArgument(param.asType().toString() + ".class");
    });

    blockStmt.addAndGetStatement(new AssignExpr()
        .setTarget(new VariableDeclarationExpr(
            new ClassOrInterfaceType().setName(Method.class.getCanonicalName()), "method"))
        .setValue(getDeclaredMethod));

    blockStmt.addAndGetStatement(
        new MethodCallExpr(new NameExpr("method"), "setAccessible").addArgument("true"));
    MethodCallExpr invoke =
        new MethodCallExpr(new NameExpr("method"), "invoke").addArgument("instance");
    for (Expression arg : args) {
      invoke.addArgument(arg);
    }

    blockStmt.addAndGetStatement(invoke);
    CatchClause catchClause1 = new CatchClause().setParameter(
        new Parameter().setType(new ClassOrInterfaceType().setName("Exception")).setName("e"));
    catchClause1.getBody().addAndGetStatement(throwStmt);
    ts.getCatchClauses().add(catchClause1);
    ts.setTryBlock(blockStmt);

    return ts;
  }
}
