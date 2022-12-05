# XML processor

## About the project
&emsp;&emsp; 
A simple XML processor that scans a folder **orders** (default) and process files with name pattern "orders##.xml" (default)
containing a list of orders with a list of products to other files containing products filtered by supplier, created in 
a folder **output** (default) with a name pattern <supplier>##.xml (## - number taken from orders##.xml).

## Prerequisites
- [JDK 18](https://jdk.java.net/18/)
- [Maven Apache](https://maven.apache.org/download.cgi)

## Technologies used
<div style="display: flex;flex-direction: row ;justify-content: space-between;">
    <img src="assets/intellij-logo.png" width="25%"  alt="IntelliJ Idea logo">
    <img src="assets/maven-logo.png" width="70%" alt="maven logo">
</div>

### Maven dependencies
- [XStream Core](https://x-stream.github.io/)
- [Project lombok](https://projectlombok.org/)
- [SLF4J API Module](https://www.slf4j.org/)
- [SLF4J LOG4J](https://www.slf4j.org/)
- [JUnit Jupiter](https://junit.org/junit5/)

## How to use

### Step 1
#### Clone the project with command
    git clone https://github.com/SidorAndrei/xml-processor
### Step 2 (Optional)
#### A. Configure your own name pattern
&emsp; Open _**src/main/resources/scan.properties**_ and you can modify input/output folder's name and input file name 
prefix and (not recommended) extension

#### B. Configure you our log file name 
&emsp; Open _**src/main/resources/log4j.properties**_ and you can change the name from the second line of the document

#### C. Create the jar file (Must be executed if any of log4j.properties or scan.properties are modified)
- Go to main folder
- Open command line (Press Win+R and type cmd, then press Enter) to the main folder
- Use command

####
    mvn install
### Step 3
#### Open the application with command
    
    java -jar .\jar\xml-processor-jar-with-dependencies.jar
