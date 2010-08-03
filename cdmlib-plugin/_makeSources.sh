#! /bin/sh

rm ./cdmlib-commons-2.4src.zip ./cdmlib-commons-2.4.jar ./cdmlib-model-2.4src.zip \
 ./cdmlib-model-2.4.jar ./cdmlib-persistence-2.4src.zip ./cdmlib-persistence-2.4.jar \
 ./cdmlib-services-2.4src.zip ./cdmlib-services-2.4.jar \
 ./cdmlib-io-2.4src.zip ./cdmlib-io-2.4.jar \
 ./cdmlib-remote-2.4src.zip ./cdmlib-remote-2.4.jar ./cdmlib-ext-2.4src.zip ./cdmlib-ext-2.4.jar > /dev/null 2>&1 


LIBDIR=~/.m2/repository/eu/etaxonomy

cp $LIBDIR/cdmlib-commons/2.4/cdmlib-commons-2.4.jar .
cp $LIBDIR/cdmlib-commons/2.4/cdmlib-commons-2.4-sources.jar ./cdmlib-commons-2.4src.zip

cp $LIBDIR/cdmlib-model/2.4/cdmlib-model-2.4.jar .
cp $LIBDIR/cdmlib-model/2.4/cdmlib-model-2.4-sources.jar ./cdmlib-model-2.4src.zip

cp $LIBDIR/cdmlib-persistence/2.4/cdmlib-persistence-2.4.jar .
cp $LIBDIR/cdmlib-persistence/2.4/cdmlib-persistence-2.4-sources.jar ./cdmlib-persistence-2.4src.zip

cp $LIBDIR/cdmlib-services/2.4/cdmlib-services-2.4.jar .
cp $LIBDIR/cdmlib-services/2.4/cdmlib-services-2.4-sources.jar ./cdmlib-services-2.4src.zip

cp $LIBDIR/cdmlib-io/2.4/cdmlib-io-2.4.jar .
cp $LIBDIR/cdmlib-io/2.4/cdmlib-io-2.4-sources.jar ./cdmlib-io-2.4src.zip

cp $LIBDIR/cdmlib-remote/2.4/cdmlib-remote-2.4.jar .
cp $LIBDIR/cdmlib-remote/2.4/cdmlib-remote-2.4-sources.jar ./cdmlib-remote-2.4src.zip

cp $LIBDIR/cdmlib-ext/2.4/cdmlib-ext-2.4.jar .
cp $LIBDIR/cdmlib-ext/2.4/cdmlib-ext-2.4-sources.jar ./cdmlib-ext-2.4src.zip
