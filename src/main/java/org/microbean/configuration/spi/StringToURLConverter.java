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

import java.net.MalformedURLException;
import java.net.URL;

import org.microbean.configuration.ConversionException;

public class StringToURLConverter extends Converter<URL> {

  private static final long serialVersionUID = 1L;
  
  public StringToURLConverter() {
    super();
    assert URL.class.equals(this.getType());
  }

  @Override
  public URL convert(final String value) {
    URL returnValue = null;
    if (value != null) {
      try {
        returnValue = new URL(value);
      } catch (final MalformedURLException kaboom) {
        throw new ConversionException(kaboom);
      }
    }
    return returnValue;
  }
  
}
