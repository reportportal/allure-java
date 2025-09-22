# Report Portal Allure integration for JUnit framework
> For more detailed installation description (e.g. logging configuration) please see original agent documentation: 
> [reportportal/agent-java-junit](https://github.com/reportportal/agent-java-junit)
## Installation
To start using Report Portal along with Allure features you need to include this agent into your dependencies and add JUnit Foundation as
Java Agent.
### Gradle
AspectJ weaver should already been there because original Allure integration uses it, but listed here as example of using JUnit Foundation
with it.
```groovy
dependencies {
    testImplementation 'com.epam.reportportal:agent-allure-junit:5.3.2'
}

test {
  doFirst {
    def junitFoundation = configurations.runtimeClasspath.resolvedConfiguration.resolvedArtifacts.find { it.name == 'junit-foundation' }
    def weaver = configurations.testRuntimeClasspath.find { it.name.contains("aspectjweaver") }
    jvmArgs += "-javaagent:${junitFoundation.file}"
    jvmArgs += "-javaagent:$weaver"
  }
}
```

### Maven
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.myCompany</groupId>
  <artifactId>myProject</artifactId>
  <version>1.0-SNAPSHOT</version>
  
  <!-- ... -->
  
  <dependency>
      <groupId>com.epam.reportportal</groupId>
      <artifactId>agent-allure-junit</artifactId>
      <version>5.3.2</version>
      <scope>test</scope>
  </dependency>

  <!-- ... -->

  <build>
    <pluginManagement>
      <plugins>
        <!-- This part is only needed for Eclipse IDE users-->
        <plugin>
          <groupId>org.eclipse.m2e</groupId>
          <artifactId>lifecycle-mapping</artifactId>
          <version>1.0.0</version>
          <configuration>
            <lifecycleMappingMetadata>
              <pluginExecutions>
                <pluginExecution>
                  <pluginExecutionFilter>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-dependency-plugin</artifactId>
                    <versionRange>[1.0.0,)</versionRange>
                    <goals>
                      <goal>properties</goal>
                    </goals>
                  </pluginExecutionFilter>
                  <action>
                    <execute/>
                  </action>
                </pluginExecution>
              </pluginExecutions>
            </lifecycleMappingMetadata>
          </configuration>
        </plugin>
      </plugins>
    </pluginManagement>
    <plugins>
      <!-- This plugin provides the path to the Java agent (used in surefire argLine part) -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.1.1</version>
        <executions>
          <execution>
            <id>getClasspathFilenames</id>
            <goals>
              <goal>properties</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>2.22.0</version>
        <configuration>
          <testFailureIgnore>false</testFailureIgnore>
          <!--suppress UnresolvedMavenProperty -->
          <argLine>-javaagent:${org.aspectj:aspectjweaver:jar}</argLine>
          <argLine>-javaagent:${com.nordstrom.tools:junit-foundation:jar}</argLine>
        </configuration>
        <dependencies>
          <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>${aspectj.version}</version>
          </dependency>
        </dependencies>
      </plugin>
    </plugins>
  </build>
</project>
```

### Property configuration file

To start using Report Portal you need to create a file named `reportportal.properties` in your Java project in a source folder
`src/main/resources` or `src/test/resources` (depending on where your tests are located):

**reportportal.properties**

```
rp.endpoint = http://localhost:8080
rp.uuid = e0e541d8-b1cd-426a-ae18-b771173c545a
rp.launch = JUnit Tests
rp.project = default_personal
```

**Property description**

* `rp.endpoint` - the URL for the report portal server (actual link).
* `rp.api.key` - an access token for Report Portal which is used for user identification. It can be found on your report portal user profile
  page.
* `rp.project` - a project ID on which the agent will report test launches. Must be set to one of your assigned projects.
* `rp.launch` - a user-selected identifier of test launches.
