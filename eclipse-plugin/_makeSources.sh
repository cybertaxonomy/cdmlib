#! /bin/sh

rm ./cdmlib-commons-2.3src.zip ./cdmlib-commons-2.3.jar ./cdmlib-model-2.3src.zip \
 ./cdmlib-model-2.3.jar ./cdmlib-persistence-2.3src.zip ./cdmlib-persistence-2.3.jar \
 ./cdmlib-services-2.3src.zip ./cdmlib-services-2.3.jar \
 ./cdmlib-io-2.3src.zip ./cdmlib-io-2.3.jar ./cdmlib-ext-2.3src.zip ./cdmlib-ext-2.3.jar > /dev/null 2>&1 


LIBDIR=~/.m2/repository/eu/etaxonomy

cp $LIBDIR/cdmlib-commons/2.3/cdmlib-commons-2.3.jar .
cp $LIBDIR/cdmlib-commons/2.3/cdmlib-commons-2.3-sources.jar ./cdmlib-commons-2.3src.zip

cp $LIBDIR/cdmlib-model/2.3/cdmlib-model-2.3.jar .
cp $LIBDIR/cdmlib-model/2.3/cdmlib-model-2.3-sources.jar ./cdmlib-model-2.3src.zip

cp $LIBDIR/cdmlib-persistence/2.3/cdmlib-persistence-2.3.jar .
cp $LIBDIR/cdmlib-persistence/2.3/cdmlib-persistence-2.3-sources.jar ./cdmlib-persistence-2.3src.zip

cp $LIBDIR/cdmlib-services/2.3/cdmlib-services-2.3.jar .
cp $LIBDIR/cdmlib-services/2.3/cdmlib-services-2.3-sources.jar ./cdmlib-services-2.3src.zip

cp $LIBDIR/cdmlib-io/2.3/cdmlib-io-2.3.jar .
cp $LIBDIR/cdmlib-io/2.3/cdmlib-io-2.3-sources.jar ./cdmlib-io-2.3src.zip

cp $LIBDIR/cdmlib-ext/2.3/cdmlib-ext-2.3.jar .
cp $LIBDIR/cdmlib-ext/2.3/cdmlib-ext-2.3-sources.jar ./cdmlib-ext-2.3src.zip
