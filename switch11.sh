#!/bin/sh
# Build on ubuntu22
sed -i -e "s/.*maven.compiler.target.*/<maven.compiler.target>11<\/maven.compiler.target>/g" pom.xml 
sed -i -e "s/.*maven.compiler.source.*/<maven.compiler.source>11<\/maven.compiler.source>/g" pom.xml 