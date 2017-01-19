# Getting started

* This project is implementation of open source Apache Directory API which can used to query and update any LDAP Directory server.

# Prerequisites
* This project will require below software to execute
1. Apache Maven 3.3.9(This version is not specifically required).
2. JDK 1.7(If you need to change the JDK version please do change it in pom.xml also).
3. You need to set MAVEN_HOME and JAVA_HOME variable in your host environment variables where you need to run the program.
4. Also you need to set PATH variable of your host environment variable.

# Assumptions
* Attributes are hardcoded now and is not configurable.

# Project build instructions
* Import the project into Eclipse by selecting option "Importing Existing Maven project into workspace".
* Ensure prerequisites are fulfilled as mentioned above.
* Checkout the project using git repository URL [https://github.com/hippodigital/syncldapattributes.git](https://github.com/hippodigital/syncldapattributes.git)
* Execute command `mvn clean install` from command line by navigating to project folder.
* This command will download all dependencies from Maven Repository and you should be able to execute the test cases from eclipse or command line.
* Maven will create an executable jar file named ldapattributesync.jar.
* To execute the jar file using below command 
` java -jar ldapattributesync.jar`
