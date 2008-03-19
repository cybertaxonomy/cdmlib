#!/bin/sh

# Simple example shell script which demonstrates
# how to use the PDFDoclet with javadoc directly
# (which means: without ANT).
CFG=cdmlib-config.properties
BASEPCKG="eu.etaxonomy.cdm.model"
PACKAGES="$BASEPCKG.agent $BASEPCKG.common $BASEPCKG.description $BASEPCKG.location $BASEPCKG.molecular $BASEPCKG.name $BASEPCKG.occurrence $BASEPCKG.package.html $BASEPCKG.reference $BASEPCKG.taxon"
SRC=../cdmlib-model/src/main/java
JAVA_HOME=/Library/Java/Home

PATH=$JAVA_HOME/bin
VERSION=1.0.2
DOCLET=com.tarsec.javadoc.pdfdoclet.PDFDoclet
JARS=pdfdoclet-$VERSION-all.jar


export JAVA_HOME PATH DOCLET JARS PACKAGES

javadoc -doclet $DOCLET -docletpath $JARS -config $CFG -sourcepath $SRC $PACKAGES
