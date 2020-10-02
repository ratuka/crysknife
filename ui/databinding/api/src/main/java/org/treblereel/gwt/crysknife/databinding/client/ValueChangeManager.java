/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.treblereel.gwt.crysknife.databinding.client;

import org.gwtproject.event.dom.client.ChangeEvent;
import org.gwtproject.event.dom.client.ChangeHandler;
import org.gwtproject.event.legacy.shared.GwtEvent;
import org.gwtproject.event.logical.shared.HasValueChangeHandlers;
import org.gwtproject.event.logical.shared.ValueChangeEvent;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.Event;
import org.gwtproject.event.shared.HandlerRegistration;
import org.gwtproject.user.client.ui.HasValue;
import org.gwtproject.user.client.ui.Widget;

/**
 * Utility class for managing widgets implementing {@link HasValue}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ValueChangeManager<T, W extends Widget & HasValue<T>>
    implements HasValueChangeHandlers<T> {

  private boolean valueChangeHandlerInitialized;
  private final W widget;

  public ValueChangeManager(final W widget) {
    this.widget = widget;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<T> handler) {
    if (!valueChangeHandlerInitialized) {
      valueChangeHandlerInitialized = true;
      widget.addDomHandler(new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
          ValueChangeEvent.fire(widget, widget.getValue());
        }
      }, ChangeEvent.getType());
    }
    return widget.addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public void fireEvent(Event<?> event) {
    widget.fireEvent(event);
  }
}
