# Report Portal Allure integration for JUnit 5 framework
## Simple Installation
The Agent uses JUnit's service location mechanism to integrate into tests. So in general if you already have Allure installed you just need
to include Agent dependency.
### Gradle
```groovy
dependencies {
    'com.epam.reportportal:agent-allure-junit5:$LATEST_VERSION'
}
```

### Maven
```xml
<dependency>
    <groupId>com.epam.reportportal</groupId>
    <artifactId>agent-allure-junit5</artifactId>
    <version>$LATEST_VERSION</version>
    <scope>test</scope>
</dependency>
```

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
