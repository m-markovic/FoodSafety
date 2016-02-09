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
java -ea -jar target/foodsafety-jar-with-dependencies.jar 2016-01-28 2016-01-29 2 3 4
```

The input parameters are:
  * fromDate, ISO local date format
  * toDate, ISO local date format
  * any number of sensor IDs
 The application will download temperature and humidity data for the sensor in the period from ```fromDate``` to ```toDate```. These will be fed to a CSPARQL engine, and one simple query will continually be reporting to ```System.out```. 

## Run static analyses

Run
```
mvn site
```
then open ```target/site/project-reports.html```
