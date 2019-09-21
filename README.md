# Voorhees JSON RPC library

Voorhees is a simple library helping to build JSON RPC 2.0 over HTTP
services in Spring applications in a quite fast and seamless way.

## Table of Contents

  * [Installation](#installation)
  * [Spring Integration](#spring-integration)
    * [Spring Boot Applications](#spring-boot-applications)
    * [Spring Applications](#spring-applications)
  * [Services](#services)
    * [Named Parameters](#named-parameters)
    * [Default Values](#default-values)
    * [Endpoint Prefix](#endpoint-prefix)
  * [Client (Java)](#client-java)
  * [Client (Python)](#client-python)
  * [Exception Handling](#exception-handling)
    * [JSON RPC Specific Errors](#json-rpc-specific-errors)
    * [User Defined Errors](#user-defined-errors)
  * [Client library generation](#client-library-generation)
    * [Groovy plugin](#groovy-plugin)

## Installation

It's not yet deployed in public maven repositories so use it as system
artifact, or deploy it to your local maven repository.

## Spring Integration

### Spring Boot Applications

There is no need to take any additional step in order to integrate
Voorhees. Having the jar dependency in your class path means that
Spring Boot application will be configured automatically.

To disable Voorhees auto-configuration, you want to set application
property `spring.autoconfigure.exclude`:

```yaml
spring:
  autoconfigure:
    exclude: com.hylamobile.voorhees.server.spring.config.VoorheesAutoConfiguration
```

### Spring Applications

To enable Voorhees in your Spring applications, use
[`@EnableVoorhees`](voorhees-server/src/main/kotlin/com/hylamobile/voorhees/server/spring/annotation/EnableVoorhees.kt)

```java
@EnableVoorhees
public class JsonRpcConfiguration {
}
```

## Services

A service in Voorhees is just a simple class marked as
[`@JsonRpcService`](voorhees-server/src/main/kotlin/com/hylamobile/voorhees/server/annotation/JsonRpcService.kt):

```java
@JsonRpcService(location = "/my")
public class MyService {

    public Person getMe() {
        return ME;
    }

    public int getAge(Person person) {
        return person.getAge();
    }

    public Person birthday(Person person) {
        person.setAge(person.getAge() + 1);
        return person;
    }
}
```

Please note that `location` attribute should specify at least one endpoint. 

All the public methods of this service class (but not its' superclasses)
will be exposed.

If you want to suppress exposing specific method, annotate it with
[`@DontExpose`](voorhees-server/src/main/kotlin/com/hylamobile/voorhees/server/annotation/DontExpose.kt).

```java
@JsonRpcService(location = "/my")
public class MyService {
    @DontExpose
    public Person getMe() {
        return ME;
    }

    // ...
}
```

### Named Parameters

Since JSON RPC supports named parameters for RPC method, Voorhees uses
parameter names of the service method as RPC method parameter names.
Java could strip this information from class files, so sometimes it's
impossible to get parameter name using Java reflection. It returns
names like `arg0`, `arg1` etc. Sometimes there is additional information
(Kotlin classes metainfo, AspectJ metainfo, etc.) that potentially could
be used in order to get parameter names. And this is exactly what Spring
parameter name discoverer does. Voorhees uses it to find these names.

But there is also other way to name the parameters: Use `name` attribute
of the [`@Param`](voorhees-server/src/main/kotlin/com/hylamobile/voorhees/server/annotation/Param.kt)
annotation:

```java
@JsonRpcService(location = "/my")
public class MyService {
    public int sum(@Param("left") int x, @Param("right") int y) {
        return x + y;
    }

    // ...
}
```

In that case `left` and `right` are being used as names when RPC with named
parameters is executed:

```python
myService.sum(left=3, right=4)
myService.sum(right=4, left=3)
```

### Default Values

Voorhees support default values for the method parameters. It means that
if there is no enough parameters in method call, and the rest of the
parameters already have default values, these values are to be used.

For example:

```java
@JsonRpcService(location = "/my")
public class MyService {
    public int sum(@Param(defaultValue="0") int x, @Param(defaultValue="1") int y) {
        return x + y;
    }

    // ...
}
```

We can call it as
```python
myService.sum(3, 4)
myService.sum(3)
myService.sum()
```

Note that default values are strings keeping JSON representation of the
default value. The only exception is string value. So if Voorhees is not
able to parse default value, it uses it as is if the type of the
parameter is String.

### Endpoint Prefix

In order to specify the common API prefix for all JSON RPC services use
`spring.voorhees.server.api.prefix` Spring property:

```yaml
spring:
  voorhees:
    server:
      api:
        prefix: /api
```

Note that it should start with slash or be an empty string.

For example, if prefix is `/api` and service location is `/my` then the
real enspoint is going to be `/api/my`.

## Client (Java)

To use a Voorhees client the service interface is required:

```java
public interface RemoteMyService {
    Person getMe();
    int getAge(Person person);
    Person birthday(Person person);
}
```

To set up it, we need to know where the server is located and what is
the service endpoint:

```java
ServerConfig serverConfig = new ServerConfig("http://localhost:8080/");
JsonRpcClient client = new JsonRpcClient(serverConfig);
```

The client is thread-safe, we can use it from parallel thread to get
the remote services. To get the remote service, provide service's
endpoint and interface:

```java
RemoteMyService myService = client.getService("/my", RemoteMyService.class);
```

That's it! Just use it as a regular java class:

```java
Person me = myService.getMe();
System.out.println(myService.getAge(me));
me = myService.birhtday(me);
System.out.println(myService.getAge(me));
```

To be updated

## Client (Python)

Let's take Python as an example of dynamically typed language to show
how easy is to use JSON RPC with it (`jsonrpc` library):

```python
from jsonrpc import ServiceProxy

myService = ServiceProxy("http://localhost:8080/my")

me = myService.getMe()
print(myService.getAge(me))
me = myService.birthday(me)
print(myService.getAge(me))
```

Note that you even don't have to create a remote service stub.

## Exception Handling

To be updated

### JSON RPC Specific Errors

To be updated

### User Defined Errors

To be updated

## Client Library Generation

Currently it's possible to generate client library instead of
implementing it manually.

### Groovy plugin

Voorhees groovy plugin provides a possibility to generate client
library, install it to local maven repository or publish to remote one.

Apply `voorhees` in build.gradle. If you want to publish client library
apply `maven-publish` plugin as well. For example:
```groovy
plugins {
	id "java"
	id "maven-publish"
	id "voorhees"
}
```

Define where you want the plugin to search for remote services in
`packagesToScan` attribute of `voorhees` extension. If you intend to
publish client library, you want to define `artifact` as well as
`group` and `version`. Note that if `group` and `version` are not
defined (as in example below) then `group` and `version` of the current
project will be used:
```groovy
group = "com.acme"
version = "0.0.1"

voorhees {
	packagesToScan = ["com.acme.remote"]
	artifact = "myservice-client"
}
```

Since your application uses `voorhees-server` it should be defined in
dependencies:
```groovy
dependencies {
	compile("com.hylamobile:voorhees-server:2.0.0")
	// other deps: spring etc.
}
```

Currently plugin is not yet deployed to public repositories, so please
use your own as in example below:
```groovy
repositories {
	jcenter()
	maven {
		url mavenRepoUrl
		credentials {
			username mavenRepoUsername
			password mavenRepoPassword
		}
	}
}
```

Same for publishing. Just use the repository you want to publish client
library to. Currently only maven repository is supported:
```groovy
publishing {
	repositories {
		// repository to publish client library
		maven {
			url mavenRepoUrl
			credentials {
				username mavenRepoUsername
				password mavenRepoPassword
			}
		}
	}
}

```

Task provided by the plugin:

* **generateJsonRpcClient** Generates classes in `build/classes/voorhees` directory.
* **jarJsonRpcClient** Archives generated classes to a jar in `build/libs`.
* **publishJsonRpcClient** (only if `maven-publish` is active) Publishes generated jar to maven repository.

Apart from that, `maven-publish` itself creates
`publishJsonRpcClientPublicationToMavenLocal` and
`publishJsonRpcClientPublicationToMavenRepository`.

Please note that only classes from the packages mentioned in
`packagesToScan` attribute and their subpackages (recursively) will be
generated for client library.
