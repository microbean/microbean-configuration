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
 * A {@link PropertiesConfiguration} that {@linkplain
 * #getValue(Resource, Map, String) gets configuration property
 * values} from {@link Properties} resources.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #getValue(Resource, Map, String)
 */
public class ApplicationPropertiesConfiguration extends PropertiesConfiguration {


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
   * Creates a new {@link ApplicationPropertiesConfiguration}.
   */
  public ApplicationPropertiesConfiguration() {
    super(new CachingResourceLoader<>(new PropertiesLoader("application.properties")));
  }
  
}
