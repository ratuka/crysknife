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

package io.crysknife.client.internal;

import io.crysknife.client.BeanManager;
import io.crysknife.client.IOCBeanDef;
import io.crysknife.client.ManagedInstance;
import io.crysknife.client.SyncBeanDef;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Dmitrii Tikhomirov Created by treblereel 4/25/21
 */
public class ManagedInstanceImpl<T> implements ManagedInstance<T> {

  private final BeanManager beanManager;

  private final Class<T> type;

  private Annotation[] qualifiers;

  public ManagedInstanceImpl(BeanManager beanManager, Class<T> type) {
    this(beanManager, type, new Annotation[] {});
  }

  public ManagedInstanceImpl(BeanManager beanManager, Class<T> type, Annotation... qualifiers) {
    this.type = type;
    this.beanManager = beanManager;
    this.qualifiers = qualifiers;
  }

  @Override
  public ManagedInstance<T> select(Annotation... annotations) {
    return new ManagedInstanceImpl<>(beanManager, type, annotations);
  }

  @Override
  public <U extends T> ManagedInstance<U> select(Class<U> subtype, Annotation... qualifiers) {
    return new ManagedInstanceImpl(beanManager, subtype, qualifiers);
  }

  @Override
  public boolean isUnsatisfied() {
    if (qualifiers == null || qualifiers.length == 0) {
      qualifiers = new Annotation[] {QualifierUtil.DEFAULT_ANNOTATION};
    }

    Collection<IOCBeanDef<T>> result =
        ((AbstractBeanManager) beanManager).doLookupBean(type, qualifiers);
    return result.size() != 1;
  }

  @Override
  public boolean isAmbiguous() {
    return beanManager.lookupBeans(type, qualifiers).stream().count() > 1;
  }

  @Override
  public void destroy(T instance) {

  }

  @Override
  public void destroyAll() {

  }

  @Override
  public Iterator<T> iterator() {
    return new ManagedInstanceImplIterator<>(beanManager.lookupBeans(type, qualifiers));
  }

  @Override
  public T get() {
    if (qualifiers.length == 0) {
      qualifiers = new Annotation[] {QualifierUtil.DEFAULT_ANNOTATION};
    }
    return beanManager.lookupBean(type, qualifiers).getInstance();
  }

  private static class ManagedInstanceImplIterator<T> implements Iterator<T> {

    private final Iterator<SyncBeanDef<T>> delegate;

    public ManagedInstanceImplIterator(final Collection<SyncBeanDef<T>> beans) {
      this.delegate = beans.iterator();
    }

    @Override
    public boolean hasNext() {
      return delegate.hasNext();
    }

    @Override
    public T next() {
      final SyncBeanDef<T> bean = delegate.next();
      final T instance = bean.getInstance();
      return instance;
    }
  }
}
