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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A {@link Comparator} that uses a {@link List} of {@link Object}s to
 * rank {@linkplain #getComparisonObject(Object) other objects} for
 * comparison purposes.
 *
 * @param <T> the type of object to {@linkplain #compare(Object,
 * Object) compare}
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #RankedComparator(List)
 *
 * @see #compare(Object, Object)
 */
public class RankedComparator<T> implements Comparator<T> {


  /*
   * Instance fields.
   */


  /**
   * An {@linkplain Collections#unmodifiableList(List) unmodifiable}
   * {@link List} of {@link Object}s to be used for ranking purposes;
   * the {@link Object} at position {@code 0} is deemed to have the
   * highest rank.
   *
   * <p>This field is never {@code null}.</p>
   *
   * @see #RankedComparator(List)
   */
  private final List<?> items;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link RankedComparator}.
   *
   * @param items a {@link List} of {@link Object}s that will be used
   * for ranking purposes; the item at position {@code 0} is deemed to
   * outrank all others; copied by value
   *
   * @see #getComparisonObject(Object)
   */
  public RankedComparator(final List<?> items) {
    super();
    if (items == null || items.isEmpty()) {
      this.items = Collections.emptyList();
    } else {
      this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }
  }


  /*
   * Instance methods.
   */


  /**
   * Returns {@code true} if this {@link RankedComparator} explicitly
   * ranks the supplied object.
   *
   * @param object the object to test; may be {@code null}
   *
   * @return {@code true} if this {@link RankedComparator} explicitly
   * ranks the supplied object; {@code false} otherwise
   *
   * @see #getComparisonObject(Object)
   */
  public final boolean ranks(final T object) {
    final Object comparisonObject = this.getComparisonObject(object);
    return comparisonObject != null && this.items.contains(comparisonObject);
  }

  /**
   * Compares two objects, returning {@code -1} if {@code one}
   * outranks {@code two}, {@code 1} if {@code two} outranks {@code
   * one}, and {@code 0} if the two objects are either ranked equally
   * or their ranks could not be determined.
   *
   * <p>This method makes use of the {@link List} {@linkplain
   * #RankedComparator(List) supplied at construction time} for
   * ranking information.</p>
   *
   * <p>Non-{@code null} objects outrank {@code null} objects.</p>
   *
   * @param one the first object; may be {@code null}
   *
   * @param two the second object; may be {@code null}
   *
   * @return {@code -1} if {@code one} outranks {@code two}, {@code 1}
   * if {@code two} outranks {@code one}, and {@code 0} if the two
   * objects are either ranked equally or their ranks could not be
   * determined
   *
   * @see #getComparisonObject(Object)
   *
   * @see #RankedComparator(List)
   */
  @Override
  public final int compare(final T one, final T two) {
    final int returnValue;
    if (one == two) {
      returnValue = 0;
    } else if (one == null) {
      assert two != null;
      returnValue = 1; // nulls sort "right"; non-nulls win
    } else if (two == null) {
      assert one != null;
      returnValue = -1; // nulls sort "right"; non-nulls win
    } else {
      final Object oneComparisonObject = this.getComparisonObject(one);
      final int oneIndex = oneComparisonObject == null ? -1 : this.items.indexOf(oneComparisonObject);
      final Object twoComparisonObject = this.getComparisonObject(two);
      final int twoIndex = twoComparisonObject == null ? -1 : this.items.indexOf(twoComparisonObject);
      if (oneIndex < 0) {
        if (twoIndex < 0) {
          returnValue = 0;
        } else {
          returnValue = 1; // two "won"; it's ranked; one isn't
        }
      } else if (twoIndex < 0) {
        returnValue = -1; // one "won"; it's ranked; two isn't
      } else if (oneIndex > twoIndex) {
        returnValue = 1; // two "won"; its rank is closer to 0
      } else if (oneIndex == twoIndex) {
        returnValue = 0;
      } else {
        returnValue = -1; // one "won"; its rank is closer to 0
      }
    }
    return returnValue;
  }

  /**
   * Given an object to be {@linkplain #compare(Object, Object)
   * compared}, returns the {@link Object} that should be used to
   * determine ranking (as determined by the {@link List} {@linkplain
   * #RankedComparator(List) supplied at construction time}).
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * <p>The default implementation of this method simply returns the
   * supplied {@code realObject} parameter value.</p>
   *
   * @param realObject the object to be {@linkplain #compare(Object,
   * Object) compared}; may be {@code null}
   *
   * @return the {@link Object} that should be used to determine
   * ranking (as determined by the {@link List} {@linkplain
   * #RankedComparator(List) supplied at construction time}), possibly
   * {@code null}
   *
   * @see #RankedComparator(List)
   *
   * @see #compare(Object, Object)
   */
  protected Object getComparisonObject(final T realObject) {
    return realObject;
  }
  
}
