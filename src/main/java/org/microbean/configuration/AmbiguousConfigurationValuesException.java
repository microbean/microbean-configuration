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
package org.microbean.configuration;

import java.io.Serializable; // for javadoc only

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.microbean.configuration.spi.ConfigurationValue;

/**
 * A {@link ConfigurationException} indicating that conflicting {@link
 * ConfigurationValue}s were found for a given configuration property
 * request.
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ConfigurationValue
 *
 * @see Configurations#performArbitration(Map, String, Collection)
 */
public class AmbiguousConfigurationValuesException extends ConfigurationException {


  /*
   * Static fields.
   */


  /**
   * The version of this class for {@linkplain Serializable
   * serialization} purposes.
   */
  private static final long serialVersionUID = 1L;


  /*
   * Instance fields.
   */

  
  /**
   * A {@link Collection} of conflicting {@link ConfigurationValue}s.
   *
   * <p>This field is never {@code null} but may be {@linkplain
   * Collection#isEmpty() empty}.</p>
   */
  private final Collection<? extends ConfigurationValue> values;

  /**
   * The configuration coordinates in effect at the time conflicting
   * values were found.
   *
   * <p>This field is never {@code null}.</p>
   */
  private final Map<? extends String, ? extends String> configurationCoordinates;

  /**
   * The name of the configuration property for which a value was requested.
   *
   * <p>This field is never {@code null}.</p>
   */
  private final String name;


  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link AmbiguousConfigurationValuesException}.
   *
   * @param message the error message; may be {@code null}
   *
   * @param cause the {@link Throwable} that caused this {@link
   * AmbiguousConfigurationValuesException} to be created; may be
   * {@code null}
   *
   * @param configurationCoordinates the configuration coordinates in
   * effect at the time conflicting values were found; may be {@code
   * null}
   *
   * @param name the name of the configuration property for which a
   * value was requested; must not be {@code null}
   *
   * @param values the conflicting values; may be {@code null}
   *
   * @exception NullPointerException if {@code name} is {@code null}
   */
  public AmbiguousConfigurationValuesException(final String message,
                                               final Throwable cause,
                                               final Map<? extends String, ? extends String> configurationCoordinates,
                                               final String name,
                                               final Collection<? extends ConfigurationValue> values) {
    super(message, cause);
    if (name == null) {
      final NullPointerException throwMe = new NullPointerException();
      throwMe.addSuppressed(this);
      throw throwMe;
    }
    this.configurationCoordinates = configurationCoordinates == null ? Collections.emptyMap() : Collections.unmodifiableMap(configurationCoordinates);
    this.name = name;
    this.values = values == null ? Collections.emptySet() : values;
  }

  /**
   * Returns the {@link Collection} of conflicting values that caused
   * this {@link AmbiguousConfigurationValuesException} to be created.
   *
   * <p>This method will never return {@code null}.</p>
   *
   * @return a non-{@code null} {@link Collection} of {@link
   * ConfigurationValue}s
   */
  public final Collection<? extends ConfigurationValue> getValues() {
    return this.values;
  }

  /**
   * Returns a {@link Map} representing the configuration coordinates
   * in effect when this {@link AmbiguousConfigurationValuesException}
   * was created.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a non-{@code null} {@link Map} of configuration
   * coordinates
   */
  public final Map<? extends String, ? extends String> getCoordinates() {
    return this.configurationCoordinates;
  }

  /**
   * Returns the name of the configuration property for which a value was requested.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the name of the configuration property for which a value
   * was requested; never {@code null}
   */
  public final String getName() {
    return this.name;
  }

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link AmbiguousConfigurationValuesException}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link AmbiguousConfigurationValuesException}
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(super.toString());
    sb.append(" ");
    sb.append(this.getCoordinates());
    sb.append(" ");
    sb.append(this.getName());
    sb.append(" \u27a1 ");
    sb.append(this.getValues());
    return sb.toString();
  }
  
}
