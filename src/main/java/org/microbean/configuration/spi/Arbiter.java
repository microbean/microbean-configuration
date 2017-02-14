/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017 MicroBean.
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

import java.util.Collection;
import java.util.Collections; // for javadoc only
import java.util.Map;

/**
 * A resolver of ambiguous {@link ConfigurationValue}s.
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #arbitrate(Map, String, Collection)
 */
public interface Arbiter {

  /**
   * Given a logical request for a configuration value, represented by
   * the {@code callerCoordinates} and {@code name} parameter values,
   * and a {@link Collection} of {@link ConfigurationValue} instances
   * that represents the ambiguous response from several {@link
   * Configuration} instances, attempts to resolve the ambiguity by
   * returning a single {@link ConfigurationValue} instead.
   *
   * <p>Implementations of this method may return {@code null}.</p>
   *
   * <p>A special case is when the supplied {@code ambiguousValues}
   * parameter is {@code null} or {@linkplain Collection#isEmpty()
   * empty}.  This means, effectively, that all consulted {@link
   * Configuration} instances returned {@code null} from their {@link
   * Configuration#getValue(Map, String)} methods.  An {@link Arbiter}
   * encountering this state of affairs and returning a single
   * non-{@code null} {@link ConfigurationValue} is effectively
   * synthesizing a default value.</p>
   *
   * @param requestedCoordinates the ({@linkplain
   * Collections#unmodifiableMap(Map) immutable}) configuration
   * coordinates in effect for the request; may be {@code null}
   *
   * @param name the name of the configuration value; must not be
   * {@code null}
   *
   * @param ambiguousValues an {@linkplain
   * Collections#unmodifiableCollection(Collection) immutable} {@link
   * Collection} of definitionally ambiguous {@link
   * ConfigurationValue}s that resulted from the request; may be
   * {@code null}
   *
   * @return the result of arbitration, or {@code null} if the dispute
   * cannot be arbitrated by this {@link Arbiter}
   *
   * @exception NullPointerException if {@code name} is {@code null}
   *
   * @exception AmbiguousConfigurationValuesException if overall
   * arbitration is to be cut short by this {@link
   * Arbiter}&mdash;normally an {@link Arbiter} should simply return
   * {@code null}
   */
  public ConfigurationValue arbitrate(final Map<? extends String, ? extends String> requestedCoordinates,
                                      final String name,
                                      final Collection<? extends ConfigurationValue> ambiguousValues);
  
}
