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

import java.io.Serializable;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import java.util.function.Function;

import org.microbean.configuration.api.ConfigurationValue;

/**
 * An {@link AbstractResourceLoadingConfiguration} that {@linkplain
 * #getValue(Resource, Map, String) gets configuration property
 * values} from {@link Properties} resources.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #getValue(Resource, Map, String)
 */
public class PropertiesConfiguration extends AbstractResourceLoadingConfiguration<Properties> implements Ranked, Serializable {


  /*
   * Static fields.
   */


  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link PropertiesConfiguration}.
   *
   * @param resourceLoader a {@link Function} that accepts a {@link
   * Map} of requested configuration coordinates and returns a {@link
   * Resource} that can {@linkplain Resource#get() supply} a {@link
   * Properties} object to serve as a source of configuration values
   * for use by the {@link #getValue(Resource, Map, String)} method;
   * may be {@code null} in which case all invocations of the {@link
   * #getValue(Map, String)} method will return {@code null}
   *
   * @see #getValue(Resource, Map, String)
   *
   * @see Resource
   */
  public PropertiesConfiguration(final Function<? super Map<? extends String, ? extends String>, ? extends Resource<? extends Properties>> resourceLoader) {
    super(resourceLoader);
  }


  /*
   * Instance methods.
   */


  /**
   * {@inheritDoc}
   *
   * <p>This implementation gets a {@link Properties} object
   * {@linkplain Resource#get() from the supplied
   * <code>Resource</code>} and uses it, plus the {@linkplain
   * Resource#getCoordinates() configuration coordinates supplied by
   * the supplied <code>Resource</code>}, to construct and return a
   * suitable {@link ConfigurationValue}.</p>
   *
   * @param propertiesResource a {@link Resource} that can {@linkplain
   * Resource#get() supply} a {@link Properties} object to serve as
   * the ultimate source of configuration values for the requested
   * coordinates; must not be {@code null}
   *
   * @param requestedCoordinates for convenience, the same {@link Map}
   * supplied to the {@link #getValue(Map, String)} method is supplied
   * here representing the configuration coordinates for which a value
   * is requested; note that these may very well be different from
   * {@linkplain Resource#getCoordinates() the configuration
   * coordinates actually pertaining to the resource}; most
   * implementations will not need to reference this parameter
   *
   * @param name the name of the configuration property for which a
   * value is to be sought; must not be {@code null}
   *
   * @return a suitable {@link ConfigurationValue} or {@code null}
   *
   * @exception NullPointerException if {@code resource} or {@code
   * name} is {@code null}
   *
   * @see #getValue(Map, String)
   *
   * @see Resource
   */
  @Override
  protected ConfigurationValue getValue(final Resource<? extends Properties> propertiesResource, final Map<String, String> requestedCoordinates, final String name) {
    final ConfigurationValue returnValue;
    final Properties properties;    
    if (propertiesResource == null) {
      properties = null;
      returnValue = null;
    } else {
      properties = propertiesResource.get();
      if (properties == null) {
        returnValue = null;
      } else {
        returnValue = new ConfigurationValue(this, propertiesResource.getCoordinates(), name, properties.getProperty(name), false);
      }
    }
    return returnValue;
  }

  @Override
  public Set<String> getNames(final Resource<? extends Properties> propertiesResource) {
    final Set<String> returnValue;
    if (propertiesResource == null) {
      returnValue = Collections.emptySet();
    } else {
      final Properties properties = propertiesResource.get();
      if (properties != null) {
        returnValue = Collections.emptySet();
      } else {
        returnValue = properties.stringPropertyNames();
      }
    }
    return returnValue;
  }

  @Override
  protected int getRank(final Resource<? extends Properties> resource) {
    final int returnValue;
    if (resource == null) {
      returnValue = super.getRank(resource);
    } else {
      final Properties properties = resource.get();
      if (properties == null) {
        returnValue = super.getRank(resource);
      } else {
        final String rankString = properties.getProperty("org.microbean.configuration.rank", "100");
        assert rankString != null;
        int temp = 100;
        try {
          temp = Integer.parseInt(rankString);
        } catch (final NumberFormatException ignoreMe) {

        } finally {
          returnValue = temp;
        }
      }
    }
    return returnValue;
  }
  
}
