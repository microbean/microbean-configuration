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

import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class SystemPropertiesConfiguration extends AbstractConfiguration {

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
  
  public SystemPropertiesConfiguration() {
    super();
  }

  @Override
  public ConfigurationValue getValue(final Map<String, String> coordinates, final String name) {
    ConfigurationValue returnValue = null;
    if (name != null) {
      final String propertyValue = System.getProperty(name);
      if (propertyValue != null) {
        returnValue = new ConfigurationValue(this, null, name, propertyValue, this.isAuthoritative(name));
      }
    }
    return returnValue;
  }

  protected boolean isAuthoritative(final String name) {
    return name != null && systemPropertiesGuaranteedToExist.contains(name);
  }
  
}
