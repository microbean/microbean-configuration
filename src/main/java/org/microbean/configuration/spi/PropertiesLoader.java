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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import java.util.Map;
import java.util.Properties;

import java.util.concurrent.ConcurrentHashMap;

import java.util.function.Function;

import org.microbean.configuration.Configurations;

import org.microbean.configuration.api.ConfigurationException;

import org.microbean.configuration.spi.AbstractResourceLoadingConfiguration.Resource;

import org.microbean.configuration.spi.converter.StringToMapStringStringConverter;

public class PropertiesLoader implements Function<Map<? extends String, ? extends String>, Resource<? extends Properties>> {

  private final ClassLoader resourceLoader;
  
  protected final String name;

  public PropertiesLoader(final String name) {
    this(Thread.currentThread().getContextClassLoader(), name);
  }
  
  public PropertiesLoader(final ClassLoader resourceLoader, final String name) {
    super();
    this.name = name;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public Resource<? extends Properties> apply(final Map<? extends String, ? extends String> requestedConfigurationCoordinates) {
    Resource<? extends Properties> returnValue = null;
    if (this.name != null) {
      ClassLoader resourceLoader = this.resourceLoader;
      if (resourceLoader == null) {
        resourceLoader = Thread.currentThread().getContextClassLoader();
        if (resourceLoader == null) {
          resourceLoader = this.getClass().getClassLoader();
        }
      }
      assert resourceLoader != null;
      final URL resource = resourceLoader.getResource(this.computeResourceName(requestedConfigurationCoordinates));
      if (resource != null) {
        Properties properties = null;
        try (final InputStream inputStream = new BufferedInputStream(resource.openStream())) {
          if (inputStream != null) {
            properties = new Properties();
            properties.load(inputStream);
          }
        } catch (final IOException ioException) {
          throw new ConfigurationException(ioException.getMessage(), ioException);
        }
        returnValue = new Resource<>(properties, new StringToMapStringStringConverter().convert(properties.getProperty(Configurations.CONFIGURATION_COORDINATES)));
      }
    }
    return returnValue;
  }

  protected String computeResourceName(final Map<? extends String, ? extends String> requestedConfigurationCoordinates) {
    return this.name;
  }

}
