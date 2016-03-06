#!/bin/bash

# Build everything with maven, and then move it all into a single directory

source vars.sh

echo "Cleaning old output"
rm -rfv $DIRECTORY
echo "Building with maven"
mvn clean package
echo "Moving new output"
mkdir -pv $DIRECTORY
cp */target/*.jar $DIRECTORY/
echo "Cleaning unneeded output"
rm -rfv $DIRECTORY/original-*.jar
