/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

/**
 * Indicates that the implementing widget displays an instance of type <M>.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 *
 * @param <M> the model type.
 */
public interface HasModel<M> {

  /**
   * Returns the model instance associated with this widget.
   *
   * @return the model instance, or null if no instance is associated with this widget.
   */
  public M getModel();

  /**
   * Associate the model instance with this widget.
   *
   * @param model the model instance.
   */
  public void setModel(M model);
}
