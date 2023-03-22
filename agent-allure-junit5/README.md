# Report Portal Allure integration for JUnit 5 framework
## Simple Installation
The Agent uses JUnit's service location mechanism to integrate into tests. So in general if you already have Allure installed you just need
to include Agent dependency.
### Gradle
```groovy
dependencies {
  testImplementation 'com.epam.reportportal:agent-allure-junit5:5.1.0'
}
```

### Maven
```xml
<dependency>
    <groupId>com.epam.reportportal</groupId>
    <artifactId>agent-allure-junit5</artifactId>
    <version>5.1.0</version>
    <scope>test</scope>
</dependency>
```
### Property configuration file

To start using Report Portal you need to create a file named `reportportal.properties` in your Java project in a source folder
`src/main/resources` or `src/test/resources` (depending on where your tests are located):

**reportportal.properties**

```
rp.endpoint = http://localhost:8080
rp.uuid = e0e541d8-b1cd-426a-ae18-b771173c545a
rp.launch = JUnit 5 Tests
rp.project = default_personal
```

**Property description**

* `rp.endpoint` - the URL for the report portal server (actual link).
* `rp.api.key` - an access token for Report Portal which is used for user identification. It can be found on your report portal user profile
  page.
* `rp.project` - a project ID on which the agent will report test launches. Must be set to one of your assigned projects.
* `rp.launch` - a user-selected identifier of test launches.

## No AspectJ configuration
If you don't have Allure configured yet and to be able to use all Agent features you also need to include AspectJ Java agent (usually it's
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
            -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/1.9.5/aspectjweaver-1.9.5.jar"
        </argLine>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.9.5</version>
        </dependency>
        <dependency>
            <groupId>org.apache.maven.surefire</groupId>
            <artifactId>surefire-junit-platform</artifactId>
            <version>2.22.2</version>
        </dependency>
    </dependencies>
</plugin>
```
