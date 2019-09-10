/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017-2018 microBean.
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
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.microbean.configuration.api.ConfigurationValue;

/**
 * An {@link AbstractConfiguration} that houses {@linkplain
 * System#getProperties() System properties} and hence, by definition,
 * minimally {@linkplain ConfigurationValue#specificity() specific}
 * {@link ConfigurationValue}s representing them.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class SystemPropertiesConfiguration extends AbstractConfiguration implements Ranked, Serializable {


  /*
   * Static fields.
   */


  /**
   * The version of this class for {@linkplain Serializable
   * serialization} purposes.
   */
  private static final long serialVersionUID = 1L;

  /**
   * A {@link Set} of {@linkplain System#getProperties() System
   * properties} that the Java Language Specification guarantees will
   * exist on all platforms.
   *
   * <p>This field is never {@code null}.</p>
   *
   * @see System#getProperties()
   */
  private static final Set<String> systemPropertiesGuaranteedToExist = new HashSet<>(29, 1F);

  static {
    // See https://docs.oracle.com/javase/8/docs/api/java/lang/System.html#getProperties--
    systemPropertiesGuaranteedToExist.add("java.version");
    systemPropertiesGuaranteedToExist.add("java.vendor"); // Java Runtime Environment vendor
    systemPropertiesGuaranteedToExist.add("java.vendor.url"); // Java vendor URL
    systemPropertiesGuaranteedToExist.add("java.home"); // Java installation directory
    systemPropertiesGuaranteedToExist.add("java.vm.specification.version"); // Java Virtual Machine specification version
    systemPropertiesGuaranteedToExist.add("java.vm.specification.vendor"); // Java Virtual Machine specification vendor
    systemPropertiesGuaranteedToExist.add("java.vm.specification.name"); // Java Virtual Machine specification name
    systemPropertiesGuaranteedToExist.add("java.vm.version"); // Java Virtual Machine implementation version
    systemPropertiesGuaranteedToExist.add("java.vm.vendor"); // Java Virtual Machine implementation vendor
    systemPropertiesGuaranteedToExist.add("java.vm.name"); // Java Virtual Machine implementation name
    systemPropertiesGuaranteedToExist.add("java.specification.version"); // Java Runtime Environment specification version
    systemPropertiesGuaranteedToExist.add("java.specification.vendor"); // Java Runtime Environment specification vendor
    systemPropertiesGuaranteedToExist.add("java.specification.name"); // Java Runtime Environment specification name
    systemPropertiesGuaranteedToExist.add("java.class.version"); // Java class format version number
    systemPropertiesGuaranteedToExist.add("java.class.path"); // Java class path
    systemPropertiesGuaranteedToExist.add("java.library.path"); // List of paths to search when loading libraries
    systemPropertiesGuaranteedToExist.add("java.io.tmpdir"); // Default temp file path
    systemPropertiesGuaranteedToExist.add("java.compiler"); // Name of JIT compiler to use
    systemPropertiesGuaranteedToExist.add("java.ext.dirs"); // Path of extension directory or directories
    systemPropertiesGuaranteedToExist.add("os.name"); // Operating system name
    systemPropertiesGuaranteedToExist.add("os.arch"); // Operating system architecture
    systemPropertiesGuaranteedToExist.add("os.version"); // Operating system version
    systemPropertiesGuaranteedToExist.add("file.separator"); // File separator ("/" on UNIX)
    systemPropertiesGuaranteedToExist.add("path.separator"); // Path separator (":" on UNIX)
    systemPropertiesGuaranteedToExist.add("line.separator"); // Line separator ("\n" on UNIX)
    systemPropertiesGuaranteedToExist.add("user.name"); // User's account name
    systemPropertiesGuaranteedToExist.add("user.home"); // User's home directory
    systemPropertiesGuaranteedToExist.add("user.dir"); // User's current working directory
  }


  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link SystemPropertiesConfiguration}.
   */
  public SystemPropertiesConfiguration() {
    super();
  }


  /*
   * Instance methods.
   */

  
  @Override
  public final int getRank() {
    return 400;
  }

  /**
   * Returns a {@link ConfigurationValue} representing the {@linkplain
   * System#getProperty(String, String) System property} identified by
   * the supplied {@code name}, or {@code null}.
   *
   * <p>The {@link ConfigurationValue} returned will be marked as
   * {@linkplain ConfigurationValue#isAuthoritative() authoritative}
   * if it is one of the {@linkplain System#getProperties() System
   * properties guaranteed by the Java Language Specification to exist
   * on every available Java platform}.</p>
   *
   * @param coordinates the configuration coordinates in effect for
   * the current request; may be {@code null}
   *
   * @param name the name of the configuration property for which to
   * return a {@link ConfigurationValue}; may be {@code null}
   *
   * @return a {@link ConfigurationValue}, or {@code null}
   */
  @Override
  public final ConfigurationValue getValue(final Map<String, String> coordinates, final String name) {
    ConfigurationValue returnValue = null;
    if (name != null) {
      final String propertyValue = System.getProperty(name);
      if (propertyValue != null) {
        returnValue = new ConfigurationValue(this, null /* deliberately null coordinates */, name, propertyValue, this.isAuthoritative(name));
      }
    }
    return returnValue;
  }

  /**
   * Returns a {@link Set} of the names of all {@link
   * ConfigurationValue}s that might be returned by this {@link
   * Configuration}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * <p>This implementation returns an immutable representation of the
   * equivalent of {@link System#getProperties()
   * System.getProperties().stringPropertyNames()}.</p>
   *
   * @return a non-{@code null} {@link Set} of names
   */
  @Override
  public final Set<String> getNames() {
    final Properties properties = System.getProperties();
    assert properties != null; // by contract
    assert !properties.isEmpty(); // by contract
    final Set<String> returnValue = Collections.unmodifiableSet(properties.stringPropertyNames());
    return returnValue;
  }
  
  /**
   * Returns {@code true} if the supplied {@code name} is not {@code
   * null} and is one of the {@linkplain System#getProperties() System
   * properties guaranteed by the Java Language Specification to exist
   * on every available Java platform}.
   *
   * @param name the name of a {@linkplain System#getProperty(String,
   * String) System property}; may be {@code null} in which case
   * {@code false} will be returned
   *
   * @return {@code true} if any value that this {@link
   * SystemPropertiesConfiguration} produces for the supplied {@code
   * name} is to be regarded as the authoritative value; {@code false}
   * otherwise
   */
  protected boolean isAuthoritative(final String name) {
    return name != null && systemPropertiesGuaranteedToExist.contains(name);
  }

  @Override
  public final int hashCode() {
    return System.getProperties().hashCode();
  }

  @Override
  public final boolean equals(final Object other) {
    return other instanceof SystemPropertiesConfiguration;
  }

  @Override
  public String toString() {
    return System.getProperties().toString();
  }
  
}
