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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.microbean.configuration.spi.ConfigurationValue;

public class AmbiguousConfigurationValuesException extends ConfigurationException {

  private static final long serialVersionUID = 1L;

  private final Collection<? extends ConfigurationValue> values;

  private final Map<? extends String, ? extends String> applicationCoordinates;

  private final String name;
  
  public AmbiguousConfigurationValuesException(final String message,
                                               final Throwable cause,
                                               final Map<? extends String, ? extends String> applicationCoordinates,
                                               final String name,
                                               final Collection<? extends ConfigurationValue> values) {
    super(message, cause);
    this.applicationCoordinates = applicationCoordinates == null ? Collections.emptyMap() : Collections.unmodifiableMap(applicationCoordinates);
    this.name = name;
    this.values = values == null ? Collections.emptySet() : values;
  }

  public Collection<? extends ConfigurationValue> getValues() {
    return this.values;
  }

  public Map<? extends String, ? extends String> getCoordinates() {
    return this.applicationCoordinates;
  }

  public String getName() {
    return this.name;
  }

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
