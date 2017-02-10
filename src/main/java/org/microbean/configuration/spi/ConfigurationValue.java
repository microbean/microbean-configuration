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

public class ConfigurationValue implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Configuration configuration;
  
  private final Map<String, String> coordinates;

  private final String name;
  
  private final String value;

  private final boolean authoritative;

  public ConfigurationValue(final Configuration configuration, final Map<String, String> coordinates, final String name, final String value) {
    this(configuration, coordinates, name, value, false);
  }
  
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

  public Configuration getConfiguration() {
    return this.configuration;
  }

  public Map<String, String> getCoordinates() {
    return this.coordinates;
  }

  public String getName() {
    return this.name;
  }
  
  public String getValue() {
    return this.value;
  }

  public boolean isAuthoritative() {
    return this.authoritative;
  }

  public final int specificity() {
    int size = 0;
    final Map<?, ?> coordinates = this.getCoordinates();
    if (coordinates != null) {
      size = coordinates.size();
    }
    return size;
  }
  
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
    return sb.toString();
  }
  
}
