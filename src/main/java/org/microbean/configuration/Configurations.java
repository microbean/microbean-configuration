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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.Set;
import java.util.ServiceLoader;

import org.microbean.configuration.spi.Configuration;
import org.microbean.configuration.spi.ConfigurationValue;

/**
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public class Configurations {

  private final Collection<Configuration> configurations;
  
  public Configurations() {
    super();
    final Collection<? extends Configuration> configurations = this.loadConfigurations();
    if (configurations == null || configurations.isEmpty()) {
      this.configurations = Collections.emptySet();
    } else {
      this.configurations = Collections.unmodifiableCollection(new LinkedList<>(configurations));
    }
  }

  public Configurations(final Collection<? extends Configuration> configurations) {
    super();
    if (configurations == null || configurations.isEmpty()) {
      this.configurations = Collections.emptySet();
    } else {
      this.configurations = Collections.unmodifiableCollection(new LinkedList<>(configurations));
    }
  }

  protected Collection<? extends Configuration> loadConfigurations() {
    final Collection<Configuration> returnValue = new LinkedList<>();
    final ServiceLoader<Configuration> configurationLoader = ServiceLoader.load(Configuration.class);
    assert configurationLoader != null;
    final Iterator<Configuration> configurationIterator = configurationLoader.iterator();
    assert configurationIterator != null;
    while (configurationIterator.hasNext()) {
      final Configuration configuration = configurationIterator.next();
      assert configuration != null;
      returnValue.add(configuration);
    }
    return returnValue;
  }
  
  public final String getValue(Map<String, String> callerCoordinates, final String name) {
    if (callerCoordinates == null) {
      callerCoordinates = Collections.emptyMap();
    }
    String returnValue = null;

    // A ConfigurationValue representing a matched value.  null means
    // we haven't matched anything.
    ConfigurationValue configurationValue = null;    

    // All non-exact matches sorted with most-specific match at position 0.
    final Set<ConfigurationValue> values = new TreeSet<>(Comparator.comparingInt(v -> -v.size()));

    for (final Configuration configuration : this.configurations) {
      assert configuration != null;

      final ConfigurationValue value = configuration.getValue(callerCoordinates, name);
      if (value != null) {

        // Make sure the name is right.
        if (name == null) {
          if (value.getName() != null) {
            throw new IllegalStateException("TODO: Configuration returned a ConfigurationValue with a name different from " + name + ": " + value.getName());
          }
        } else if (!name.equals(value.getName())) {
          throw new IllegalStateException("TODO: Configuration returned a ConfigurationValue with a name different from " + name + ": " + value.getName());
        }

        final Map<String, String> valueCoordinates = value.getCoordinates();

        if (callerCoordinates.equals(valueCoordinates)) {
          if (configurationValue != null) {
            throw new RuntimeException("TODO: Configuration returned two maximally specific matches. Old one: " + configurationValue + "; new one: " + value);
          } else {
            configurationValue = value;
          }
        } else if (configurationValue == null &&
                   valueCoordinates != null &&
                   valueCoordinates.size() < callerCoordinates.size() &&
                   callerCoordinates.entrySet().containsAll(valueCoordinates.entrySet())) {
          // We specified, e.g., {a=b, c=d, e=f} and they have, say,
          // {c=d, e=f} or {a=b, c=d} etc. but not, say, {q=r}.
          values.add(value);
          
        }
      }
    }
    
    if (configurationValue == null) {      
      int size = -1;
      for (final ConfigurationValue value : values) {
        assert value != null;

        // "Biggest" (most-specific) will be on top
        int valueSize = value.size();
        assert size == -1 || valueSize <= size;
        if (valueSize == size) {
          // Conflict, right? It's a Set, not a List.
          throw new RuntimeException("TODO: conflict");
        } else if (size == -1 || valueSize < size) {
          if (configurationValue == null) {
            configurationValue = value;
            size = valueSize;
          } else {
            // We have a candidate in there already with a bigger size
            // (greater specificity).  Break.
            break;
          }
        } else {
          assert false : "valueSize > size: " + valueSize + " > " + size;
        }
      }
    }
    
    if (configurationValue != null) {
      returnValue = configurationValue.getValue();
    }
    return returnValue;
  }
  
}
