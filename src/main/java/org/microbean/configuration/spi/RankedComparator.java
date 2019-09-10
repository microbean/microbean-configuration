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

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A {@link Comparator} that ranks {@linkplain
 * #getComparisonObject(Object) other objects} for comparison
 * purposes.
 *
 * <p>A <em>rank</em> for the purposes of this class is a positive
 * integer.  One integer representing a rank <em>outranks</em> another
 * integer representing a rank if the first integer is greater than
 * the second.  Hence, <em>e.g.</em>, {@code 1} outranks {@code 0}.  A
 * negative integer cannot represent a rank and in the context of
 * ranking means, effectively, that any rank it might otherwise
 * represent is unknown.</p>
 *
 * <p>A {@link RankedComparator} can accept an optional {@link List}
 * at {@linkplain #RankedComparator(List) construction time} that can
 * be used for ranking purposes in case the type of objects the {@link
 * RankedComparator} compares are not instances of {@link Ranked}.  In
 * such a case, the {@linkplain List#indexOf(Object) index of} an
 * object in a {@linkplain Collections#reverse(List) reversed} copy of
 * the supplied {@link List} is used as its rank.</p>
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
public class RankedComparator<T> implements Comparator<T>, Serializable {


  /*
   * Static fields.
   */


  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;


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
   * @see #RankedComparator(List)
   */
  public RankedComparator() {
    this(null);
  }

  /**
   * Creates a new {@link RankedComparator}.
   *
   * @param items a {@link List} of {@link Object}s that will be used
   * for ranking purposes; may be {@code null}; the item at position
   * {@code 0} is deemed to outrank all others; copied by value
   *
   * @see #getComparisonObject(Object)
   */
  public RankedComparator(final List<?> items) {
    super();
    if (items == null || items.isEmpty()) {
      this.items = Collections.emptyList();
    } else {
      final List<?> copy = new ArrayList<>(items);
      Collections.reverse(copy);
      this.items = Collections.unmodifiableList(copy);
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
    return comparisonObject != null && (comparisonObject instanceof Ranked || this.items.contains(comparisonObject));
  }

  private final int getRank(final Object object) {
    final int rank;
    if (object == null) {
      rank = -1;
    } else if (object instanceof Ranked) {
      rank = ((Ranked)object).getRank();
    } else {
      rank = this.items.indexOf(object);
    }
    return rank;
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
      final int oneRank = this.getRank(this.getComparisonObject(one));
      final int twoRank = this.getRank(this.getComparisonObject(two));
      if (oneRank < 0) {
        if (twoRank < 0) {
          returnValue = 0; // a negative rank means "no idea"
        } else {
          returnValue = 1; // two "won"; it's ranked; one isn't
        }
      } else if (oneRank > twoRank) {
        returnValue = -1; // one "won"; its rank is higher than two's
      } else if (oneRank == twoRank) {
        returnValue = 0;
      } else {
        returnValue = 1; // two "won"; its rank is higher than one's
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
