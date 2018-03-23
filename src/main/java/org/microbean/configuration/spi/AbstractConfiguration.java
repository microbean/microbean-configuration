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

import java.util.Set;

import org.microbean.configuration.Configurations;

/**
 * A skeletal implementation of the {@link Configuration} interface.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public abstract class AbstractConfiguration implements Configuration {


  /*
   * Instance fields.
   */


  /**
   * The {@link Configurations} governing this {@link Configuration}.
   *
   * <p>This field may be {@code null}.</p>
   *
   * @see #getConfigurations()
   *
   * @see #setConfigurations(Configurations)
   */
  private Configurations configurations;


  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link AbstractConfiguration}.
   */
  protected AbstractConfiguration() {
    super();
  }


  /*
   * Instance methods.
   */
  

  /**
   * Installs the supplied {@link Configurations} on this {@link
   * AbstractConfiguration} implementation.
   *
   * @param configurations the {@link Configurations} to install; may
   * be {@code null}
   *
   * @see #getConfigurations()
   */
  @Override
  public void setConfigurations(final Configurations configurations) {
    this.configurations = configurations;
  }

  /**
   * Returns the {@link Configurations} installed on this {@link
   * AbstractConfiguration} implementation.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the {@link Configurations} installed on this {@link
   * AbstractConfiguration} implementation, or {@code null}
   *
   * @see #setConfigurations(Configurations)
   */
  public Configurations getConfigurations() {
    return this.configurations;
  }

}
