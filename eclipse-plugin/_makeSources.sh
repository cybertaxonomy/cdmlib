#! /bin/sh

rm ./cdmlib-commons-2.2src.zip ./cdmlib-commons-2.2.jar ./cdmlib-model-2.2src.zip \
 ./cdmlib-model-2.2.jar ./cdmlib-persistence-2.2src.zip ./cdmlib-persistence-2.2.jar \
 ./cdmlib-services-2.2src.zip ./cdmlib-services-2.2.jar \
 ./cdmlib-io-2.2src.zip ./cdmlib-io-2.2.jar ./cdmlib-ext-2.2src.zip ./cdmlib-ext-2.2.jar > /dev/null 2>&1


LIBDIR=~/.m2/repository/eu/etaxonomy

cp $LIBDIR/cdmlib-commons/2.2/cdmlib-commons-2.2.jar .
cp $LIBDIR/cdmlib-commons/2.2/cdmlib-commons-2.2-sources.jar ./cdmlib-commons-2.2src.zip

cp $LIBDIR/cdmlib-model/2.2/cdmlib-model-2.2.jar .
cp $LIBDIR/cdmlib-model/2.2/cdmlib-model-2.2-sources.jar ./cdmlib-model-2.2src.zip

cp $LIBDIR/cdmlib-persistence/2.2/cdmlib-persistence-2.2.jar .
cp $LIBDIR/cdmlib-persistence/2.2/cdmlib-persistence-2.2-sources.jar ./cdmlib-persistence-2.2src.zip

cp $LIBDIR/cdmlib-services/2.2/cdmlib-services-2.2.jar .
cp $LIBDIR/cdmlib-services/2.2/cdmlib-services-2.2-sources.jar ./cdmlib-services-2.2src.zip

cp $LIBDIR/cdmlib-io/2.2/cdmlib-io-2.2.jar .
cp $LIBDIR/cdmlib-io/2.2/cdmlib-io-2.2-sources.jar ./cdmlib-io-2.2src.zip

cp $LIBDIR/cdmlib-ext/2.2/cdmlib-ext-2.2.jar .
cp $LIBDIR/cdmlib-ext/2.2/cdmlib-ext-2.2-sources.jar ./cdmlib-ext-2.2src.zip