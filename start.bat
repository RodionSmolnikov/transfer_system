IF "%M2_HOME%"=="" SET M2_HOME=D:\development\apache-maven-3.5.0
IF "%JAVA_HOME%"=="" SET JAVA_HOME=C:\Program Files\Java\jdk1.8.0_161
SET JAVA_HOME=C:\Program Files\Java\jdk1.8.0_161

CALL "%M2_HOME%\bin\mvn" package

CALL "%JAVA_HOME%\bin\java" -jar target\money.transfer.system-0.1-jar-with-dependencies.jar