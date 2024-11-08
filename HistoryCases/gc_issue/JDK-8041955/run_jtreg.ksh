#! /bin/ksh

while /java/re/jtreg/4.1/promoted/latest/binaries/jtreg/solaris/bin/jtreg -retain:pass -jdk:$JAVA_HOME -server TestGCLogMessages.java
	do date
done
