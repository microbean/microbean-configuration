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
package org.microbean.configuration;

import java.beans.FeatureDescriptor;

import java.lang.reflect.Type;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

import java.util.function.Function;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.stream.Collectors;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.StandardELContext;
import javax.el.ValueExpression;

import org.microbean.configuration.api.AmbiguousConfigurationValuesException;
import org.microbean.configuration.api.ConfigurationValue;
import org.microbean.configuration.api.ConversionException;
import org.microbean.configuration.api.TypeLiteral;

import org.microbean.configuration.spi.Arbiter;
import org.microbean.configuration.spi.Configuration;
import org.microbean.configuration.spi.Converter;

/**
 * A single source for configuration values suitable for an
 * application.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Configuration
 */
public class Configurations extends org.microbean.configuration.api.Configurations {


  /*
   * Static fields.
   */


  /**
   * An {@linkplain Collections#unmodifiableMap(Map) immutable} {@link
   * Map} of "wrapper" {@link Class} instances indexed by their
   * {@linkplain Class#isPrimitive() primitive} counterparts.
   *
   * <p>This field is never {@code null}.</p>
   */
  private static final Map<Class<?>, Class<?>> wrapperTypes = Collections.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
      private static final long serialVersionUID = 1L;
      {
        put(boolean.class, Boolean.class);
        put(byte.class, Byte.class);
        put(char.class, Character.class);
        put(double.class, Double.class);
        put(float.class, Float.class);
        put(int.class, Integer.class);
        put(long.class, Long.class);
        put(short.class, Short.class);
        put(void.class, Void.class);
      }
    });
  
  /**
   * The name of the configuration property whose value is a {@link
   * Map} of <em>configuration coordinates</em> for the current
   * application.
   *
   * <p>This field is never {@code null}.</p>
   *
   * <p>A request is made via the {@link #getValue(Map, String, Type)}
   * method with {@code null} as the value of its first parameter and
   * the value of this field as its second parameter and {@link Map
   * Map.class} as the value of its third parameter.  The returned
   * {@link Map} is cached for the lifetime of this {@link
   * Configurations} object and is returned by the {@link
   * #getConfigurationCoordinates()} method.</p>
   *
   * @see #getValue(Map, String, Type)
   *
   * @see #getConfigurationCoordinates()
   */
  public static final String CONFIGURATION_COORDINATES = "configurationCoordinates";

  /**
   * A deliberately non-{@code static} {@link ThreadLocal} containing
   * a {@link Set} of {@link Configuration}s that are currently in the
   * process of executing their {@link Configuration#getValue(Map,
   * String)} methods.
   *
   * <p>This field is never {@code null}.</p>
   *
   * @see #getValue(Map, String, Converter, String)
   *
   * @see #activate(Configuration)
   *
   * @see #deactivate(Configuration)
   *
   * @see #isActive(Configuration)
   */
  private final ThreadLocal<Set<Configuration>> currentlyActiveConfigurations;


  /*
   * Instance fields.
   */

  
  /**
   * Whether this {@link Configurations} has been initialized.
   *
   * @see #Configurations(Collection, Collection, Collection)
   */
  private final boolean initialized;

  /**
   * The {@link Collection} of {@link Configuration} instances that
   * can {@linkplain Configuration#getValue(Map, String) provide}
   * configuration values.
   *
   * <p>This field is never {@code null}.</p>
   *
   * @see #Configurations(Collection, Collection, Collection)
   */
  private final Collection<Configuration> configurations;

  /**
   * The {@link Collection} of {@link Arbiter}s that can resolve
   * otherwise ambiguous configuration values.
   *
   * <p>This field is never {@code null}.</p>
   *
   * @see #Configurations(Collection, Collection, Collection)
   */
  private final Collection<Arbiter> arbiters;

  /**
   * A {@link Map} of {@link Converter} instances, indexed under
   * {@linkplain Converter#getType() their <code>Type</code>}.
   *
   * <p>This field is never {@code null}.</p>
   *
   * @see #Configurations(Collection, Collection, Collection)
   */
  private final Map<Type, Converter<?>> converters;

  /**
   * A {@link Map} representing the <em>configuration coordinates</em>
   * of the application using this {@link Configurations}.
   *
   * <p>This field may be {@code null}.</p>
   *
   * @see #getConfigurationCoordinates()
   */
  private final Map<String, String> configurationCoordinates;

  /**
   * An {@link ELContext} used for Expression Language evaluation.
   *
   * <p>This field is never {@code null}.</p>
   */
  private final ELContext elContext;

  /**
   * An {@link ExpressionFactory} used for Expression Language
   * evaluation.
   *
   * <p>This field is never {@code null}.</p>
   */
  private final ExpressionFactory expressionFactory;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link Configurations}.
   *
   * <p>The {@link #loadConfigurations()}, {@link #loadConverters()}
   * and {@link #loadArbiters()} methods will be invoked during
   * construction.</p>
   *
   * @see #loadConfigurations()
   *
   * @see #loadConverters()
   *
   * @see #loadArbiters()
   *
   * @see #Configurations(Collection, Collection, Collection)
   */
  public Configurations() {
    this(null, null, null);
  }

  /**
   * Creates a new {@link Configurations}.
   *
   * <p>The {@link #loadConverters()} and {@link #loadArbiters()}
   * methods will be invoked during construction.  IF the supplied
   * {@code configurations} is {@code null}, then the {@link
   * #loadConfigurations()} method will be invoked during
   * construction.</p>
   *
   * @param configurations a {@link Collection} of {@link
   * Configuration} instances; if {@code null} then the return value
   * of the {@link #loadConfigurations()} method will be used instead
   *
   * @see #loadConfigurations()
   *
   * @see #loadConverters()
   *
   * @see #loadArbiters()
   *
   * @see #Configurations(Collection, Collection, Collection)
   */
  public Configurations(final Collection<? extends Configuration> configurations) {
    this(configurations, null, null);
  }

  /**
   * Creates a new {@link Configurations}.
   *
   * <p>The {@link #loadConverters()} and {@link #loadArbiters()}
   * methods will be invoked during construction.  IF the supplied
   * {@code configurations} is {@code null}, then the {@link
   * #loadConfigurations()} method will be invoked during
   * construction.</p>
   *
   * @param configurations a {@link Collection} of {@link
   * Configuration} instances; if {@code null} then the return value
   * of the {@link #loadConfigurations()} method will be used instead
   *
   * @param converters a {@link Collection} of {@link Converter}
   * instances; if {@code null} then the return value of the {@link
   * #loadConverters()} method will be used instead
   *
   * @param arbiters a {@link Collection} of {@link Arbiter}
   * instances; if {@code null} then the return value of the {@link
   * #loadArbiters()} method will be used instead
   *
   * @see #loadConfigurations()
   *
   * @see #loadConverters()
   *
   * @see #loadArbiters()
   */
  public Configurations(Collection<? extends Configuration> configurations,
                        Collection<? extends Converter<?>> converters,
                        Collection<? extends Arbiter> arbiters) {
    super();
    this.currentlyActiveConfigurations = ThreadLocal.withInitial(() -> new HashSet<>());

    this.expressionFactory = ExpressionFactory.newInstance();
    assert this.expressionFactory != null;
    final StandardELContext standardElContext = new StandardELContext(this.expressionFactory);
    standardElContext.addELResolver(new ConfigurationELResolver());
    this.elContext = standardElContext;
    
    if (configurations == null) {
      configurations = this.loadConfigurations();
    }
    if (configurations == null || configurations.isEmpty()) {
      this.configurations = Collections.emptySet();
    } else {
      this.configurations = Collections.unmodifiableCollection(new LinkedList<>(configurations));
    }
    for (final Configuration configuration : configurations) {
      if (configuration != null) {
        configuration.setConfigurations(this);
      }
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

    this.initialized = true;
    
    final Map<String, String> coordinates = this.getValue(null, CONFIGURATION_COORDINATES, new TypeLiteral<Map<String, String>>() {
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
  

  /**
   * Loads a {@link Collection} of {@link Configuration} objects and
   * returns it.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * <p>The default implementation of this method uses the {@link
   * ServiceLoader} mechanism to load {@link Configuration}
   * instances.</p>
   *
   * @return a non-{@code null}, {@link Collection} of {@link
   * Configuration} instances
   *
   * @see ServiceLoader#load(Class)
   */
  protected Collection<? extends Configuration> loadConfigurations() {
    final String cn = this.getClass().getName();
    final String mn = "loadConfigurations";
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.entering(cn, mn);
    }
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
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }

  /**
   * Loads a {@link Collection} of {@link Converter} objects and
   * returns it.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * <p>The default implementation of this method uses the {@link
   * ServiceLoader} mechanism to load {@link Converter} instances.</p>
   *
   * @return a non-{@code null}, {@link Collection} of {@link
   * Converter} instances
   *
   * @see ServiceLoader#load(Class)
   */
  protected Collection<? extends Converter<?>> loadConverters() {
    final String cn = this.getClass().getName();
    final String mn = "loadConverters";
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.entering(cn, mn);
    }
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
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }

  /**
   * Loads a {@link Collection} of {@link Arbiter} objects and returns
   * it.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * <p>The default implementation of this method uses the {@link
   * ServiceLoader} mechanism to load {@link Arbiter} instances.</p>
   *
   * @return a non-{@code null}, {@link Collection} of {@link Arbiter}
   * instances
   *
   * @see ServiceLoader#load(Class)
   */
  protected Collection<? extends Arbiter> loadArbiters() {
    final String cn = this.getClass().getName();
    final String mn = "loadArbiters";
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.entering(cn, mn);
    }
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
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }

  /**
   * Returns a {@link Map} of <em>configuration
   * coordinates</em>&mdash;aspects and their values that define a
   * location within which requests for configuration values may take
   * place.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * <p>The default implementation of this method returns
   * configuration coordinates that are discovered at {@linkplain
   * #Configurations() construction time} and cached for the lifetime
   * of this {@link Configurations} object.</p>
   *
   * @return a {@link Map} of configuration coordinates; may be {@code
   * null}
   */
  @Override
  public Map<String, String> getConfigurationCoordinates() {
    return this.configurationCoordinates;
  }

  /**
   * Returns a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) immutable} {@link Set} of {@link
   * Type}s representing all the types to which {@link String}
   * configuration values may be converted by the {@linkplain
   * #loadConverters() <code>Converter</code>s loaded} by this {@link
   * Configurations} object.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) immutable} {@link Set} of {@link
   * Type}s
   */
  @Override
  public final Set<Type> getConversionTypes() {
    this.checkState();
    return this.converters.keySet();
  }

  /**
   * Returns a configuration value corresponding to the configuration
   * property suitable for the supplied {@code
   * configurationCoordinates} and {@code name}, or the supplied
   * {@code defaultValue} if {@code null} would otherwise be returned,
   * converted, if possible, to the type represented by the supplied
   * {@code type}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param <T> the type to which a {@link String}-typed configuration
   * value should be converted
   *
   * @param configurationCoordinates a {@link Map} representing the
   * configuration coordinates in effect for this request; may be
   * {@code null}
   *
   * @param name the name of the configuration property for which a
   * value will be returned; must not be {@code null}
   *
   * @param type a {@link Type} representing the type to
   * which the configuration value will be {@linkplain
   * Converter#convert(String) converted}; must not be {@code null}
   *
   * @param defaultValue the value that will be converted if {@code
   * null} would otherwise be returned; may be {@code null}
   *
   * @return the configuration value, or {@code null}
   *
   * @exception NullPointerException if {@code name} or {@code type}
   * is {@code null}
   *
   * @exception NoSuchConverterException if there is no {@link
   * Converter} available that {@linkplain Converter#getType()
   * handles} the {@link Type} represented by the supplied {@code
   * type}
   *
   * @exception ConversionException if type conversion could not occur
   * for any reason
   *
   * @exception AmbiguousConfigurationValuesException if two or more
   * values were found that could be suitable and arbitration
   * {@linkplain #performArbitration(Map, String, Collection) was
   * performed} but could not resolve the dispute
   *
   * @see #getValue(Map, String, Converter, String)
   */
  @Override
  public final <T> T getValue(final Map<String, String> configurationCoordinates, final String name, Type type, final String defaultValue) {
    final String cn = this.getClass().getName();
    final String mn = "getValue";
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.entering(cn, mn, new Object[] { configurationCoordinates, name, type, defaultValue });
    }
    if (type instanceof Class) {
      final Class<?> c = (Class<?>)type;
      if (c.isPrimitive()) {
        type = wrapperTypes.get(c);
      }
    }
    @SuppressWarnings("unchecked")
    final Converter<T> converter = (Converter<T>)this.converters.get(type);
    if (converter == null) {
      throw new NoSuchConverterException(type);
    }
    if (this.logger.isLoggable(Level.FINE)) {
      this.logger.logp(Level.FINE, cn, mn, "Using {0} to convert String to {1}", new Object[] { converter, type });
    }
    final T returnValue = this.getValue(configurationCoordinates, name, converter, defaultValue);
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }

  /**
   * Returns a configuration value corresponding to the configuration
   * property suitable for the supplied {@code name}, as {@linkplain
   * Converter#convert(String) converted} by the supplied {@link
   * Converter}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param <T> the type to which a {@link String}-typed configuration
   * value should be converted
   *
   * @param name the name of the configuration property for which a
   * value will be returned; must not be {@code null}
   *
   * @param converter a {@link Converter} instance that will convert
   * any {@link String} configuration value into the type of object
   * that this method will return; must not be {@code null}
   *
   * @return the configuration value, or {@code null}
   *
   * @exception NullPointerException if {@code name} or {@code
   * converter} is {@code null}
   *
   * @exception ConversionException if type conversion could not occur
   * for any reason
   *
   * @exception AmbiguousConfigurationValuesException if two or more
   * values were found that could be suitable and arbitration
   * {@linkplain #performArbitration(Map, String, Collection) was
   * performed} but could not resolve the dispute
   *
   * @see #getValue(Map, String, Converter, String)
   */
  public final <T> T getValue(final String name, final Converter<T> converter) {
    return this.getValue(this.getConfigurationCoordinates(), name, converter, null);
  }

  /**
   * Returns a configuration value corresponding to the configuration
   * property suitable for the supplied {@code
   * configurationCoordinates} and {@code name}, as {@linkplain
   * Converter#convert(String) converted} by the supplied {@link
   * Converter}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param <T> the type to which a {@link String}-typed configuration
   * value should be converted
   *
   * @param configurationCoordinates a {@link Map} representing the
   * configuration coordinates in effect for this request; may be
   * {@code null}
   *
   * @param name the name of the configuration property for which a
   * value will be returned; must not be {@code null}
   *
   * @param converter a {@link Converter} instance that will convert
   * any {@link String} configuration value into the type of object
   * that this method will return; must not be {@code null}
   *
   * @return the configuration value, or {@code null}
   *
   * @exception NullPointerException if {@code name} or {@code
   * converter} is {@code null}
   *
   * @exception ConversionException if type conversion could not occur
   * for any reason
   *
   * @exception AmbiguousConfigurationValuesException if two or more
   * values were found that could be suitable and arbitration
   * {@linkplain #performArbitration(Map, String, Collection) was
   * performed} but could not resolve the dispute
   *
   * @see #getValue(Map, String, Converter, String)
   */
  public final <T> T getValue(final Map<String, String> configurationCoordinates, final String name, final Converter<T> converter) {
    return this.getValue(configurationCoordinates, name, converter, null);
  }

  /**
   * Returns an object that is the value for the configuration request
   * represented by the supplied {@code configurationCoordinates},
   * {@code name} and {@code defaultValue} parameters, as converted by
   * the supplied {@link Converter}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param <T> the type of the object to be returned
   *
   * @param configurationCoordinates the configuration coordinates for which
   * a value should be selected; may be {@code null}
   *
   * @param name the name of the configuration property within the
   * world defined by the supplied {@code configurationCoordinates}
   * whose value is to be selected; must not be {@code null}
   *
   * @param converter a {@link Converter} instance that will convert
   * any {@link String} configuration value into the type of object
   * that this method will return; must not be {@code null}
   *
   * @param defaultValue the fallback default value to use as an
   * absolute last resort; may be {@code null}; will also be converted
   * by the supplied {@link Converter}
   *
   * @return the value for the implied configuration property, or {@code null}
   *
   * @exception NullPointerException if either {@code name} or {@code
   * converter} is {@code null}
   *
   * @exception ConversionException if type conversion could not occur
   * for any reason
   *
   * @exception AmbiguousConfigurationValuesException if two or more
   * values were found that could be suitable and arbitration
   * {@linkplain #performArbitration(Map, String, Collection) was
   * performed} but could not resolve the dispute
   *
   * @see Converter#convert(String)
   *
   * @see Configuration#getValue(Map, String)
   *
   * @see #performArbitration(Map, String, Collection)
   *
   * @see #handleMalformedConfigurationValues(Collection)
   */
  public <T> T getValue(Map<String, String> configurationCoordinates, final String name, final Converter<T> converter, final String defaultValue) {
    final String cn = this.getClass().getName();
    final String mn = "getValue";
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.entering(cn, mn, new Object[] { configurationCoordinates, name, converter, defaultValue });
    }
    Objects.requireNonNull(name);
    Objects.requireNonNull(converter);
    this.checkState();
    if (configurationCoordinates == null) {
      configurationCoordinates = Collections.emptyMap();
    }
    T returnValue = null;

    ConfigurationValue selectedValue = null;    

    // We use a PriorityQueue of ConfigurationValues sorted by their
    // specificity (most specific first) to keep track of the most
    // specific ConfigurationValue found so far.  We create it only
    // when necessary.
    final Comparator<ConfigurationValue> comparator = Comparator.<ConfigurationValue>comparingInt(v -> v.specificity()).reversed();
    PriorityQueue<ConfigurationValue> values = null;

    Collection<ConfigurationValue> badValues = null;
    
    for (final Configuration configuration : this.configurations) {
      assert configuration != null;

      final ConfigurationValue value;
      try {
        if (isActive(configuration)) {
          value = null;
        } else {
          this.activate(configuration);
          value = configuration.getValue(configurationCoordinates, name);
        }
      } finally {
        this.deactivate(configuration);
      }

      if (value != null) {

        if (!name.equals(value.getName())) {
          badValues.add(value);
          continue;
        }

        Map<String, String> valueCoordinates = value.getCoordinates();
        if (valueCoordinates == null) {
          valueCoordinates = Collections.emptyMap();
        }

        final int configurationCoordinatesSize = configurationCoordinates.size();
        final int valueCoordinatesSize = valueCoordinates.size();

        if (configurationCoordinatesSize < valueCoordinatesSize) {
          // Bad value!
          if (badValues == null) {
            badValues = new LinkedList<>();
          }
          badValues.add(value);
          
        } else if (configurationCoordinates.equals(valueCoordinates)) {
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

        } else if (configurationCoordinatesSize == valueCoordinatesSize) {
          // Bad value!  The configuration subsystem handed back a
          // value containing coordinates not drawn from the
          // configurationCoordinatesSet.  We know this because we already
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
          
        } else if (configurationCoordinates.entrySet().containsAll(valueCoordinates.entrySet())) {
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
    assert this.allConfigurationsInactive();

    if (badValues != null && !badValues.isEmpty()) {
      this.handleMalformedConfigurationValues(badValues);
    }
    
    if (selectedValue == null) {
      final Collection<ConfigurationValue> valuesToArbitrate = new LinkedList<>();
      int highestSpecificitySoFarEncountered = -1;
      if (values != null) {
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
      }

      if (selectedValue == null) {
        selectedValue = this.performArbitration(configurationCoordinates, name, Collections.unmodifiableCollection(valuesToArbitrate));
      }
    }

    if (selectedValue == null) {
      if (defaultValue == null) {
        returnValue = converter.convert(null);
      } else {
        returnValue = converter.convert(this.interpolate(defaultValue));
      }
    } else {
      final String valueToConvert = selectedValue.getValue();
      if (valueToConvert == null) {
        returnValue = converter.convert(null);
      } else {
        returnValue = converter.convert(this.interpolate(valueToConvert));
      }
    }

    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }

  /**
   * Interpolates any expressions occurring within the supplied {@code
   * value} and returns the result of interpolation.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * <p>The default implementation of this method performs
   * interpolation by using a {@link ValueExpression} as {@linkplain
   * ExpressionFactory#createValueExpression(ELContext, String, Class)
   * produced by an <code>ExpressionFactory</code>}.</p>
   *
   * <p>A {@code configurations} object is made available to any
   * Expression Language expressions, which exposes this {@link
   * Configurations} object.  This means, among other things, that you
   * can retrieve configuration values using the following syntax:</p>
   * <pre>${configurations["java.home"]}</pre>
   *
   * @param value a configuration value {@link String}, before any
   * type conversion has taken place, with (possibly) expression
   * language expressions in it; may be {@code null} in which case
   * {@code null} is returned
   *
   * @return the result of interpolating the supplied {@code value},
   * or {@code null}
   *
   * @exception PropertyNotFoundException if the supplied {@code
   * value} contained a valid expression language expression that
   * identifies an unknown property
   *
   * @see <a
   * href="https://docs.oracle.com/javaee/7/tutorial/jsf-el.htm#GJDDD">the
   * section in the Java EE Tutorial on the Unified Expression
   * Language</a>
   */
  public String interpolate(final String value) {
    final String cn = this.getClass().getName();
    final String mn = "interpolate";
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.entering(cn, mn, value);
    }
    final String returnValue;
    if (value == null) {
      returnValue = null;
    } else {
      final ValueExpression valueExpression = this.expressionFactory.createValueExpression(this.elContext, value, String.class);
      assert valueExpression != null;
      returnValue = String.class.cast(valueExpression.getValue(this.elContext));
    }
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }

  /**
   * Returns a {@link Set} of names of {@link ConfigurationValue}s
   * that might be returned by this {@link Configurations} instance.
   *
   * <p>This method does not return {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * <p>Just because a name appears in the returned {@link Set} does
   * <em>not</em> mean that a {@link ConfigurationValue} <em>will</em>
   * be returned for it in a location in configuration space
   * identified by any arbitrary set of configuration coordinates.</p>
   *
   * @return a non-{@code null} {@link Set} of names of {@link
   * ConfigurationValue}s
   */
  public Set<String> getNames() {
    final Set<String> returnValue;
    if (this.configurations == null || this.configurations.isEmpty()) {
      returnValue = Collections.emptySet();
    } else {
      final Set<String> names = new TreeSet<>();
      for (final Configuration configuration : this.configurations) {
        if (configuration != null) {
          final Set<String> configurationNames = configuration.getNames();
          if (configurationNames != null && !configurationNames.isEmpty()) {
            names.addAll(configurationNames);
          }
        }
      }
      if (names.isEmpty()) {
        returnValue = Collections.emptySet();
      } else {
        returnValue = Collections.unmodifiableSet(names);
      }
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
   * the {@code configurationCoordinates} and {@code name} parameter
   * values, and a {@link Collection} of {@link ConfigurationValue}
   * instances that represents the ambiguous response from several
   * {@link Configuration} instances, attempts to resolve the
   * ambiguity by returning a single {@link ConfigurationValue}
   * instead.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * <p>The default implementation of this method asks all registered
   * {@link Arbiter}s in turn to perform the arbitration and returns
   * the first non-{@code null} response received.</p>
   *
   * @param configurationCoordinates the ({@linkplain
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
  protected ConfigurationValue performArbitration(final Map<? extends String, ? extends String> configurationCoordinates,
                                                  final String name,
                                                  final Collection<? extends ConfigurationValue> values) {
    final String cn = this.getClass().getName();
    final String mn = "performArbitration";
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.entering(cn, mn, new Object[] { configurationCoordinates, name, values });
    }

    ConfigurationValue returnValue = null;
    if (this.arbiters != null && !this.arbiters.isEmpty()) {
      for (final Arbiter arbiter : arbiters) {
        if (arbiter != null) {
          final ConfigurationValue arbitrationResult = arbiter.arbitrate(configurationCoordinates, name, values);
          if (arbitrationResult != null) {
            returnValue = arbitrationResult;
            break;
          }
        }
      }
    }
    if (returnValue == null && values != null && !values.isEmpty()) {
      throw new AmbiguousConfigurationValuesException(null, null, configurationCoordinates, name, values);
    }
    
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }

  /**
   * If this {@link Configurations} has not yet finished {@linkplain
   * #Configurations() constructing}, then this method will throw an
   * {@link IllegalStateException}.
   *
   * @exception IllegalStateException if this {@link Configurations}
   * has not yet finished {@linkplain #Configurations() constructing}
   *
   * @see #Configurations()
   */
  private final void checkState() {
    if (!this.initialized) {
      throw new IllegalStateException();
    }
  }

  /**
   * Returns {@code true} if the supplied {@link Configuration} is
   * currently in the middle of executing its {@link
   * Configuration#getValue(Map, String)} method on the current {@link
   * Thread}.
   *
   * @param configuration the {@link Configuration} to test; may be
   * {@code null} in which case {@code false} will be returned
   *
   * @return {@code true} if the supplied {@link Configuration} is
   * active; {@code false} otherwise
   *
   * @see #activate(Configuration)
   *
   * @see #deactivate(Configuration)
   */
  private final boolean isActive(final Configuration configuration) {
    return configuration != null && this.currentlyActiveConfigurations.get().contains(configuration);
  }

  /**
   * Records that the supplied {@link Configuration} is in the middle
   * of executing its {@link Configuration#getValue(Map, String)}
   * method on the current {@link Thread}.
   *
   * <p>This method is idempotent.</p>
   *
   * @param configuration the {@link Configuration} in question; may
   * be {@code null} in which case no action is taken
   *
   * @see #isActive(Configuration)
   *
   * @see #deactivate(Configuration)
   */
  private final void activate(final Configuration configuration) {
    if (configuration != null) {
      this.currentlyActiveConfigurations.get().add(configuration);
      assert this.isActive(configuration);
    }
  }

  /**
   * Records that the supplied {@link Configuration} is no longer in
   * the middle of executing its {@link Configuration#getValue(Map,
   * String)} method on the current {@link Thread}.
   *
   * <p>This method is idempotent.</p>
   *
   * @param configuration the {@link Configuration} in question; may
   * be {@code null} in which case no action is taken
   *
   * @see #isActive(Configuration)
   *
   * @see #activate(Configuration)
   */
  private final void deactivate(final Configuration configuration) {
    if (configuration != null) {
      this.currentlyActiveConfigurations.get().remove(configuration);
    }
    assert !this.isActive(configuration);
  }

  /**
   * Returns {@code true} if all {@link Configuration} instances have
   * been {@linkplain #deactivate(Configuration) deactivated}.
   *
   * @return {@code true} if all {@link Configuration} instances have
   * been {@linkplain #deactivate(Configuration) deactivated}; {@code
   * false} otherwise
   *
   * @see #isActive(Configuration)
   *
   * @see #deactivate(Configuration)
   */
  private final boolean allConfigurationsInactive() {
    return this.currentlyActiveConfigurations.get().isEmpty();
  }


  /*
   * Inner and nested classes.
   */


  /**
   * An {@link ELResolver} that resolves a {@code configurations}
   * top-level object in the Expression Language and resolves its
   * properties by treating them as the names of configuration
   * properties.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see ELResolver
   *
   * @see StandardELContext#addELResolver(ELResolver)
   */
  private final class ConfigurationELResolver extends ELResolver {


    /*
     * Constructors.
     */

    /**
     * Creates a new {@link ConfigurationELResolver}.
     */
    private ConfigurationELResolver() {
      super();
    }

    /**
     * Returns {@link Class Object.class} when invoked.
     *
     * @param elContext the {@link ELContext} in effect; ignored
     *
     * @param base the base {@link Object} in effect; ignored
     *
     * @return {@link Class Object.class} when invoked
     *
     * @see ELResolver#getCommonPropertyType(ELContext, Object)
     */
    @Override
    public final Class<?> getCommonPropertyType(final ELContext elContext, final Object base) {
      return Object.class;
    }

    /**
     * Returns {@code null} when invoked.
     *
     * @param elContext the {@link ELContext} in effect; ignored
     *
     * @param base the base {@link Object} in effect; ignored
     *
     * @return {@code null} when invoked
     *
     * @see ELResolver#getFeatureDescriptors(ELContext, Object)
     */
    @Override
    public final Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext elContext, final Object base) {
      return null;
    }

    /**
     * Calls {@link ELContext#setPropertyResolved(boolean)
     * elContext.setPropertyResolved(true)} and returns {@code true}
     * when invoked.
     *
     * @param elContext the {@link ELContext} in effect; ignored
     *
     * @param base the base {@link Object} in effect; ignored
     *
     * @param property the property in effect; ignored
     *
     * @return {@code true} when invoked
     *
     * @see ELContext#setPropertyResolved(boolean)
     *
     * @see ELResolver#isReadOnly(ELContext, Object, Object)
     */
    @Override
    public final boolean isReadOnly(final ELContext elContext, final Object base, final Object property) {
      if (elContext != null && (property instanceof String || property instanceof Configurations)) {
        elContext.setPropertyResolved(true);
      }
      return true;
    }

    /**
     * Returns a {@link Class} representing the type of object that
     * the supplied {@code base}/{@code property} pair represents.
     *
     * <p>This method may return {@code null}.</p>
     *
     * <p>If the supplied {@code base} is {@code null} and the
     * supplied {@code property} is a {@link String} equal to "{@code
     * configurations}", then this method returns {@link Class
     * Configurations.class}.</p>
     *
     * <p>If the supplied {@code base} is an instance of {@link
     * Configurations} and the supplied {@code property} is a {@link
     * String}, then the {@code property} is treated as the name of a
     * configuration property, and the {@link
     * Configurations#getValue(String)} method is invoked on the
     * {@code base} object with it.  If the return value of that
     * method invocation is {@code null}, then a {@link
     * PropertyNotFoundException} is thrown; otherwise {@link Class
     * String.class} is returned.</p>
     *
     * <p>This method returns {@code null} in all other cases.</p>
     *
     * @param elContext the {@link ELContext} in effect; must not be
     * {@code null}
     *
     * @param base the base; may be {@code null}
     *
     * @param property the property; may be {@code null}
     *
     * @return {@link Class String.class} or {@code null}
     *
     * @exception NullPointerException if {@code elContext} is {@code
     * null}
     *
     * @exception PropertyNotFoundException if {@code property} is a
     * {@link String} but does not identify a configuration property
     * that has a value
     *
     * @see ELResolver#getType(ELContext, Object, Object)
     *
     * @see Configurations#getValue(String)
     */
    @Override
    public final Class<?> getType(final ELContext elContext, final Object base, final Object property) {
      Objects.requireNonNull(elContext);
      Class<?> returnValue = null;
      if (base == null) {
        if ("configurations".equals(property)) {
          elContext.setPropertyResolved(true);
          returnValue = Configurations.class;
        }
      } else if (base instanceof Configurations) {
        if (property instanceof String) {
          final String value = ((Configurations)base).getValue((String)property);
          elContext.setPropertyResolved(true);
          if (value == null) {
            throw new PropertyNotFoundException((String)property);
          }
          returnValue = String.class;
        }
      }
      return returnValue;      
    }

     /**
     * Returns the proper value for the supplied {@code base}/{@code
     * property} pair.
     *
     * <p>This method may return {@code null}.</p>
     *
     * <p>If the supplied {@code base} is {@code null} and the
     * supplied {@code property} is a {@link String} equal to "{@code
     * configurations}", then this method returns the {@link
     * Configurations} object housing this {@link ELResolver}
     * implementation.</p>
     *
     * <p>If the supplied {@code base} is an instance of {@link
     * Configurations} and the supplied {@code property} is a {@link
     * String}, then the {@code property} is treated as the name of a
     * configuration property, and the {@link
     * Configurations#getValue(String)} method is invoked on the
     * {@code base} object with it.  If the return value of that
     * method invocation is {@code null}, then a {@link
     * PropertyNotFoundException} is thrown; otherwise the return
     * value of its {@link ConfigurationValue#getValue()} method is
     * returned.</p>
     *
     * <p>This method returns {@code null} in all other cases.</p>
     *
     * @param elContext the {@link ELContext} in effect; must not be
     * {@code null}
     *
     * @param base the base; may be {@code null}
     *
     * @param property the property; may be {@code null}
     *
     * @return a {@link Configurations} object, a {@link String} or
     * {@code null}
     *
     * @exception NullPointerException if {@code elContext} is {@code
     * null}
     *
     * @exception PropertyNotFoundException if {@code property} is a
     * {@link String} but does not identify a configuration property
     * that has a value
     *
     * @see ELResolver#getValue(ELContext, Object, Object)
     *
     * @see Configurations#getValue(String)
     */
    @Override
    public final Object getValue(final ELContext elContext, final Object base, final Object property) {
      Objects.requireNonNull(elContext);
      Object returnValue = null;
      if (base == null) {
        if ("configurations".equals(property)) {
          elContext.setPropertyResolved(true);
          returnValue = Configurations.this;
        }
      } else if (base instanceof Configurations) {
        if (property instanceof String) {
          final String configurationPropertyName = (String)property;
          returnValue = ((Configurations)base).getValue(configurationPropertyName);
          elContext.setPropertyResolved(true);
          if (returnValue == null) {
            throw new PropertyNotFoundException(configurationPropertyName);
          }          
        }
      }
      return returnValue;
    }

    /**
     * Effectively does nothing when invoked.
     *
     * @param elContext the {@link ELContext} in effect; ignored; may be {@code null}
     *
     * @param base the base; ignored; may be {@code null}
     *
     * @param property the property; ignored; may be {@code null}
     *
     * @param value the value; ignored; may be {@code null}
     *
     * @see ELResolver#setValue(ELContext, Object, Object, Object)
     *
     * @see #isReadOnly(ELContext, Object, Object)
     */
    @Override
    public final void setValue(final ELContext elContext, final Object base, final Object property, final Object value) {
      if (elContext != null) {
        elContext.setPropertyResolved(false);
      }
    }
    
  }
  
}
