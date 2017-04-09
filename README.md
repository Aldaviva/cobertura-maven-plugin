cobertura-maven-plugin
===

This is a [Maven](https://maven.apache.org/) plugin that lets you measure code coverage of your tests using 
[Cobertura](https://cobertura.github.io/cobertura/). It's a different project than the official 
[`org.codehaus.mojo:cobertura-maven-plugin`](http://www.mojohaus.org/cobertura-maven-plugin/).

## Features

- Tests run once, not twice
- Minimum thresholds for line or branch coverage percentage that will allow the build to succeed
- Exclude certain files from coverage threshold checks
- Cobertura is compatible with testing utilities that modify bytecode, like the fantastic 
[PowerMock](https://powermock.github.io/), because classes are instrumented on disk before tests start, not during 
runtime with a JVM agent like the otherwise-fantastic [JaCoCo](http://www.eclemma.org/jacoco/).

## Usage

### Installation

Add these snippets to your POM.

```xml
<project>
    <dependencies>
        <!-- This allows tests to update coverage counters when instructions are executed -->
        <dependency>
            <groupId>net.sourceforge.cobertura</groupId>
            <artifactId>cobertura</artifactId>
            <version>2.1.1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>com.aldaviva.coverage</groupId>
                <artifactId>cobertura-maven-plugin</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <configuration>
                    <exclusions>
                        <!-- Exclude any files you don't want counting against your coverage thresholds. -->
                             Ant path syntax:
                               ** = multiple directories
                               *  = single directory or multiple characters
                               ?  = single character
                        -->
                        <param>**/*Exception.class</param>
                        <param>com/mycompany/myprogram/Untestable.class</param>
                    </exclusions>
                    
                    <!-- Specify minimum coverage thresholds for lines of code or branching statements as a floating-point number between 0.0 (no coverage) and 1.0 (full coverage) -->
                    <minLineCoveredRatio>0.80</minLineCoveredRatio>
                    <minBranchCoveredRatio>0.80</minBranchCoveredRatio>
                </configuration>

                <executions>
                    <!-- Add instrumentation instructions to bytecode before tests run -->
                    <execution>
                        <id>instrument</id>
                        <goals>
                            <goal>instrument</goal>
                        </goals>
                        <phase>process-test-classes</phase>
                    </execution>

                    <!-- Restore uninstrumented class files after tests are done, and fail the build if coverage thresholds were not met -->
                    <execution>
                        <id>check</id>
                        <goals>
                            <goal>check</goal>
                        </goals>
                        <phase>prepare-package</phase>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Execution example
```text
$ mvn package
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building Sample project 0.0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ sample-project ---
[INFO] Deleting C:\Users\Ben\Documents\Projects\sample-project\target
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ sample-project ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 0 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.6.1:compile (default-compile) @ sample-project ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 2 source files to C:\Users\Ben\Documents\Projects\sample-project\target\classes
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ sample-project ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory C:\Users\Ben\Documents\Projects\sample-project\src\test\resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.6.1:testCompile (default-testCompile) @ sample-project ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to C:\Users\Ben\Documents\Projects\sample-project\target\test-classes
[INFO] 
[INFO] --- cobertura-maven-plugin:0.0.1-SNAPSHOT:instrument (instrument) @ sample-project ---
[INFO] Cobertura: Saved information on 1 classes.
[INFO] 
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ sample-project ---
[INFO] Surefire report directory: C:\Users\Ben\Documents\Projects\sample-project\target\surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.aldaviva.coverage.MainTest
Configuring TestNG with: org.apache.maven.surefire.testng.conf.TestNG652Configurator@1963006a
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.402 sec
[INFO] Cobertura: Loaded information on 1 classes.
[INFO] Cobertura: Saved information on 1 classes.

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- cobertura-maven-plugin:0.0.1-SNAPSHOT:check (check) @ sample-project ---
[INFO] Cobertura: Loaded information on 1 classes.
[INFO] Line coverage: 85.7% actual, 100.0% required.
[INFO] Branch coverage: 50.0% actual, 100.0% required.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.410 s
[INFO] Finished at: 2017-04-09T07:28:17-07:00
[INFO] Final Memory: 19M/248M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal com.aldaviva.coverage:cobertura-maven-plugin:0.0.1-SNAPSHOT:check (check) on project sample-project: Coverage checks not met -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
[INFO] Cobertura: Loaded information on 1 classes.
[INFO] Cobertura: Saved information on 1 classes.
```

Cobertura's data file will be saved as `cobertura.ser` in the `${project.baseDir}` directory, so your build machine can generate useful coverage reports.