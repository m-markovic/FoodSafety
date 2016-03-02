# FoodSafety

Requires Java JDK 8 and maven 3.3. Refer to the following links to install these:
  * http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
  * https://maven.apache.org/download.cgi

## Prerequisite

This project depends on version 0.9.7 of C-Sparql which is published for Maven at the time of writing (see https://github.com/streamreasoning/CSPARQL-engine/issues/14), so you need to compile and install this locally:

```
cd ..
git clone https://github.com/streamreasoning/CSPARQL-engine.git
cd CSPARQL-engine
mvn install
cd ../FoodSafety
```

## Creating an Eclipse project

```
mvn eclipse:clean eclipse:eclipse
```

## Building and running

Edit the info in ```src/test/resources/example-input.json.txt```, then
```
mvn clean package
export WTCRED="me@myemail.com mypassword" # credentials for wirelesstag.net - NO space in the password
java -ea -jar target/foodsafety-jar-with-dependencies.jar < src/test/resources/example-input.json.txt
```

## Run static analyses

Run
```
mvn site
```
then open ```target/site/project-reports.html```
