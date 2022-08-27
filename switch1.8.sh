#!/bin/sh
# Build on ubuntu14
sed -i -e "s/.*maven.compiler.target.*/<maven.compiler.target>1.8<\/maven.compiler.target>/g" pom.xml 
sed -i -e "s/.*maven.compiler.source.*/<maven.compiler.source>1.8<\/maven.compiler.source>/g" pom.xml 