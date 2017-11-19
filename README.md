Sometimes, the code needs to know things about the way it is being executed.
For instance:

- if this a test, use test data; 
- if this is a stand-alone run, terminate subsystems that would prevent JVM exit -
 but don't do this if running in a webapp;   

I needed a way to answer such questions from within the running code on multiple projects
(at multiple employers). So, not to waste any more time on doing the same thing again
(and slightly differently every time), I decided to record my approach - and publish
the code.

The code supports Maven, Gradle, Intellij Idea IDE and Tomcat.
This is all I currently use it for, but if there is an interest in supporting
other tools, I might consider it :)

## Tests

### Tests on the command line

Gradle (in "gradle" directory):

`gradle run`

`gradle test`


Maven (in "maven/main" directory):

`mvn exec:java`

`mvn test`


### Tests in Intellij Idea

Import appropriate project into Idea; then:

Main run

Main debug

Test run

Test debug

Web (war) run

Web (war) debug

Web (exploded) run

Web (exploded) debug
