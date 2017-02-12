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

import java.io.Serializable; // for javadoc only

/**
 * A {@link TypeLiteral} that can convert {@link String} values into
 * another kind of {@link Object}.
 *
 * @param <T> the type of {@link Object} to which {@link String}
 * values may be converted
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #convert(String)
 */
public abstract class Converter<T> extends TypeLiteral<T> {


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
   * Creates a new {@link Converter}.
   */
  protected Converter() {
    super(Converter.class);
  }


  /*
   * Instance methods.
   */
  

  /**
   * Converts the supplied {@code value} into an {@link Object} of the
   * appropriate type.
   *
   * <p>Implementations of this method are permitted to return {@code
   * null}.</p>
   *
   * @param value the value to convert; may be {@code null}
   *
   * @return the converted value, which may be {@code null}
   */
  public abstract T convert(final String value);

}
