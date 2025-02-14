/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package io.crysknife.ui.navigation.client.local.spi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;

import io.crysknife.client.internal.collections.Multimap;
import io.crysknife.client.utils.CreationalCallback;
import io.crysknife.ui.navigation.client.local.HistoryToken;
import io.crysknife.ui.navigation.client.local.Navigation;
import io.crysknife.ui.navigation.client.local.Page;
import io.crysknife.ui.navigation.client.local.TransitionTo;
import io.crysknife.ui.navigation.client.local.api.NavigationControl;

/**
 * Represents a page (a distinct place that can be navigated to and bookmarked to return to later).
 * <p>
 * Thinking of the application flow as a directed graph, Pages are the nodes and
 * {@link TransitionTo}s are the edges.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface PageNode<P> {

  /**
   * Returns the name of this page.
   *
   * @return This page's name. Never null.
   */
  String name();

  /**
   * Returns the URL template specified for this page by {@link Page#path()}. If no template is
   * specified, it returns the page name.
   *
   * @return This page's URL. Never null.
   */
  String getURL();

  /**
   * Retrieves the widget that provides this page's content from the client-side bean manager.
   *
   * @param callback The callback that will receive the widget to display for this page. The Widget
   *        will have the same runtime type as returned by {@link #contentType()}, and will never be
   *        null.
   */
  void produceContent(CreationalCallback<P> callback);

  /**
   * Returns the type of widget that this page node's {@link #produceContent(CreationalCallback)}
   * method will produce.
   *
   * @return The type of widget that supplies this page's content. Never null.
   */
  Class<P> contentType();

  /**
   * Called by the framework when this page node is about to be displayed in the navigation content
   * panel.
   * <p>
   * If this method throws an exception when called, framework behaviour is undefined.
   *
   * @param page the page instance that was just returned from a call to
   *        {@link #produceContent(CreationalCallback)}. Never null.
   * @param state the state of the page, parsed from the history token on the URL. Never null.
   */
  void pageShowing(P page, HistoryToken state, NavigationControl control);

  /**
   * Called by the framework when this page node was displayed in the navigation content panel.
   * <p>
   * If this method throws an exception when called, framework behaviour is undefined.
   *
   * @param page the page instance that was just returned from a call to
   *        {@link #produceContent(CreationalCallback)}. Never null.
   * @param state the state of the page, parsed from the history token on the URL. Never null.
   */
  void pageShown(P page, HistoryToken state);

  /**
   * Called by the framework when this page node is about to be removed from the navigation content
   * panel.
   * <p>
   * If this method throws an exception when called, framework behaviour is undefined.
   *
   * @param page the page instance (which is currently in the navigation content panel) that was
   *        previously used in the call to {@link #pageShowing(P, HistoryToken, NavigationControl)}.
   *        Never null.
   */
  void pageHiding(P page, NavigationControl control);

  /**
   * Called by the framework after this page has been removed from the navigation content panel.
   * <p>
   * If this method throws an exception when called, framework behaviour is undefined.
   *
   * @param page the page instance (which was in the navigation content panel) that was previously
   *        used in the call to {@link #pageShowing(P, HistoryToken, NavigationControl)}. Never
   *        null.
   */
  void pageHidden(P page);

  /**
   * Called by the framework when this page node state was updated
   * {@link Navigation#updateState(Multimap)}.
   * <p>
   * If this method throws an exception when called, framework behaviour is undefined.
   *
   * @param page the page instance that was just returned from a call to
   *        {@link #produceContent(CreationalCallback)}. Never null.
   * @param state the state of the page, parsed from the history token on the URL. Never null.
   */
  void pageUpdate(P page, HistoryToken state);

  /**
   * Used by the framework to destroy {@link Dependent} scoped beans after a page is no longer
   * needed. For {@link ApplicationScoped} beans this method is a noop.
   *
   * @param page The page instance that will be destroyed if it is a dependent-scoped bean. Never
   *        null.
   */
  void destroy(P page);
}
