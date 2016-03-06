#!/bin/bash

source vars.sh

cd $DIRECTORY
# Now this is a truly ugly hack. For some reason, bash doesn't want to expand things properly,
# so we list everything in $DIRECTORY and convert it into a colon-delimited string
CLASSPATH=$(ls . | sed -e ':a;N;$!ba;s/\n/:/g' -e 's/^/\/usr\/lib\/jvm\/java-8-oracle\/lib\/tools.jar:/')

echo $CLASSPATH

# Assumes that Oracle JDK 8 is installed. Terrible to use a hardcoded path, I know, but
# $JAVA_HOME isn't set on my system and setting it is work. ._.
java -cp $CLASSPATH me.curlpipesh.jritMain.JRIT
