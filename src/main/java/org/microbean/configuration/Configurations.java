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

import java.lang.reflect.Type;

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

import java.util.function.Function;

import java.util.stream.Collectors;

import org.microbean.configuration.spi.Arbiter;
import org.microbean.configuration.spi.Configuration;
import org.microbean.configuration.spi.ConfigurationValue;
import org.microbean.configuration.spi.Converter;
import org.microbean.configuration.spi.TypeLiteral;

/**
 * A single source for configuration values in an application.
 *
 * <h2>Design Notes</h2>
 *
 * <p>The {@code public} methods in this class are not {@code final}
 * only so that {@link Configurations} objects may be used in
 * environments requiring proxies (like CDI, HK2 and the like).  In
 * general, overriding of non-{@code protected} methods is
 * discouraged, and in such cases the burden is fully upon the
 * overrider to make sure that contracts described in this class
 * documentation are honored.</p>
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Configuration
 */
public class Configurations {


  /*
   * Instance fields.
   */

  
  private final Collection<Configuration> configurations;

  private final Collection<Arbiter> arbiters;

  private final Map<Type, Converter<?>> converters;

  private final Map<String, String> configurationCoordinates;


  /*
   * Constructors.
   */

  
  public Configurations() {
    this(null, null, null);
  }

  public Configurations(final Collection<? extends Configuration> configurations) {
    this(configurations, null, null);
  }
  
  public Configurations(Collection<? extends Configuration> configurations,
                        Collection<? extends Converter<?>> converters,
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

    if (converters == null) {
      converters = this.loadConverters();
    }
    if (converters == null || converters.isEmpty()) {
      converters = Collections.emptySet();
    } else {
      converters = Collections.unmodifiableCollection(new LinkedList<>(converters));
    }
    assert converters != null;
    this.converters = Collections.unmodifiableMap(converters.stream().collect(Collectors.toMap(c -> c.getType(), Function.identity())));

    if (arbiters == null) {
      arbiters = this.loadArbiters();
    }
    if (arbiters == null || arbiters.isEmpty()) {
      this.arbiters = Collections.emptySet();
    } else {
      this.arbiters = Collections.unmodifiableCollection(new LinkedList<>(arbiters));
    }

    final Map<String, String> coordinates = this.getValue(null, "configurationCoordinates", new TypeLiteral<Map<String, String>>() {
        private static final long serialVersionUID = 1L; }.getType());
    if (coordinates == null || coordinates.isEmpty()) {
      this.configurationCoordinates = Collections.emptyMap();
    } else {
      this.configurationCoordinates = Collections.unmodifiableMap(coordinates);
    }
  }


  /*
   * Instance methods.
   */
  

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

  protected Collection<? extends Converter<?>> loadConverters() {
    final Collection<Converter<?>> returnValue = new LinkedList<>();
    @SuppressWarnings("rawtypes")
    final ServiceLoader<Converter> converterLoader = ServiceLoader.load(Converter.class);
    assert converterLoader != null;
    @SuppressWarnings("rawtypes")
    final Iterator<Converter> converterIterator = converterLoader.iterator();
    assert converterIterator != null;
    while (converterIterator.hasNext()) {
      final Converter<?> converter = converterIterator.next();
      assert converter != null;
      returnValue.add(converter);
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

  public Map<String, String> getConfigurationCoordinates() {
    return this.configurationCoordinates;
  }
  
  public Set<Type> getConversionTypes() {
    return this.converters.keySet();
  }
  
  public String getValue(final String name) {
    return this.getValue(this.getConfigurationCoordinates(), name, String.class);
  }
  
  public String getValue(Map<String, String> callerCoordinates, final String name) {
    return this.getValue(callerCoordinates, name, String.class);
  }

  public <T> T getValue(final String name, final Class<T> type) {
    return this.getValue(this.getConfigurationCoordinates(), name, type);
  }
  
  public <T> T getValue(Map<String, String> callerCoordinates, final String name, final Class<T> type) {
    return this.getValue(callerCoordinates, name, (Type)type);
  }

  public <T> T getValue(final String name, final TypeLiteral<T> typeLiteral) {
    return this.getValue(this.getConfigurationCoordinates(), name, typeLiteral);
  }
  
  public <T> T getValue(Map<String, String> callerCoordinates, final String name, final TypeLiteral<T> typeLiteral) {
    return this.getValue(callerCoordinates, name, typeLiteral == null ? (Type)null : typeLiteral.getType());
  }

  public <T> T getValue(final String name, final Type type) {
    return this.getValue(this.getConfigurationCoordinates(), name, type);
  }
  
  public <T> T getValue(Map<String, String> callerCoordinates, final String name, final Type type) {
    @SuppressWarnings("unchecked")
    final Converter<T> converter = (Converter<T>)this.converters.get(type);
    if (converter == null) {
      throw new NoSuchConverterException(null, null, type);
    }
    return this.getValue(callerCoordinates, name, converter);
  }

  public <T> T getValue(final String name, final Converter<T> converter) {
    return this.getValue(this.getConfigurationCoordinates(), name, converter);
  }
  
  public <T> T getValue(Map<String, String> callerCoordinates, final String name, final Converter<T> converter) {
    Objects.requireNonNull(converter);
    if (callerCoordinates == null) {
      callerCoordinates = Collections.emptyMap();
    }
    T returnValue = null;

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
          // Nothing to do; we've already got our candidate.  We don't
          // break here because we're going to ensure there aren't any
          // duplicates.
          
        } else if (callerCoordinates.entrySet().containsAll(valueCoordinates.entrySet())) {
          // We specified, e.g., {a=b, c=d, e=f} and they have, say,
          // {c=d, e=f} or {a=b, c=d} etc. but not, say, {q=r}.
          if (values == null) {
            values = new PriorityQueue<>(comparator);
          }
          values.add(value);
          
        } else {
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
          if (value.isAuthoritative()) {
            if (selectedValue.isAuthoritative()) {
              // Both say they're authoritative; arbitration required
              valuesToArbitrate.add(selectedValue);
              selectedValue = null;
              valuesToArbitrate.add(value);
            } else {
              // value is authoritative; selectedValue is not; so swap
              // them
              selectedValue = value;
            }
          } else if (selectedValue.isAuthoritative()) {
            // value is not authoritative; selected value is; so just
            // drop value on the floor; it's not authoritative.
          } else {
            // Neither is authoritative; arbitration required.
            valuesToArbitrate.add(selectedValue);
            selectedValue = null;
            valuesToArbitrate.add(value);
          }
        } else {
          assert false : "valueSpecificity > highestSpecificitySoFarEncountered: " + valueSpecificity + " > " + highestSpecificitySoFarEncountered;
        }
        
      }
      
      if (!valuesToArbitrate.isEmpty()) {
        selectedValue = this.performArbitration(callerCoordinates, name, Collections.unmodifiableCollection(valuesToArbitrate));
      }
    }
    
    if (selectedValue != null) {
      returnValue = converter.convert(selectedValue.getValue());
    }
    return returnValue;
  }

  /**
   * Handles any badly formed {@link ConfigurationValue} instances
   * received from {@link Configuration} instances during the
   * execution of a configuration value request.
   *
   * <p>The default implementation of this method does nothing.
   * Malformed values are thus effectively discarded.</p>
   *
   * @param badValues a {@link Collection} of {@link
   * ConfigurationValue} instances that were deemed to be malformed in
   * some way; may be {@code null}
   */
  protected void handleMalformedConfigurationValues(final Collection<ConfigurationValue> badValues) {
    
  }

  /**
   * Given a logical request for a configuration value, represented by
   * the {@code callerCoordinates} and {@code name} parameter values,
   * and a {@link Collection} of {@link ConfigurationValue} instances
   * that represents the ambiguous response from several {@link
   * Configuration} instances, attempts to resolve the ambiguity by
   * returning a single {@link ConfigurationValue} instead.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * <p>The default implementation of this method asks all registered
   * {@link Arbiter}s in turn to perform the arbitration and returns
   * the first non-{@code null} response received.</p>
   *
   * @param callerCoordinates the ({@linkplain
   * Collections#unmodifiableMap(Map) immutable}) configuration
   * coordinates in effect for the request; may be {@code null}
   *
   * @param name the name of the configuration value; may be {@code
   * null}
   *
   * @param values an {@linkplain
   * Collections#unmodifiableCollection(Collection) immutable} {@link
   * Collection} of definitionally ambiguous {@link
   * ConfigurationValue}s that resulted from the request; may be
   * {@code null}
   *
   * @return the result of arbitration, or {@code null}
   *
   * @exception AmbiguousConfigurationValuesException if successful
   * arbitration did not happen for any reason
   *
   * @see Arbiter
   */
  protected ConfigurationValue performArbitration(final Map<? extends String, ? extends String> callerCoordinates,
                                                  final String name,
                                                  final Collection<? extends ConfigurationValue> values) {
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
