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

package io.crysknife.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Qualifier;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

import com.google.auto.common.MoreElements;
import io.crysknife.generator.context.IOCContext;
import jsinterop.annotations.JsProperty;
import io.crysknife.exception.GenerationException;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 2/21/19
 */
public class Utils {

  private Utils() {

  }

  public static String getQualifiedFactoryName(TypeElement bean) {
    return getPackageName(bean) + "." + getFactoryClassName(bean);
  }

  public static String getPackageName(TypeElement singleton) {
    return MoreElements.getPackage(singleton).getQualifiedName().toString();
  }

  public static String getFactoryClassName(TypeElement bean) {
    return (bean.getEnclosingElement().getKind().equals(ElementKind.PACKAGE) ? ""
        : (bean.getEnclosingElement().getSimpleName() + "_")) + bean.getSimpleName().toString()
        + "_Factory";
  }

  public static String getSimpleClassName(TypeElement bean) {
    return (bean.getEnclosingElement().getKind().equals(ElementKind.PACKAGE) ? ""
        : (bean.getEnclosingElement().getSimpleName() + ".")) + bean.getSimpleName().toString();
  }

  public static String getJsFieldName(VariableElement field) {
    if (field.getAnnotation(JsProperty.class) != null) {
      return field.getSimpleName().toString();
    }
    StringBuffer sb = new StringBuffer();
    sb.append("f_");
    sb.append(field.getSimpleName().toString());
    sb.append("__");
    sb.append(MoreElements.asType(field.getEnclosingElement()).getQualifiedName().toString()
        .replaceAll("\\.", "_"));
    if (field.getModifiers().contains(Modifier.PRIVATE)) {
      sb.append("_");
    }

    return sb.toString();
  }

  public static Set<Element> getAnnotatedElements(Elements elements, TypeElement type,
      Class<? extends Annotation> annotation) {
    Set<Element> found = new HashSet<>();
    for (Element e : elements.getAllMembers(type)) {
      if (e.getAnnotation(annotation) != null) {
        found.add(e);
      }
    }
    return found;
  }

  public static String toVariableName(TypeElement injection) {
    return toVariableName(getQualifiedName(injection));
  }

  public static String toVariableName(String name) {
    return name.toLowerCase().replaceAll("\\.", "_");
  }

  public static String getQualifiedName(Element elm) {
    if (elm.getKind().equals(ElementKind.FIELD) || elm.getKind().equals(ElementKind.PARAMETER)) {
      VariableElement variableElement = MoreElements.asVariable(elm);
      DeclaredType declaredType = (DeclaredType) variableElement.asType();
      return declaredType.toString();
    } else if (elm.getKind().equals(ElementKind.CONSTRUCTOR)) {
      ExecutableElement executableElement = MoreElements.asExecutable(elm);
      return executableElement.getEnclosingElement().toString();
    } else if (elm.getKind().isClass() || elm.getKind().isInterface()) {
      TypeElement typeElement = MoreElements.asType(elm);
      return typeElement.getQualifiedName().toString();
    }
    throw new GenerationException("Unable to process bean " + elm.toString());
  }

  public static List<AnnotationMirror> getAllElementQualifierAnnotations(IOCContext context,
      Element element) {
    System.out.println("element " + element);

    List<AnnotationMirror> result = new ArrayList<>();
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      for (AnnotationMirror allAnnotationMirror : context.getGenerationContext().getElements()
          .getAllAnnotationMirrors(annotationMirror.getAnnotationType().asElement())) {
        if (isAnnotationMirrorOfType(allAnnotationMirror,
            javax.inject.Qualifier.class.getCanonicalName())) {
          result.add(allAnnotationMirror);
        }
      }
    }
    return result;
  }


  /**
   * @url {https://github.com/hibernate/hibernate-metamodelgen/blob/master/src/main/java/org/hibernate/jpamodelgen/util/TypeUtils.java}
   */
  public static boolean containsAnnotation(Element element, String... annotations) {
    assert element != null;
    assert annotations != null;

    List<String> annotationClassNames = new ArrayList<>();
    Collections.addAll(annotationClassNames, annotations);

    List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
    for (AnnotationMirror mirror : annotationMirrors) {
      if (annotationClassNames.contains(mirror.getAnnotationType().toString())) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @url {https://github.com/hibernate/hibernate-metamodelgen/blob/master/src/main/java/org/hibernate/jpamodelgen/util/TypeUtils.java}
   *
   *      Returns {@code true} if the provided annotation type is of the same type as the provided
   *      class, {@code false} otherwise. This method uses the string class names for comparison.
   *      See also <a href=
   *      "http://www.retep.org/2009/02/getting-class-values-from-annotations.html">getting-class-values-from-annotations</a>.
   *
   * @param annotationMirror The annotation mirror
   * @param fqcn the fully qualified class name to check against
   *
   * @return {@code true} if the provided annotation type is of the same type as the provided class,
   *         {@code false} otherwise.
   */
  public static boolean isAnnotationMirrorOfType(AnnotationMirror annotationMirror, String fqcn) {
    assert annotationMirror != null;
    assert fqcn != null;
    String annotationClassName = annotationMirror.getAnnotationType().toString();

    return annotationClassName.equals(fqcn);
  }

  /**
   * @url {https://github.com/hibernate/hibernate-metamodelgen/blob/master/src/main/java/org/hibernate/jpamodelgen/util/TypeUtils.java}
   */
  public static Object getAnnotationValue(AnnotationMirror annotationMirror,
      String parameterValue) {
    assert annotationMirror != null;
    assert parameterValue != null;

    Object returnValue = null;
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror
        .getElementValues().entrySet()) {
      if (parameterValue.equals(entry.getKey().getSimpleName().toString())) {
        returnValue = entry.getValue().getValue();
        break;
      }
    }
    return returnValue;
  }

}
