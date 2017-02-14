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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.microbean.configuration.Configurations; // for javadoc only

/**
 * A value for a configuration property as returned by a {@link
 * Configuration} in the service of a {@link Configurations} instance.
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Configuration#getValue(Map, String)
 */
public class ConfigurationValue implements Serializable {


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
   * The {@link Configuration} that produced this {@link
   * ConfigurationValue}.
   *
   * <p>This field may be {@code null}.</p>
   */
  private transient Configuration configuration;

  /**
   * A {@link Map} representing the specific configuration coordinates
   * this {@link ConfigurationValue} is selected for.
   *
   * <p>This field may be {@code null}.</p>
   */
  private final Map<String, String> coordinates;

  /**
   * The name of the configuration property for which this {@link
   * ConfigurationValue} is a value.
   *
   * <p>This field is never {@code null}.</p>
   */
  private final String name;

  /**
   * The actual configuration value represented by this {@link
   * ConfigurationValue}.
   *
   * <p>This field may be {@code null}.</p>
   */
  private final String value;

  /**
   * Whether or not this {@link ConfigurationValue} is to be regarded
   * as the authoritative value for the configuration property in
   * question.
   */
  private final boolean authoritative;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link ConfigurationValue}.
   *
   * @param configuration the {@link Configuration} creating this
   * {@link ConfigurationValue}; must not be {@code null}
   *
   * @param coordinates the configuration coordinates to which this
   * {@link ConfigurationValue} applies; must be a subset of the
   * configuration coordinates that resulted in this {@link
   * ConfigurationValue} being created; may be {@code null}
   *
   * @param name the name of the configuration property for which this
   * is a value; must not be {@code null}
   *
   * @param value the value; may be {@code null}
   *
   * @exception NullPointerException if either {@code configuration}
   * or {@code name} is {@code null}
   *
   * @see #ConfigurationValue(Configuration, Map, String, String, boolean)
   */
  public ConfigurationValue(final Configuration configuration, final Map<String, String> coordinates, final String name, final String value) {
    this(configuration, coordinates, name, value, false);
  }

   /**
   * Creates a new {@link ConfigurationValue}.
   *
   * @param configuration the {@link Configuration} creating this
   * {@link ConfigurationValue}; must not be {@code null}
   *
   * @param coordinates the configuration coordinates to which this
   * {@link ConfigurationValue} applies; must be a subset of the
   * configuration coordinates that resulted in this {@link
   * ConfigurationValue} being created; may be {@code null}
   *
   * @param name the name of the configuration property for which this
   * is a value; must not be {@code null}
   *
   * @param value the value; may be {@code null}
   *
   * @param authoritative whether this {@link ConfigurationValue} is
   * to be considered authoritative
   *
   * @exception NullPointerException if either {@code configuration}
   * or {@code name} is {@code null}
   */
  public ConfigurationValue(final Configuration configuration, final Map<String, String> coordinates, final String name, final String value, final boolean authoritative) {
    super();
    Objects.requireNonNull(configuration);
    Objects.requireNonNull(name);
    this.configuration = configuration;
    if (coordinates == null || coordinates.isEmpty()) {
      this.coordinates = Collections.emptyMap();
    } else {
      this.coordinates = Collections.unmodifiableMap(new HashMap<>(coordinates));
    }
    this.name = name;
    this.value = value;
    this.authoritative = authoritative;
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the {@link Configuration} that created this {@link
   * ConfigurationValue}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link Configuration} that created this {@link
   * ConfigurationValue}; never {@code null}
   */
  public final Configuration getConfiguration() {
    return this.configuration;
  }

  /**
   * Returns the configuration coordinates locating this {@link
   * ConfigurationValue} in configuration space.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the configuration coordinates locating this {@link
   * ConfigurationValue} in configuration space, or {@code null}
   */
  public final Map<String, String> getCoordinates() {
    return this.coordinates;
  }

  /**
   * Returns the name of the configuration property for which this
   * {@link ConfigurationValue} is a value.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the name of the configuration property for which this
   * {@link ConfigurationValue} is a value; never {@code null}
   */
  public final String getName() {
    return this.name;
  }

  /**
   * Returns the actual value that this {@link ConfigurationValue}
   * represents.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the actual value that this {@link ConfigurationValue}
   * represents, or {@code null}
   */
  public final String getValue() {
    return this.value;
  }

  /**
   * Returns {@code true} if this {@link ConfigurationValue} is to be
   * regarded as authoritative.
   *
   * @return {@code true} if this {@link ConfigurationValue} is to be
   * regarded as authoritative; {@code false} otherwise
   */
  public final boolean isAuthoritative() {
    return this.authoritative;
  }

  /**
   * Returns the <em>specificity</em> of this {@link
   * ConfigurationValue}.
   *
   * <p>The specificity of a {@link ConfigurationValue} is equal to
   * the {@linkplain Map#size() size} of its {@linkplain
   * #getCoordinates() configuration coordinates
   * <code>Map</code>}.</p>
   *
   * @return the specificity of this {@link ConfigurationValue};
   * always zero or a positive integer
   */
  public final int specificity() {
    int size = 0;
    final Map<?, ?> coordinates = this.getCoordinates();
    if (coordinates != null) {
      size = coordinates.size();
    }
    return size;
  }

  /**
   * Returns a hashcode for this {@link ConfigurationValue}.
   *
   * @return a hashcode for this {@link ConfigurationValue}
   */
  @Override
  public int hashCode() {
    int hashCode = 17;

    // Note: getConfiguration() and isAuthoritative() are deliberately
    // omitted from hashCode calculation.
    
    final Object coordinates = this.getCoordinates();
    int c = coordinates == null ? 0 : coordinates.hashCode();
    hashCode = 37 * hashCode + c;

    final Object name = this.getName();
    c = name == null ? 0 : name.hashCode();
    hashCode = 37 * hashCode + c;

    final Object value = this.getValue();
    c = value == null ? 0 : value.hashCode();
    hashCode = 37 * hashCode + c;
    
    return hashCode;
  }

  /**
   * Returns {@code true} if the supplied {@link Object} is equal to
   * this {@link ConfigurationValue}.
   *
   * <p>An {@link Object} is equal to a {@link ConfigurationValue}
   * if:</p>
   *
   * <ul>
   *
   * <li>It is an instance of {@link ConfigurationValue}</li>
   *
   * <li>Its {@linkplain #getName() name} and {@link #getCoordinates()
   * coordinates} are equal to those of the {@link ConfigurationValue}
   * to which it is being compared</li>
   *
   * <li>Its {@linkplain #getValue() value} is equal to that of the
   * {@link ConfigurationValue} to which it is being compared</li>
   *
   * </ul>
   *
   * @param other the {@link Object} to test; may be {@code null}
   *
   * @return {@code true} if the supplied {@link Object} is equal to
   * this {@link ConfigurationValue}; {@code false} otherwise
   */
  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof ConfigurationValue) {
      final ConfigurationValue her = (ConfigurationValue)other;

      // Note: getConfiguration() and isAuthoritative() are
      // deliberately omitted from the algorithm.
      
      final Object coordinates = this.getCoordinates();
      if (coordinates == null) {
        if (her.getCoordinates() != null) {
          return false;
        }
      } else if (!coordinates.equals(her.getCoordinates())) {
        return false;
      }
      
      final Object name = this.getName();
      if (name == null) {
        if (her.getName() != null) {
          return false;
        }
      } else if (!name.equals(her.getName())) {
        return false;
      }

      final Object value = this.getValue();
      if (value == null) {
        if (her.getValue() != null) {
          return false;
        }
      } else if (!value.equals(her.getValue())) {
        return false;
      }

      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link ConfigurationValue}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link ConfigurationValue}
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("(");
    sb.append(this.getConfiguration());
    sb.append(") ");
    final Map<String, String> coordinates = this.getCoordinates();
    if (coordinates != null && !coordinates.isEmpty()) {
      sb.append(coordinates);
      sb.append(" ");
    }
    sb.append(this.getName());
    sb.append("=");
    sb.append(this.getValue());
    final boolean authoritative = this.isAuthoritative();
    if (authoritative) {
      sb.append(" (authoritative)");
    }
    return sb.toString();
  }
  
}
