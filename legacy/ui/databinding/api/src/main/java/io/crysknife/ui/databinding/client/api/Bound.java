/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package io.crysknife.ui.databinding.client.api;

import elemental2.dom.Element;
import jsinterop.annotations.JsType;
import org.gwtproject.event.dom.client.KeyUpEvent;
import org.gwtproject.event.logical.shared.ValueChangeEvent;
import org.gwtproject.user.client.ui.HasText;
import org.gwtproject.user.client.ui.HasValue;
import org.gwtproject.user.client.ui.Widget;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated component (a {@link Widget}, {@link Element}, or native
 * {@link JsType} wrapping a DOM element) should automatically be bound to a property of a data
 * model associated with a {@link DataBinder} (see {@link AutoBound} and {@link Model}).
 * <p>
 * The annotated component can either be a field, method or constructor parameter or a method return
 * value. If it is a {@link Widget} or a templated Errai UI bean then it must implement either
 * {@link HasText}, {@link HasValue} or {@link TakesValue}. Note that a {@link Bound} field can but
 * does not have to be injected. The following example shows all valid use cases for the
 * {@link Bound} annotation.
 *
 * <pre>
 *      public class MyBean {
 *        {@code @Inject} {@code @Model}
 *        private MyModel model;
 *
 *        {@code @Bound}
 *        private Label boundLabel = new Label();
 *
 *        {@code @Inject} {@code @Bound}
 *        private TextBox injectedBoundTextBox;
 *
 *        {@code @Inject}
 *        public MyBean({@code @Bound} SomeWidget boundWidget) {
 *          this.boundWidget = boundWidget;
 *        }
 *
 *        {@code @Inject}
 *        public void setWidget({@code @Bound} SomeWidget boundWidget) {
 *          this.boundWidget = boundWidget;
 *        }
 *
 *        {@code @Bound}
 *        public SomeWidget getWidget() {
 *          ...
 *        }
 *      }
 * </pre>
 *
 * If no property is specified, the component is bound to the data model property with the same name
 * as the field, parameter, or method which is the target of this annotation.
 * <p>
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("rawtypes")
public @interface Bound {

  /**
   * The name of the data model property (or a property chain) to bind the component to, following
   * Java bean conventions. If omitted, the widget will be bound to the data model property with the
   * same name as the field, parameter or method which is the target of this annotation.
   */
  String property() default "";

  /**
   * The {@link Converter} to use when setting values on the model or component.
   */
  // The NO_CONVERTER class needs to be fully qualified here to work around a
  // JDK bug: http://bugs.sun.com/view_bug.do?bug_id=6512707
  Class<? extends Converter> converter() default NO_CONVERTER.class;

  static abstract class NO_CONVERTER implements Converter {
  }

  /**
   * A flag indicating whether or not the data model property should be updated when the widget
   * fires a {@link KeyUpEvent}, in addition to the default {@link ValueChangeEvent}.
   */
  boolean onKeyUp() default false;
}
