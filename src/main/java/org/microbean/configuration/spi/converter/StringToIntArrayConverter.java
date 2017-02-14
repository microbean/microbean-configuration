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

import java.util.Arrays;

import org.microbean.configuration.spi.Converter;

public final class StringToIntArrayConverter extends Converter<int[]> {

  private static final long serialVersionUID = 1L;

  private static final int[] EMPTY_INT_ARRAY = new int[0];

  private final StringToIntegerConverter scalarConverter;
  
  public StringToIntArrayConverter() {
    super();
    this.scalarConverter = new StringToIntegerConverter();
    assert int[].class.equals(this.getType());
  }

  @Override
  public final int[] convert(final String value) {
    int[] returnValue = null;
    if (value != null) {
      if (value.isEmpty()) {
        returnValue = EMPTY_INT_ARRAY;
      } else {
        returnValue = Arrays.stream(value.split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .mapToInt(this.scalarConverter::convert)
          .toArray();
      }
    }
    return returnValue;
  }
  
}
