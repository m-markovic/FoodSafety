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
echo {\"from\": \"2016-01-29T15:30:00\", \"to\": \"2016-01-29T15:40:00\", \"meatProbeDir\": \"./mydata/\", \"wirelessTags\": [2,3]} > input.json
java -ea -jar target/foodsafety-jar-with-dependencies.jar < input.json
```

The application will download temperature and humidity data for the sensor in the period from ```from``` to ```to```. These will be fed to a CSPARQL engine, and one simple query will continually be reporting to ```System.out```. 

## Run static analyses

Run
```
mvn site
```
then open ```target/site/project-reports.html```
