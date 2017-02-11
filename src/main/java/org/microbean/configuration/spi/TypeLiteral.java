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

import java.io.Serializable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * A class that can represent generic types.
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Type
 */
public abstract class TypeLiteral<T> implements Serializable {


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
   * Within any given class hierarchy rooted with this class, the
   * first class beneath the root whose immediate subclasses will be
   * inspected for type information.
   *
   * <p>This field will never be {@code null}.</p>
   *
   * @see #TypeLiteral(Class)
   */
  private final Class<?> classHierarchyParent;

  /**
   * The actual {@link Type} housed by this {@link TypeLiteral}.
   *
   * <p>This field will never be {@code null}.</p>
   */
  private transient Type type;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link TypeLiteral}.
   */
  protected TypeLiteral() {
    this(TypeLiteral.class);
  }

  /**
   * Creates a new {@link TypeLiteral} with a deeper class hierarchy.
   *
   * <p>This specialized constructor is for use only by {@code
   * abstract} subclasses of {@link TypeLiteral} that are extending
   * and looking to use the subsequently extended type inspection
   * capabilities of this class.</p>
   *
   * @param <T> a type representing a subclass of {@link TypeLiteral}
   *
   * @param classHierarchyParent the first class beneath this class in
   * the subclass hierarchy whose immediate subclasses will be
   * inspected for parameterized type information; must not be {@code
   * null}
   *
   * @exception NullPointerException if {@code classHierarchyParent}
   * is {@code null}
   *
   * @see Converter
   */
  <T extends TypeLiteral<?>> TypeLiteral(final Class<T> classHierarchyParent) {
    super();
    Objects.requireNonNull(classHierarchyParent);
    this.classHierarchyParent = classHierarchyParent;
  }

  /**
   * Returns the {@link Type} represented by this {@link TypeLiteral}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link Type} represented by this {@link TypeLiteral};
   * never {@code null}
   *
   * @exception IllegalStateException if somehow this {@link
   * TypeLiteral} was instantiated without a type parameter
   */
  public final Type getType() {
    if (this.type == null) {
      final Class<?> topmostTypeLiteralSubclass = getFirstSubclass(this.getClass(), this.classHierarchyParent);
      assert topmostTypeLiteralSubclass != null;
      this.type = getSoleTypeParameter(topmostTypeLiteralSubclass);
      if (this.type == null) {
        throw new IllegalStateException("No type parameter specified");
      }
    }
    return this.type;
  }

  /**
   * Returns a hashcode for this {@link TypeLiteral} based off its
   * {@link #getType() Type}'s hashcode.
   *
   * @return a hashcode for this {@link TypeLiteral}
   *
   * @see #equals(Object)
   */
  @Override
  public int hashCode() {
    final Object type = this.getType();
    return type == null ? 0 : type.hashCode();
  }

  /**
   * Returns {@code true} if the supplied {@link Object} is equal to
   * this {@link TypeLiteral}.
   *
   * <p>Two {@link TypeLiteral} instances are equal if their {@link
   * #getType()} methods' return values {@linkplain
   * Object#equals(Object) are equal}.</p>
   *
   * @param other the {@link Object} to test; may be {@code null}
   *
   * @return {@code true} if the supplied {@link Object} is equal to
   * this {@link TypeLiteral}; {@code false} otherwise
   *
   * @see #hashCode()
   */
  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof TypeLiteral) {
      final TypeLiteral<?> her = (TypeLiteral<?>)other;
      final Object type = this.getType();
      if (type == null) {
        if (her.getType() != null) {
          return false;
        }
      } else if (!type.equals(her.getType())) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link TypeLiteral}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link TypeLiteral}
   */
  @Override
  public String toString() {
    return String.valueOf(this.getType());
  }

  
  /*
   * Static methods.
   */
  

  private static final Class<?> getFirstSubclass(final Class<?> c, final Class<?> classHierarchyParentType) {
    final Class<?> returnValue;
    if (c == null || c.equals(Object.class)) {
      returnValue = null;
    } else {
      final Class<?> superclass = c.getSuperclass();
      assert superclass != null;
      if (superclass.equals(classHierarchyParentType)) {
        returnValue = c;
      } else if (superclass.equals(Object.class)) {
        returnValue = null;
      } else {
        returnValue = getFirstSubclass(superclass, classHierarchyParentType); // recursive call
      }
    }
    return returnValue;
  }

  private static final Type getSoleTypeParameter(final Class<?> c) {
    Type returnValue = null;
    if (c != null) {
      final Type genericSuperclass = c.getGenericSuperclass();
      if (genericSuperclass instanceof ParameterizedType) {
        final ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments != null && actualTypeArguments.length == 1) {
          returnValue = actualTypeArguments[0];
        }
      }
    }
    return returnValue;
  }

  private static final class X {}
  
}
