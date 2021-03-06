# microBean™ Configuration

[![Build Status](https://travis-ci.org/microbean/microbean-configuration.svg?branch=master)](https://travis-ci.org/microbean/microbean-configuration)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.microbean/microbean-configuration/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.microbean/microbean-configuration)

The microBean™ Configuration project provides yet another framework
for acquiring configuration values from within a Java SE application,
implementing the [microBean™ Configuration API project][10].

There are many configuration frameworks for Java SE.  All focus on
making it easy for application developers to ask for configuration
values without having to know what systems provide those values.

microBean™ Configuration takes a slightly different approach.  The
application's full configuration is considered to be located by its
_configuration coordinates_ within _configuration space_.  Within that
space reside its actual configuration properties.  This terminology is
important for understanding how microBean™ Configuration works.

Terminology
===========

Configuration Space
-------------------

_Configuration space_ is defined as the unbounded universe of
configuration properties and their values, for all applications,
regardless of which system or systems might define or house them.
Dimensions within this space might include locale, region, data center
identifier, tenant identifier&mdash;these are only examples.  Of note
is that these are dimensions, not nodes within a hierarchy.

Configuration Coordinates
-------------------------

_Configuration coordinates_ are those which semantically locate an
application in configuration space.  So an application run in the US
locale, in the test environment, located in the Western region of a
cloud provider, in the Nevada data center and run on behalf of tenant
number 34 (to use the examples from the Configuration Space section
above) is located in configuration space with, perhaps, the following
logical configuration coordinates:

    locale=en_US
    environment=test
    region=west
    dataCenter=Nevada
    tenantId=34
    
While these coordinates look like configuration values themselves,
they are configuration values for locating the application in
configuration space, not (normally) the values that the application
will be looking up during the course of its execution.

Configuration Properties
------------------------

_Configuration properties_ are the names for which (non-hard-coded)
values are sought by an application.  `portNumber`,
`timeoutInSeconds`, `databaseUrl` and `userEmail` are arbitrary
examles of configuration properties.  These are, by definition,
non-specific identifiers.  They gain specificity only when further
pinpointed in configuration space by a set of configuration
coordinates.  That is, `databaseUrl` is a configuration property that
might have many different possible values for many different possible
applications.  The configuration property `databaseUrl` used by a
particular application is only relevant when it is interpreted in the
context of the appropriate set of configuration coordinates.  For
example, a configuration author may only be able to write a value for
the `databaseUrl` configuration property when she knows that the value
is supposed to be for the test environment in the Nevada data center
on behalf of tenant 34.  Or she may also be able to write a value for
the `databaseUrl` configuration property when she knows that the value
is supposed to be for the test environment and the Western region.  As
you can see, configuration coordinates are not hierarchical, and
neither (necessarily) are configuration properties.

Configuration Values
--------------------

_Configuration values_ are two things: the _values received by an
application_ situated in configuration space by its configuration
coordinates when that application asks for a value for a configuration
property, and _values that are written_ in such a way that they may be
found by one or more such applications.

For example, a configuration author might write a value, `red`, for a
hypothetical `color` configuration property, into a system somewhere,
once.  The author may decide (let's say) that this value is suited for
any application possessing any configuration coordinates.  In this
case, the value received by an instance of application _A_ identified
by one set of configuration coordinates, asking for a value for the
`color` configuration property will be the same as the value received
by another instance of _A_ identified by a different set of
configuration coordinates asking for a value for the `color`
configuration property.  The author thus wrote one configuration
value, but from the standpoint of the querying application instances,
there are two values in play.

(_Configuration value_ may also refer to the [`ConfigurationValue`][9]
service provider class returned by [`Configuration`][2] instances.)

Configuration
-------------

A _configuration_ is simply a collection of some configuration
properties and their values designated for one or more
points&mdash;identified by configuration coordinates&mdash;in
configuration space.

(_Configuration_ can also mean the actual [`Configuration`][2] object
in the framework.)

Usage Patterns
==============

Application
-----------

Applications wishing to use the microBean™ Configuration project create
a [`Configurations`][0] object and then call its [`getValue()`][1]
methods.  The [`Configurations`][0] object is the centerpiece of the
microBean™ Configuration framework.

Deployment and Integration
--------------------------

Individual configurations are represented by implementations of
the [`Configuration`][2] service provider interface.  These
implementations are loaded at [`Configurations`][0] [creation time][3]
by the standard Java SE [`ServiceLoader` infrastructure][4].  To
activate a given [`Configuration`][2] implementation, put the name of
its class on one line in a file named
`META-INF/services/org.microbean.configuration.spi.Configuration`
located on the classpath of the application.

A [`Configuration`][2] implementation is responsible for determining
whether it contains a configuration value that is suitable for a
supplied set of configuration coordinates and a configuration
property.  If it does not contain such a value, then it returns
`null`.  If it _does_ contain such a value, then it must not only
return the `String` value it houses, but also a set of configuration
coordinates for which that value is suitable.

The set of configuration coordinates returned is either exactly the
set supplied to the [`Configuration`][2], or a subset of it.  This
causes the returned value to be seen as an _exact match_ or a merely
_suitable match_.  You can think of matches in terms of specificity:
a configuration value that is said to exactly match a configuration
property and a set of configuration coordinates is maximally specific;
a configuration value that is said to suitable match a configuration
property and a set of configuration coordinates is less specific.

Conversion
----------

Configuration values are, at their root, always `String`s.  They are
converted to other types by [`Converter`s][5].
Like [`Configuration`][2] instances, [`Converter`s][5] are loaded by
the standard Java SE [`ServiceLoader` infrastructure][4].  To activate
a given [`Converter`][5] implementation, put the name of
its class on one line in a file named
`META-INF/services/org.microbean.configuration.spi.Converter`
located on the classpath of the application.

Clients who wish to understand what [`Type`s][7] exist to which
`String` configuration values may be converted may call
the [`Configurations#getConversionTypes()`][8] method.

Dispute Arbitration
-------------------

From time to time, a configuration property may have several values
that are suitable for it.  For example, for a hypothetical
configuration property named `databaseUrl`, there may be a configuration
value assigned to it for the configuration coordinates represented by
this listing:

    environment=test
    tenantId=34
    
...and a different value assigned to it suitable for this listing:

    environment=test
    dataCenter=Nevada
    
Both sets of configuration coordinates represent suitable matches for application
located in configuration space by the following coordinates:

    locale=en_US
    environment=test
    region=west
    dataCenter=Nevada
    tenantId=34
    
...but neither is maximally specific.  So should the application use
the value suitable for the first listing, or the value suitable for
the second listing?

This is a _configuration dispute_.  Configuration disputes may be
resolved by [`Arbiter`][6] instances.  Like [`Configuration`][2]
instances, [`Arbiter`s][6] are loaded by the standard Java
SE [`ServiceLoader` infrastructure][4].  To activate a
given [`Arbiter`][6] implementation, put the name of its class on
one line in a file named
`META-INF/services/org.microbean.configuration.spi.Arbiter` located
on the classpath of the application.

[0]: apidocs/org/microbean/configuration/Configurations.html
[1]: apidocs/org/microbean/configuration/Configurations.html#getValue-java.util.Map-java.lang.String-
[2]: apidocs/org/microbean/configuration/spi/Configuration.html
[3]: apidocs/org/microbean/configuration/Configurations.html#Configurations--
[4]: https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html
[5]: apidocs/org/microbean/configuration/spi/Converter.html
[6]: apidocs/org/microbean/configuration/spi/Arbiter.html
[7]: https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Type.html
[8]: apidocs/org/microbean/configuration/Configurations.html#getConversionTypes--
[9]: apidocs/org/microbean/configuration/spi/ConfigurationValue.html
[10]: https://microbean.github.io/microbean-configuration-api/
