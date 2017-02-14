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
import java.util.Collections;
import java.util.Map;

import java.util.stream.Collectors;

import org.microbean.configuration.api.ConversionException;

import org.microbean.configuration.spi.Converter;

public final class StringToMapStringStringConverter extends Converter<Map<String, String>> {

  private static final long serialVersionUID = 1L;
  
  @Override
  public final Map<String, String> convert(String value) {
    Map<String, String> returnValue = null;
    if (value != null) {
      value = value.trim();
      if (value.isEmpty()) {
        returnValue = Collections.emptyMap();
      } else if (value.startsWith("{") && value.endsWith("}")) {
        value = value.substring(1, value.length() - 1).trim(); // chops off the { and the }
      }
      if (value.isEmpty()) {
        returnValue = Collections.emptyMap();
      } else {
        returnValue = Arrays.stream(value.split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .map(s -> s.split("="))
          .collect(Collectors.toMap(a -> a[0].trim(), a -> a[1].trim()));
      }
    }
    return returnValue;
  }
  
}
