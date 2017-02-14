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
package org.microbean.configuration.spi.converter;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.microbean.configuration.ConversionException;

import org.microbean.configuration.spi.Converter;

public final class StringToPathConverter extends Converter<Path> {

  private static final long serialVersionUID = 1L;

  @Override
  public final Path convert(final String value) {
    final Path returnValue;
    if (value == null) {
      returnValue = null;
    } else {
      Path path = null;
      try {
        path = Paths.get(value);
      } catch (final InvalidPathException badPath) {
        throw new ConversionException(badPath);
      } finally {
        returnValue = path;
      }
    }
    return returnValue;
  }
  
}
