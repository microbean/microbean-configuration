/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017–2019 microBean.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.configuration.spi;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import java.util.function.Function;

import org.microbean.configuration.spi.AbstractResourceLoadingConfiguration.Resource;

public class CachingResourceLoader<T> implements Function<Map<? extends String, ? extends String>, Resource<? extends T>> {

  private final Function<? super Map<? extends String, ? extends String>, ? extends Resource<? extends T>> delegate;

  private final Map<Map<? extends String, ? extends String>, Resource<? extends T>> cache;
  
  public CachingResourceLoader(final Function<? super Map<? extends String, ? extends String>, ? extends Resource<? extends T>> delegate) {
    super();
    this.delegate = delegate;
    this.cache = delegate == null ? null : new ConcurrentHashMap<>();
  }

  public Resource<? extends T> apply(final Map<? extends String, ? extends String> requestedConfigurationCoordinates) {
    return this.cache == null ? null : this.cache.computeIfAbsent(requestedConfigurationCoordinates, k -> this.delegate.apply(k));
  }

}
