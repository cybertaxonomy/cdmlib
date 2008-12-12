#! /bin/sh

rm ./cdmlib-commons-2.0src.zip ./cdmlib-commons-2.0.jar ./cdmlib-model-2.0src.zip \
 ./cdmlib-model-2.0.jar ./cdmlib-persistence-2.0src.zip ./cdmlib-persistence-2.0.jar \
 ./cdmlib-services-2.0src.zip ./cdmlib-services-2.0.jar ./cdmlib-io-2.0src.zip \
 ./cdmlib-io-2.0.jar ./cdmlib-ext-2.0src.zip ./cdmlib-ext-2.0.jar > /dev/null 2>&1 


LIBDIR=~/.m2/repository/eu/etaxonomy

cp $LIBDIR/cdmlib-commons/2.0/cdmlib-commons-2.0.jar .
cp $LIBDIR/cdmlib-commons/2.0/cdmlib-commons-2.0-sources.jar ./cdmlib-commons-2.0src.zip

cp $LIBDIR/cdmlib-model/2.0/cdmlib-model-2.0.jar .
cp $LIBDIR/cdmlib-model/2.0/cdmlib-model-2.0-sources.jar ./cdmlib-model-2.0src.zip

cp $LIBDIR/cdmlib-persistence/2.0/cdmlib-persistence-2.0.jar .
cp $LIBDIR/cdmlib-persistence/2.0/cdmlib-persistence-2.0-sources.jar ./cdmlib-persistence-2.0src.zip

cp $LIBDIR/cdmlib-services/2.0/cdmlib-services-2.0.jar .
cp $LIBDIR/cdmlib-services/2.0/cdmlib-services-2.0-sources.jar ./cdmlib-services-2.0src.zip

cp $LIBDIR/cdmlib-io/2.0/cdmlib-io-2.0.jar .
cp $LIBDIR/cdmlib-io/2.0/cdmlib-io-2.0-sources.jar ./cdmlib-io-2.0src.zip

cp $LIBDIR/cdmlib-ext/2.0/cdmlib-ext-2.0.jar .
cp $LIBDIR/cdmlib-ext/2.0/cdmlib-ext-2.0-sources.jar ./cdmlib-ext-2.0src.zip