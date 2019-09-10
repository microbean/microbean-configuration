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

import java.util.Comparator;
import java.util.Objects;

/**
 * An {@code abstract} {@link Arbiter} that uses a {@link Comparator}
 * in some way.
 *
 * @param <T> the type of object that will be compared
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public abstract class ComparatorBasedArbiter<T> implements Arbiter {


  /*
   * Instance fields.
   */


  /**
   * A {@link Comparator} used by this {@link ComparatorBasedArbiter}
   * in some way.
   *
   * @see #getComparator()
   */
  private final Comparator<T> comparator;

  
  /*
   * Constructors.
   */


  /**
   * Creates a new {@link ComparatorBasedArbiter}.  Should never be
   * used.
   *
   * @see #ComparatorBasedArbiter(Comparator)
   *
   * @deprecated Please use the {@link
   * #ComparatorBasedArbiter(Comparator)} constructor instead.
   */
  @Deprecated
  private ComparatorBasedArbiter() {
    super();
    this.comparator = null;
  }

  /**
   * Creates a new {@link ComparatorBasedArbiter}.
   *
   * @param comparator the {@link Comparator} to use; must not be
   * {@code null}
   *
   * @exception NullPointerException if {@code comparator} is {@code
   * null}
   *
   * @see #getComparator()
   */
  protected ComparatorBasedArbiter(final Comparator<T> comparator) {
    super();
    this.comparator = Objects.requireNonNull(comparator);
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the {@link Comparator} used by this {@link
   * ComparatorBasedArbiter} implementation.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must never return {@code null}.</p>
   *
   * @return a non-{@code null} {@link Comparator}
   *
   * @see #ComparatorBasedArbiter(Comparator)
   */
  protected Comparator<T> getComparator() {
    return this.comparator;
  }
  
}
