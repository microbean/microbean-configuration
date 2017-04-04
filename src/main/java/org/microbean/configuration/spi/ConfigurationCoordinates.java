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
 * An {@link AbstractConfiguration} conceptually housing only the
 * configuration property that returns a {@link Map}-convertible
 * {@link String} representing configuration coordinates for the
 * calling application.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Configurations#getConfigurationCoordinates()
 */
public class ConfigurationCoordinates extends AbstractConfiguration implements Serializable {  


  /*
   * Static fields.
   */


  /**
   * The version of this class for {@linkplain Serializable
   * serialization} purposes.
   */
  private static final long serialVersionUID = 1L;
  

  /*
   * Constructors.
   */

  
  /**
   * Creates a new {@link ConfigurationCoordinates}.
   */
  public ConfigurationCoordinates() {
    super();
  }


  /*
   * Instance methods.
   */

  
  /**
   * Attempts to return a value for the {@linkplain
   * System#getProperty(String, String) System property} named {@value
   * org.microbean.configuration.Configurations#CONFIGURATION_COORDINATES}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param ignoredConfigurationCoordinates a {@link Map} of
   * configuration coordinates in effect for the request; effectively
   * ignored since by definition this method will be returning a
   * {@link ConfigurationValue} {@linkplain
   * ConfigurationValue#getValue() containing} a {@link String} value
   * representing a {@link Map} of configuration coordinates for the
   * caller; may be {@code null}; not used by this implementation
   *
   * @param name the name of the configuration property for which a
   * value should be returned; may be {@code null}
   *
   * @return a {@link ConfigurationValue} representing the
   * configuration coordinates to assign to the caller, or {@code
   * null}
   */
  @Override
  public ConfigurationValue getValue(final Map<String, String> ignoredConfigurationCoordinates, final String name) {
    ConfigurationValue returnValue = null;
    if (Configurations.CONFIGURATION_COORDINATES.equals(name)) {
      final String configurationCoordinates = System.getProperty(Configurations.CONFIGURATION_COORDINATES);
      if (configurationCoordinates != null) {
        returnValue = new ConfigurationValue(this, null, name, configurationCoordinates, true);
      }
    }
    return returnValue;
  }
  
}
