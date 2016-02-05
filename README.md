# FoodSafety

Requires Java JDK 8 and maven 3.3. Refer to the following links to install these:
  * http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
  * https://maven.apache.org/download.cgi

## Creating an Eclipse project

```
mvn eclipse:eclipse
```

## Building and running

```
mvn package
export WTCRED="me@myemail.com mypassword" # credentials for wirelesstag.net - NO space in the password
java -ea -jar target/foodsafety-jar-with-dependencies.jar
```
