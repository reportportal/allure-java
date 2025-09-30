# Report Portal Allure integration for TestNG framework

## Simple Installation

The Agent uses TestNG's service location mechanism to integrate into tests. So in general if you already have Allure
installed you just need
to include Agent dependency.

### Gradle

```groovy
dependencies {
    testImplementation 'com.epam.reportportal:agent-allure-testng:5.4.0'
}
```

### Maven

```xml

<dependency>
    <groupId>com.epam.reportportal</groupId>
    <artifactId>agent-allure-testng</artifactId>
    <version>5.4.0</version>
    <scope>test</scope>
</dependency>
```

### Property configuration file

To start using Report Portal you need to create a file named `reportportal.properties` in your Java project in a source
folder
`src/main/resources` or `src/test/resources` (depending on where your tests are located):

**reportportal.properties**

```
rp.endpoint = http://localhost:8080
rp.uuid = e0e541d8-b1cd-426a-ae18-b771173c545a
rp.launch = TestNG Tests
rp.project = default_personal
```

**Property description**

* `rp.endpoint` - the URL for the report portal server (actual link).
* `rp.api.key` - an access token for Report Portal which is used for user identification. It can be found on your report
  portal user profile
  page.
* `rp.project` - a project ID on which the agent will report test launches. Must be set to one of your assigned
  projects.
* `rp.launch` - a user-selected identifier of test launches.

## No AspectJ configuration

If you don't have Allure configured yet and to be able to use all Agent features you also need to include AspectJ Java
agent (usually it's
already done in scope of Allure configuration).

### Gradle

```groovy
test {
    doFirst {
        def weaver = configurations.testRuntimeClasspath.find { it.name.contains("aspectjweaver") }
        jvmArgs += "-javaagent:$weaver"
    }
}
```

### Maven

You need to update your surefire plugin configuration (find existing not creating new) as here:

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.22.2</version>
    <configuration>
        <argLine>
            -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/${aspectj.version}/aspectjweaver-${aspectj.version}.jar"
        </argLine>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>${aspectj.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.maven.surefire</groupId>
            <artifactId>surefire-testng</artifactId>
            <version>2.22.2</version>
        </dependency>
    </dependencies>
</plugin>
```
