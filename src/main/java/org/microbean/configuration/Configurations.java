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
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.Set;

import org.microbean.configuration.spi.Arbiter;
import org.microbean.configuration.spi.Configuration;
import org.microbean.configuration.spi.ConfigurationValue;

/**
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public class Configurations {

  private final Collection<Configuration> configurations;

  private final Collection<Arbiter> arbiters;
  
  public Configurations() {
    this(null, null);
  }

  public Configurations(final Collection<? extends Configuration> configurations) {
    this(configurations, null);
  }
  
  public Configurations(Collection<? extends Configuration> configurations,
                        Collection<? extends Arbiter> arbiters) {
    super();
    if (configurations == null) {
      configurations = this.loadConfigurations();
    }
    if (configurations == null || configurations.isEmpty()) {
      this.configurations = Collections.emptySet();
    } else {
      this.configurations = Collections.unmodifiableCollection(new LinkedList<>(configurations));
    }
    if (arbiters == null) {
      arbiters = this.loadArbiters();
    }
    if (arbiters == null || arbiters.isEmpty()) {
      this.arbiters = Collections.emptySet();
    } else {
      this.arbiters = Collections.unmodifiableCollection(new LinkedList<>(arbiters));
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

  protected Collection<? extends Arbiter> loadArbiters() {
    final Collection<Arbiter> returnValue = new LinkedList<>();
    final ServiceLoader<Arbiter> arbiterLoader = ServiceLoader.load(Arbiter.class);
    assert arbiterLoader != null;
    final Iterator<Arbiter> arbiterIterator = arbiterLoader.iterator();
    assert arbiterIterator != null;
    while (arbiterIterator.hasNext()) {
      final Arbiter arbiter = arbiterIterator.next();
      assert arbiter != null;
      returnValue.add(arbiter);
    }
    return returnValue;
  }

  public String getValue(final String name) {
    return this.getValue(Collections.emptyMap(), name);
  }
  
  public String getValue(Map<String, String> callerCoordinates, final String name) {
    if (callerCoordinates == null) {
      callerCoordinates = Collections.emptyMap();
    }
    String returnValue = null;

    ConfigurationValue selectedValue = null;    

    // We use a PriorityQueue of ConfigurationValues sorted by their
    // specificity to keep track of the most specific
    // ConfigurationValue found so far.  We create it only when necessary.
    final Comparator<ConfigurationValue> comparator = Comparator.<ConfigurationValue>comparingInt(v -> v.specificity()).reversed();
    PriorityQueue<ConfigurationValue> values = null;

    Collection<ConfigurationValue> badValues = null;
    
    for (final Configuration configuration : this.configurations) {
      assert configuration != null;
      
      final ConfigurationValue value = configuration.getValue(callerCoordinates, name);
      if (value != null) {

        if (name == null) {
          if (value.getName() != null) {
            badValues.add(value);
          }
          continue;
        } else if (!name.equals(value.getName())) {
          badValues.add(value);
          continue;
        }

        Map<String, String> valueCoordinates = value.getCoordinates();
        if (valueCoordinates == null) {
          valueCoordinates = Collections.emptyMap();
        }

        final int callerCoordinatesSize = callerCoordinates.size();
        final int valueCoordinatesSize = valueCoordinates.size();

        if (callerCoordinatesSize < valueCoordinatesSize) {
          // Bad value!
          if (badValues == null) {
            badValues = new LinkedList<>();
          }
          badValues.add(value);
          
        } else if (callerCoordinates.equals(valueCoordinates)) {
          // We have an exact match.  We hope it's going to be the
          // only one.
          
          if (selectedValue == null) {
            
            if (values == null || values.isEmpty()) {
              // There aren't any conflicts yet; this is good.  This
              // value will be our candidate.
              selectedValue = value;
              
            } else {
              // We got a match, but we already *had* a match, so we
              // don't have a candidate--instead, add it to the bucket
              // of values that will be arbitrated later.
              values.add(value);
              
            }
            
          } else {
            assert selectedValue != null;
            // We have an exact match, but we already identified a
            // candidate, so oops, we have to treat our prior match
            // and this one as non-candidates.
            
            if (values == null) {
              values = new PriorityQueue<>(comparator);
            }
            values.add(selectedValue);
            selectedValue = null;
            values.add(value);
          }

        } else if (callerCoordinatesSize == valueCoordinatesSize) {
          // Bad value!  The configuration subsystem handed back a
          // value containing coordinates not drawn from the
          // callerCoordinatesSet.  We know this because we already
          // tested for Set equality, which failed, so this test means
          // disparate entries.
          if (badValues == null) {
            badValues = new LinkedList<>();
          }
          badValues.add(value);
          
        } else if (selectedValue != null) {
          assert callerCoordinatesSize > valueCoordinatesSize;
          // Nothing to do; we've already got our candidate.  We don't
          // break here because we're going to ensure there aren't any
          // duplicates.
          
        } else if (callerCoordinates.entrySet().containsAll(valueCoordinates.entrySet())) {
          assert callerCoordinatesSize > valueCoordinatesSize;
          // We specified, e.g., {a=b, c=d, e=f} and they have, say,
          // {c=d, e=f} or {a=b, c=d} etc. but not, say, {q=r}.
          if (values == null) {
            values = new PriorityQueue<>(comparator);
          }
          values.add(value);
          
        } else {
          assert callerCoordinatesSize > valueCoordinatesSize;
          assert selectedValue == null;
          // Bad value!
          if (badValues == null) {
            badValues = new LinkedList<>();
          }
          badValues.add(value);
          
        }
      }
    }

    if (badValues != null && !badValues.isEmpty()) {
      this.handleMalformedConfigurationValues(badValues);
    }
    
    if (selectedValue == null && values != null && !values.isEmpty()) {
      final Collection<ConfigurationValue> valuesToArbitrate = new LinkedList<>();
      int highestSpecificitySoFarEncountered = -1;
      while (!values.isEmpty()) {

        final ConfigurationValue value = values.poll();
        assert value != null;

        // The values are sorted by their specificity, most specific
        // first.
        final int valueSpecificity = Math.max(0, value.specificity());
        assert highestSpecificitySoFarEncountered < 0 || valueSpecificity <= highestSpecificitySoFarEncountered;

        if (highestSpecificitySoFarEncountered < 0 || valueSpecificity < highestSpecificitySoFarEncountered) {
          if (selectedValue == null) {
            assert valuesToArbitrate.isEmpty();
            selectedValue = value;
            highestSpecificitySoFarEncountered = valueSpecificity;
          } else if (valuesToArbitrate.isEmpty()) {
            break;
          } else {
            valuesToArbitrate.add(value);
          }
        } else if (valueSpecificity == highestSpecificitySoFarEncountered) {
          assert selectedValue != null;
          valuesToArbitrate.add(selectedValue);
          selectedValue = null;
          valuesToArbitrate.add(value);
        } else {
          assert false : "valueSpecificity > highestSpecificitySoFarEncountered: " + valueSpecificity + " > " + highestSpecificitySoFarEncountered;
        }
        
      }
      
      if (!valuesToArbitrate.isEmpty()) {
        selectedValue = this.arbitrate(callerCoordinates, name, valuesToArbitrate);
      }
    }
    
    if (selectedValue != null) {
      returnValue = selectedValue.getValue();
    }
    return returnValue;
  }

  protected void handleMalformedConfigurationValues(final Collection<ConfigurationValue> badValues) {
    if (badValues != null) {
      badValues.clear();
    }
  }
  
  protected ConfigurationValue arbitrate(final Map<? extends String, ? extends String> callerCoordinates,
                                         final String name,
                                         final Collection<ConfigurationValue> values) {
    if (this.arbiters != null && !this.arbiters.isEmpty()) {
      for (final Arbiter arbiter : arbiters) {
        if (arbiter != null) {
          final ConfigurationValue arbitrationResult = arbiter.arbitrate(callerCoordinates, name, values);
          if (arbitrationResult != null) {
            return arbitrationResult;
          }
        }
      }
    }
    throw new AmbiguousConfigurationValuesException(null, null, callerCoordinates, name, values);
  }

}
