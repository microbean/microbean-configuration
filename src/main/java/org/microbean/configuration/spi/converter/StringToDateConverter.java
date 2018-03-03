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

import java.time.Instant;

import java.util.Date;

import org.microbean.configuration.spi.Converter;

public final class StringToDateConverter extends Converter<Date> {

  private static final long serialVersionUID = 1L;

  private static final StringToInstantConverter stringToInstantConverter = new StringToInstantConverter();

  public StringToDateConverter() {
    super();
  }
  
  @Override
  public final Date convert(final String value) {
    final Instant instant = stringToInstantConverter.convert(value);
    final Date returnValue;
    if (instant == null) {
      returnValue = null;
    } else {
      returnValue = Date.from(instant);
    }
    return returnValue;
  }
  
}