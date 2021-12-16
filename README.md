# bachelor-performance
repository for my bachelor thesis

### +++ Because of the log4j exploit the external database from gcp was compromised and is not accessible +++




## Configuration

Database configuration can be set in Performance.java (url, username, pw)

Further comments on how to build the database:
https://github.com/rbalink/bachelor-performance/issues/14#issuecomment-963609100



## Make it run

The java source code is inside src/ and needs to be compiled with all libraries (HtmlUnit, Postgresql).

Compile the code from bachelor-performance/src

`javac -cp .:postgresql-42.2.20.jar Performance.java`

Run the code from bachelor-performance/src in bash

`java -cp .:postgresql-42.2.20.jar Performance`

Besides running the code from the terminal, the project can also be run from any IDE which supports Java 1.8
