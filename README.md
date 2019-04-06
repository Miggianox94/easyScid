# EasyScid

EasyScid is a Java library that allow hot-deployments of your application at method level.
The base idea is to store the method's code in a caching system that is initialized and refreshed using a persisted Database.

Actually the library is tested only for Spring Boot applications but in the future it will be integrable in all Java applications.

## Installation

For now these are the steps (future improvements are planned):

__1. Add the following items to your pom.xml:__
``` 
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.3.RELEASE</version>
		<relativePath /> <!-- lookup parent from repository -->
	</parent>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>

		<!-- JEDIS -->
		<dependency>
			<groupId>redis.clients</groupId>
			<artifactId>jedis</artifactId>
			<type>jar</type>
			<scope>compile</scope>
		</dependency>

		<dependency>
			<groupId>com.sun</groupId>
			<artifactId>tools</artifactId>
			<version>1.8</version>
		</dependency>

		<!-- SPRING AOP -->
		<dependency>
			<groupId>org.aspectj</groupId>
			<artifactId>aspectjweaver</artifactId>
			<scope>compile</scope>
		</dependency>
	</dependencies>

	<build>
		<resources>
			<resource>
				<directory>src/main/java</directory>
				<includes>
					<include>**/*.java</include>
				</includes>
			</resource>
		</resources>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
```
*note that you need to install com.sun.tools to your local repository in order to reference it

**note that the java files will be included in the packaged artifact (jar, war ecc..)

__2. Add the .jar library as dependency in your project: easyscid-core-0.0.1.jar__

__3. Mark your methods with the @EasyScidMethod annotation and specify your own proprierties__

## Usage

```python
TODO: need to add some examples
```

## Roadmap

- [x] Share the demo idea on github
- [ ] Make better docs
- [ ] Develop unit tests
- [ ] Put on maven the jar dependency
- [ ] Develop the generalized version for any Java application
- [ ] Test on distributed enviroments
- [ ] Make performance tests
- [ ] Add a cached system (actually every request get a fresh version of the code)


## License
[GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)
