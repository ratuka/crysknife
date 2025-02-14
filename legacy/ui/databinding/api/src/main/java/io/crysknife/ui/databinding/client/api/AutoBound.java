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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is only meaningful on a {@link DataBinder} field or constructor/method parameter.
 * <p>
 * It indicates that the annotated {@link DataBinder} is used to automatically bind all
 * corresponding widgets to properties of a data model (the model instance associated with the data
 * binder instance). The widgets are inferred from all enclosing fields and methods annotated with
 * {@link org.jboss.errai.ui.shared.api.annotations.Bound} of the class that defines the
 * {@link DataBinder} and all its super classes.
 * <p>
 * There can only be one auto bound {@link DataBinder} per class.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoBound {

}
