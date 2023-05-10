Overview

This console application is using Spring-boot and Project Reactor in order to provide best possible performance in given requirements. 
Reactor offers an opportunity to perfrom non-blocking operations, async http requests and parallel processing for large amounts of data.

**Final score:** 500 articles - 17 seconds. Thus, in order to process 40000 articles it is required - 20 minutes.

Due to <body>Too many requests -- error 999.</body> the retry and fallback logic was added.  I
In order to configure amount of articles to be processed from the file you can update articles.batchSize properpy in application.properties file.


To start-up application follow this steps:
1) Install JDK 17
2) Install Gradle > 7.4.2
3) Navigate to root project directory 
4) Perform 'gradle build'
5) Perform 'java -jar build/libs/fire-fly-0.0.1-SNAPSHOT.jar' to start up the application.
