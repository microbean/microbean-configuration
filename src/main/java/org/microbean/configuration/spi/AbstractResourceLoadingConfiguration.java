/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017–2019 microBean.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import java.util.function.Function;
import java.util.function.Supplier;

import org.microbean.configuration.api.ConfigurationValue;

/**
 * An {@link AbstractConfiguration} that flexibly loads some kind of
 * resource, or uses a previously loaded one, to satisfy demands for
 * configuration property values.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #AbstractResourceLoadingConfiguration(Function)
 *
 * @see #getValue(Resource, Map, String)
 */
public abstract class AbstractResourceLoadingConfiguration<T> extends AbstractConfiguration implements Ranked {


  /*
   * Instance fields.
   */


  private final Function<? super Map<? extends String, ? extends String>, ? extends Resource<? extends T>> resourceLoader;


  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link AbstractResourceLoadingConfiguration}.
   *
   * @param resourceLoader a {@link Function} that accepts a {@link
   * Map} of requested configuration coordinates and returns a {@link
   * Resource} that can {@linkplain Resource#get() supply} a source of
   * configuration values for use by the {@link #getValue(Resource,
   * Map, String)} method; may be {@code null} in which case all
   * invocations of the {@link #getValue(Map, String)} method will
   * return {@code null}
   *
   * @see #getValue(Resource, Map, String)
   *
   * @see #getValue(Map, String)
   *
   * @see Resource
   */
  protected AbstractResourceLoadingConfiguration(final Function<? super Map<? extends String, ? extends String>, ? extends Resource<? extends T>> resourceLoader) {
    super();
    this.resourceLoader = resourceLoader;
  }


  /*
   * Instance methods.
   */


  @Override
  public int getRank() {
    final int returnValue;
    if (this.resourceLoader == null) {
      returnValue = 100;
    } else {
      returnValue = this.getRank(this.resourceLoader.apply(null));
    }
    return returnValue;
  }

  protected int getRank(final Resource<? extends T> resource) {
    return 100;
  }
  
  /**
   * {@inheritDoc}
   *
   * <p>This implementation calls the {@link #getValue(Resource, Map,
   * String)} method with the return value resulting from the
   * invocation of the {@link Function} supplied {@linkplain
   * #AbstractResourceLoadingConfiguration(Function) at construction
   * time}.
   *
   * @param coordinates the requested configuration coordinates; may be {@code null}
   *
   * @param name the name of a configuration property for which a
   * value should be returned; must not be {@code null}
   *
   * @exception NullPointerException if {@code name} is {@code null}
   *
   * @see #getValue(Resource, Map, String)
   */
  @Override
  public ConfigurationValue getValue(final Map<String, String> coordinates, final String name) {
    final ConfigurationValue returnValue;
    if (this.resourceLoader == null) {
      returnValue = null;
    } else {
      returnValue = this.getValue(this.resourceLoader.apply(coordinates), coordinates, name);
    }
    return returnValue;
  }

  @Override
  public Set<String> getNames() {
    final Set<String> returnValue;
    if (this.resourceLoader == null) {
      returnValue = Collections.emptySet();
    } else {
      returnValue = this.getNames(this.resourceLoader.apply(null));
    }
    return returnValue;
  }

  /**
   * Returns a {@link ConfigurationValue} suitable for the supplied
   * {@code name} normally sourced in some fashion from the supplied
   * {@link Resource}, or {@code null} if no such {@link
   * ConfigurationValue} can be found.
   *
   * <p>Implementations of this method are permitted to return {@code
   * null}.</p>
   *
   * <p>Implementations of this method must not call the {@link
   * #getValue(Map, String)} method or undefined behavior will
   * result.</p>
   *
   * @param resource a {@link Resource} hopefully providing access to
   * the ultimate source of configuration values for the requested
   * coordinates; must not be {@code null}
   *
   * @param requestedCoordinates for convenience, the same {@link Map}
   * supplied to the {@link #getValue(Map, String)} method is supplied
   * here representing the configuration coordinates for which a value
   * is requested; note that these may very well be different from
   * {@linkplain Resource#getCoordinates() the configuration
   * coordinates actually pertaining to the resource}; most
   * implementations will not need to reference this parameter
   *
   * @param name the name of the configuration property for which a
   * value is to be sought; must not be {@code null}
   *
   * @return a suitable {@link ConfigurationValue} or {@code null}
   *
   * @exception NullPointerException if {@code resource} or {@code
   * name} is {@code null}
   *
   * @see #getValue(Map, String)
   *
   * @see Resource
   */
  protected abstract ConfigurationValue getValue(final Resource<? extends T> resource, final Map<String, String> requestedCoordinates, final String name);

  protected abstract Set<String> getNames(final Resource<? extends T> resource);

  /*
   * Inner and nested classes.
   */


  /**
   * A {@link Supplier} of a particular kind of resource from which
   * configuration property values may be retrieved.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   */
  public static final class Resource<T> implements Supplier<T> {


    /*
     * Instance fields.
     */

    
    private final T resource;

    private final Map<String, String> coordinates;


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link Resource}.
     *
     * @param resource the actual underlying source of configuration
     * property values; may be {@code null}
     *
     * @param coordinates the configuration coordinates this {@link
     * Resource} provides values for; may be {@code null}
     */
    public Resource(final T resource, final Map<String, String> coordinates) {
      super();
      this.resource = resource;
      if (coordinates == null) {
        this.coordinates = null;
      } else if (coordinates.isEmpty()) {
        this.coordinates = Collections.emptyMap();
      } else {
        this.coordinates = Collections.unmodifiableMap(new HashMap<>(coordinates));
      }
    }


    /*
     * Instance methods.
     */
    

    /**
     * Returns the actual underlying source of configuration property
     * values, typically for use by implementations of the {@link
     * AbstractResourceLoadingConfiguration#getValue(Resource, Map,
     * String)} method.
     *
     * <p>This method may return {@code null}.</p>
     *
     * @return the the actual underlying source of configuration
     * property values, or {@code null}
     */
    public final T get() {
      return this.resource;
    }

    /**
     * Returns an {@linkplain Collections#unmodifiableMap(Map)
     * immutable} {@link Map} representing the configuration
     * coordinates for which this {@link Resource} can assist in
     * providing values.
     *
     * <p>This method may return {@code null}.</p>
     *
     * @return the configuration coordinates, or {@code null}
     */
    public final Map<String, String> getCoordinates() {
      return this.coordinates;
    }

    /**
     * Returns a hashcode for this {@link Resource}.
     *
     * @return a hashcode for this {@link Resource}
     *
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
      int hashCode = 17;

      Object resource = this.get();
      int c = resource == null ? 0 : resource.hashCode();
      hashCode = 37 * hashCode + c;

      Object coordinates = this.getCoordinates();
      c = coordinates == null ? 0 : coordinates.hashCode();
      hashCode = 37 * hashCode + c;
      
      return hashCode;
    }

    /**
     * Returns {@code true} if this {@link Resource} is equal to the
     * supplied {@link Object}.
     *
     * @param other the {@link Object} to test; may be {@code null} in
     * which case {@code false} will be returned
     *
     * @return {@code true} if this {@link Resource} is equal to the
     * supplied {@link Object}; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      } else if (other instanceof Resource) {
        final Resource<?> her = (Resource<?>)other;

        final Object myResource = this.get();
        if (myResource == null) {
          if (her.get() != null) {
            return false;
          }
        } else if (!myResource.equals(her.get())) {
          return false;
        }

        final Object coordinates = this.getCoordinates();
        if (coordinates == null) {
          if (her.getCoordinates() != null) {
            return false;
          }
        } else if (!coordinates.equals(her.getCoordinates())) {
          return false;
        }

        return true;
      } else {
        return false;
      }
    }
    
  }
  
}
