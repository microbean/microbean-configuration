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
package org.microbean.configuration;

import java.io.Serializable; // for javadoc only

import java.lang.reflect.Type;

import org.microbean.configuration.api.ConfigurationException;

import org.microbean.configuration.spi.Converter; // for javadoc only

/**
 * A {@link ConfigurationException} that indicates that a {@linkplain
 * Converter#getType() suitable} {@link Converter} could not be found
 * for a given {@link Type}.
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public class NoSuchConverterException extends ConfigurationException {


  /*
   * Static fields.
   */


  /**
   * The version of this class for {@linkplain Serializable
   * serialization} purposes.
   */
  private static final long serialVersionUID = 1L;


  /*
   * Instance fields.
   */


  /**
   * The {@link Type} for which a {@linkplain Converter#getType()
   * suitable} {@link Converter} could not be found.
   *
   * <p>This field may be {@code null}.</p>
   */
  private final Type type;


  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link NoSuchConverterException}.
   */
  public NoSuchConverterException() {
    super();
    this.type = null;
  }

  /**
   * Creates a new {@link NoSuchConverterException}.
   *
   * @param type the {@link Type} for which a {@linkplain
   * Converter#getType() suitable} {@link Converter} could not be
   * found; may be {@code null}
   *
   * @see #getType()
   */
  public NoSuchConverterException(final Type type) {
    super();
    this.type = type;
  }
  
  /**
   * Creates a new {@link NoSuchConverterException}.
   *
   * @param message the error message; may be {@code null}
   *
   * @param cause the {@link Throwable} that caused this {@link
   * NoSuchConverterException} to be created; may be {@code null}
   *
   * @param type the {@link Type} for which a {@linkplain
   * Converter#getType() suitable} {@link Converter} could not be
   * found; may be {@code null}
   *
   * @see #getType()
   */
  public NoSuchConverterException(final String message, final Throwable cause, final Type type) {
    super(message, cause);
    this.type = type;
  }


  /*
   * Instance methods.
   */
  

  /**
   * Returns the {@link Type} for which a {@linkplain Converter#getType()
   * suitable} {@link Converter} could not be found.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the {@link Type} for which a {@linkplain
   * Converter#getType() suitable} {@link Converter} could not be
   * found, or {@code null}
   */
  public final Type getType() {
    return this.type;
  }
  
}
