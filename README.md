Junit-Support
=============

[![Maven Central](https://img.shields.io/maven-central/v/de.mklinger.commons/junit-support.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22de.mklinger.commons%22%20AND%20a%3A%22junit-support%22)


What is it?
--

Junit-Support provides base classes for JUnit tests to cover common tests.

- Test bean classes: Do all setters correspond to their getters? Do copy
  constructors cover all bean properties? Are `equals()` and
  `hashCode()` implemented correctly?
- Test delegate classes: Do methods expected to call the delegate actually
  call the delegate?
- Test exception classes: Do the standard exception constructors exist and
  work as expected?


Usage
--

Maven dependency:

```xml
<dependency>
    <groupId>de.mklinger.commons</groupId>
    <artifactId>junit-support</artifactId>
    <version>0.10</version>
</dependency>
```

For a given bean class `MyBean` a basic test looks like this:

```java
public class MyBeanTest extends BeanTestBase<MyBean> {
}
```

License
--

Junit-Support is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
