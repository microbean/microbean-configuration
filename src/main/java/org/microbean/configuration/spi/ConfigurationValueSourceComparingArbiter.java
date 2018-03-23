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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections; // for javadoc only
import java.util.List;
import java.util.Map;

import org.microbean.configuration.api.ConfigurationValue;

/**
 * A {@link ComparatorBasedArbiter} that compares {@link
 * ConfigurationValue}s by their {@linkplain
 * ConfigurationValue#getSource() sources}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ConfigurationValue
 *
 * @see ComparatorBasedArbiter
 */
public class ConfigurationValueSourceComparingArbiter extends ComparatorBasedArbiter<ConfigurationValue> {


  /*
   * Constructors.
   */

  
  /**
   * Creates a new {@link ConfigurationValueSourceComparingArbiter}.
   *
   * @param configurationTypes a {@link List} of {@link Class}
   * instances describing configuration value {@linkplain
   * ConfigurationValue#getSource() sources}; may be (trivially)
   * {@code null}
   *
   * @see RankedComparator#RankedComparator(List)
   */
  public ConfigurationValueSourceComparingArbiter(final List<? extends Class<? extends Configuration>> configurationTypes) {
    super(new RankedComparator<ConfigurationValue>(configurationTypes) {
        private static final long serialVersionUID = 1L;
        @Override
        protected final Object getComparisonObject(final ConfigurationValue realObject) {
          Object returnValue = null;
          if (realObject != null) {
            final Object source = realObject.getSource();
            if (source != null) {
              returnValue = source.getClass();
            }
          }
          return returnValue;
        }
      });
  }


  /*
   * Instance methods.
   */


  /**
   * Resolves the configuration value ambiguity represented by the
   * supplied {@code ambiguousValues} parameter value by selecting the
   * {@link ConfigurationValue} whose {@linkplain
   * ConfigurationValue#getSource() associated source} is {@linkplain
   * #ConfigurationValueSourceComparingArbiter(List) ranked} closer to
   * {@code 0}.
   *
   * @param requestedCoordinates the ({@linkplain
   * Collections#unmodifiableMap(Map) immutable}) configuration
   * coordinates in effect for the request; may be {@code null}
   *
   * @param name the name of the configuration value; must not be
   * {@code null}
   *
   * @param ambiguousValues an {@linkplain
   * Collections#unmodifiableCollection(Collection) immutable} {@link
   * Collection} of definitionally ambiguous {@link
   * ConfigurationValue}s that resulted from the request; may be
   * {@code null}
   *
   * @return the result of arbitration, or {@code null} if the dispute
   * cannot be arbitrated by this {@link Arbiter}
   *
   * @exception NullPointerException if {@code name} is {@code null}
   */
  @Override
  public ConfigurationValue arbitrate(final Map<? extends String, ? extends String> requestedCoordinates,
                                      final String name,
                                      final Collection<? extends ConfigurationValue> ambiguousValues) {
    final ConfigurationValue returnValue;
    if (ambiguousValues == null || ambiguousValues.isEmpty() || !this.canArbitrate(ambiguousValues)) {
      returnValue = null;
    } else {
      final int size = ambiguousValues.size();
      assert size > 0;
      switch (size) {
      case 1: // pathological case
        returnValue = ambiguousValues.iterator().next();
        break;
      default:
        final List<? extends ConfigurationValue> list = new ArrayList<>(ambiguousValues);
        list.sort(this.getComparator());
        returnValue = list.get(0);
        break;
      }
    }
    return returnValue;
  }

  /**
   * Returns the {@link RankedComparator} used by this {@link
   * ConfigurationValueSourceComparingArbiter} implementation.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must never return {@code null}.</p>
   *
   * @return a non-{@code null} {@link RankedComparator}
   */
  @Override
  protected RankedComparator<ConfigurationValue> getComparator() {
    return (RankedComparator<ConfigurationValue>)super.getComparator();
  }

  /**
   * Returns {@code true} if this {@link
   * ConfigurationValueSourceComparingArbiter} can effectively
   * arbitrate the conflict represented by the supplied {@link
   * Collection} of ambiguous {@link ConfigurationValue}s.
   *
   * @param ambiguousValues a {@link Collection} of {@link
   * ConfigurationValue}s; may be {@code null}; must not be modified
   * by overrides of this method
   *
   * @return {@code true} if this {@link
   * ConfigurationValueSourceComparingArbiter} can effectively
   * arbitrate the conflict represented by the supplied {@link
   * Collection} of ambiguous {@link ConfigurationValue}s; {@code
   * false} otherwise
   */
  protected boolean canArbitrate(final Collection<? extends ConfigurationValue> ambiguousValues) {
    final boolean returnValue;
    if (ambiguousValues == null || ambiguousValues.isEmpty()) {
      returnValue = false;
    } else {
      boolean canArbitrate = true;
      final RankedComparator<ConfigurationValue> comparator = this.getComparator();
      assert comparator != null;
      for (final ConfigurationValue value : ambiguousValues) {
        if (value == null || !comparator.ranks(value)) {
          canArbitrate = false;
          break;
        }
      }
      returnValue = canArbitrate;
    }
    return returnValue;
  }

}
