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

import java.io.Serializable;

import java.util.Map;

import org.microbean.configuration.Configurations;

import org.microbean.configuration.api.ConfigurationValue;

/**
 * An accessor of {@link ConfigurationValue}s in configuration space.
 *
 * <p>{@link Configuration} instances are typically controlled in the
 * service of a governing {@link Configurations} object.</p>
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #getValue(Map, String)
 *
 * @see ConfigurationValue
 *
 * @see Configurations#loadConfigurations()
 */
public interface Configuration {

  /**
   * Installs a {@link Configurations} object into this {@link
   * Configuration} implementation.
   *
   * @param configurations the {@link Configurations} to install; must
   * not be {@code null}
   *
   * @exception NullPointerException if {@code configurations} is
   * {@code null}
   */
  public void setConfigurations(final Configurations configurations);

  /**
   * Returns a {@link ConfigurationValue} suitable for the supplied
   * {@code configurationCoordinates} and {@code name}, or {@code
   * null} if there is no suitable value.
   *
   * <p>Implementations of this method may return {@code null}.</p>
   *
   * <p>The {@link ConfigurationValue} that is returned must
   * {@linkplain ConfigurationValue#getName() have a name} that is
   * equal to the supplied {@code name}.</p>
   *
   * <p>The {@link ConfigurationValue} that is returned must be
   * {@linkplain ConfigurationValue#ConfigurationValue(Serializable,
   * Map, String, String, boolean) created} with this {@link
   * Configuration} as the first parameter value supplied to its
   * {@linkplain ConfigurationValue#ConfigurationValue(Serializable,
   * Map, String, String, boolean) constructor}.</p>
   *
   * <p>The {@link ConfigurationValue} that is returned must be
   * {@linkplain ConfigurationValue#ConfigurationValue(Serializable,
   * Map, String, String, boolean) created} with a {@linkplain
   * ConfigurationValue#getCoordinates() set of configuration
   * coordinates} that is a subset of the supplied {@code
   * configurationCoordinates}.</p>
   *
   * @param configurationCoordinates the configuration coordinates for
   * which a value for the relevant configuration property, identified
   * by the supplied {@code name}, should be returned; may be {@code
   * null}
   *
   * @param name the name of the configuration property for which a
   * {@link ConfigurationValue} will be returned; must not be {@code
   * null}
   *
   * @return a {@link ConfigurationValue} suitable for the supplied
   * {@code configurationCoordinates} and {@code name}, or {@code
   * null}
   *
   * @exception NullPointerException if {@code name} is {@code null}
   *
   * @see ConfigurationValue
   */
  public ConfigurationValue getValue(final Map<String, String> configurationCoordinates, final String name);
  
}
