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

package org.treblereel.client;

import javax.inject.Inject;
import javax.inject.Singleton;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import org.jboss.elemento.IsElement;
import org.treblereel.client.inject.Injector;
import org.treblereel.gwt.crysknife.templates.client.annotation.DataField;
import org.treblereel.gwt.crysknife.templates.client.annotation.EventHandler;
import org.treblereel.gwt.crysknife.templates.client.annotation.ForEvent;
import org.treblereel.gwt.crysknife.templates.client.annotation.Templated;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 2/22/19
 */
@Singleton
@Templated("transitiveinjection.html")
public class TransitiveInjection implements IsElement<HTMLDivElement> {

    @Inject
    @DataField
    private HTMLDivElement form;

    @Inject
    @DataField
    private HTMLInputElement textBox;

    @Inject
    @DataField
    private HTMLButtonElement checkBtn;

    @Inject
    private Injector injector;

    private void setText(String text) {
        textBox.value = text;
    }

    @Override
    public HTMLDivElement element() {
        return form;
    }

    @EventHandler("checkBtn")
    protected void onClick(@ForEvent("click") final MouseEvent event) {
        setText(injector.callBeanTwo());
    }
}

