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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Properties;

public class AbstractPropertiesConfiguration extends AbstractConfiguration {

  private final Map<String, String> coordinates;

  private final Properties properties;
  
  public AbstractPropertiesConfiguration(final Map<String, String> coordinates,
                                         final Properties properties) {
    super();
    if (coordinates == null || coordinates.isEmpty()) {
      this.coordinates = Collections.emptyMap();
    } else {
      this.coordinates = Collections.unmodifiableMap(coordinates);
    }
    if (properties == null) {
      this.properties = new Properties();
    } else {
      this.properties = properties;
    }
  }

  protected final Map<String, String> getCoordinates() {
    return this.coordinates;
  }
  
  protected final Properties getProperties() {
    return this.properties;
  }
  
  @Override
  public ConfigurationValue getValue(final Map<String, String> coordinates, final String name) {
    final Properties properties = this.getProperties();
    assert properties != null;
    final Set<String> stringPropertyNames = properties.stringPropertyNames();
    final ConfigurationValue returnValue;
    if (stringPropertyNames != null && stringPropertyNames.contains(name)) {
      returnValue = new ConfigurationValue(this, this.getCoordinates(), name, properties.getProperty(name));
    } else {
      returnValue = null;
    }
    return returnValue;
  }
  
}
