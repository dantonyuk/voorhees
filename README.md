# Voorhees JSON RPC library

![Logo](doc/images/voorhees-logo.gif)

Voorhees is a simple library helping to build JSON RPC 2.0 over HTTP
services in Spring applications in a quite fast and seamless way.

## Table of Contents

  * [Installation](#installation)
  * [Services](#services)
    * [Renaming Methods](#renaming-methods)
    * [Named Parameters](#named-parameters)
    * [Default Values](#default-values)
    * [Endpoint Prefix](#endpoint-prefix)
  * [Client (Java)](#client-java)
  * [Client (Python)](#client-python)
  * [Exception Handling](#exception-handling)
    * [JSON RPC Specific Errors](#json-rpc-specific-errors)
    * [User Defined Errors](#user-defined-errors)
  * [Spring Integration](#spring-integration)
    * [Spring Boot Applications](#spring-boot-applications)
    * [Spring Applications](#spring-applications)
    * [Client in Spring Boot](#client-in-spring-boot)
  * [Client library generation](#client-library-generation)
    * [Groovy plugin](#groovy-plugin)

## Installation

It's not yet deployed in public maven repositories so use it as system
artifact, or deploy it to your local maven repository.

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

In case you want to register services programmatically, there is a way
to do that:

```java
@Autowired
private JsonRpcHandlerMapping jsonRpcMapping;

jsonRpcMapping.registerService(new MyService(), "/my");
```

It could be helpful if you prefer to not use annotations, or it's just
impossible 'cause a service is a third-party class.

### Renaming Methods

By default the Java method name is used by JSON RPC. However, it is
possible to change it.

For example, one can add prefix to all exposed methods of a particular
service:

```java
@JsonRpcService(location = "/my", prefix = "mine")
public class MyService {

    public Person getMe() {
        return ME;
    }
```

The method name in this case will be "mine.getMe" not "getMe".

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
real endpoint is going to be `/api/my`.

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

If an exception has been thrown during remote method call, JSON RPC
returns error object with a code specific for this error. Depending on
this code a client could throw specific exception that is mapped to this
code.

### JSON RPC Specific Errors

There are several predefined errors in JSON RPC:

| Code   | Description      | Exception               |
|--------|------------------|-------------------------|
| -32700 | Parse error      | ParseErrorException     |
| -32600 | Invalid Request  | InvalidRequestException |
| -32601 | Method not found | MethodNotFoundException |
| -32602 | Invalid params   | InvalidParamsException  |
| -32603 | Internal error   | InternalErrorException  |

### User Defined Errors

One can specify their own custom exception. In order to allow Spring
to automatically pick it up, the exception should have static field
`int CODE` with the error code this exception is mapped to.

```java
public class FieldException extends JsonRpcException {

    private static final int CODE = 214;

    public FieldException(String message, List<String> fields) {
        super(new Error(CODE, message, userErrorCodes));
    }
}
```

If Spring Boot is not in use, the exception should be registered
directly in JSON RPC client:

```java
jsonRpcClient.registerException(FieldException.class)
```

If this exception is thrown, the JSON RPC client parses it properly,
for example if we have response

```json
{
  "id": 1,
  "jsonrpc": "2.0",
  "error": {
    "code": 214,
    "message": "Some field values are invalid",
    "data": [
      "confirmation should be same as password",
      "email is invalid"
    ]
  }
}
```

then on the client side this exception will be thrown:

```java
throw new FieldException("Some field values are invalid", listOf(
      "confirmation should be same as password",
      "email is invalid"
));
```

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

### Client in Spring Boot

If you use [client-library generation procedure](#client-library-generation)
you may take an advantage of remote services auto-registration:

```groovy
dependencies {
	compile("com.hylamobile:voorhees-client-springboot:3.0.0-RC1")
    // ...
}
```

Instead of manual creating of JSON RPC clients and building services
out of them, just describe them in `application.yml`:

```yaml
voorhees:
  client:
    basePackage: com.acme.remote
    services:
      default:
        endpoint: http://server.com/api
      user-service:
        endpoint: http://server.com/user-service
        targets: com.acme.remote.user
        restTemplate: userRestTemplate
```

There could be `default` client that builds all of the services from
the `basePackage` that are not listed in `targets` of any other service.

The property `target` is a list of service classes and/or packages that
are supposed to be scanned recursively in order to find services for
the current client. E.g. in the example provided above for the client
`user-service` package `com.acme.remote.user` will be scanned to pick up
all the services from it.

Apparently `endpoint` property binds specified endpoint to all belonging
services.

By default Voorhees uses JSON RPC transport that is located in classpath
(see JSON RPC transport). But for auto-registered services if the
property `restTemplate` is specified then custom transport will be used.
This transport uses `RestTemplate` bean with specified name to get the
data from the JSON RPC server. In the example above for `user-service`
client `userRestTemplate` will be used. It's helpful if you need to
define specific HTTP properties, e.g. authentication etc.

To use a service just autowire it:

```java
@Autowired
private MyService myService;
```

Voila!

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
	compile("com.hylamobile:voorhees-server:3.0.0-RC1")
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
