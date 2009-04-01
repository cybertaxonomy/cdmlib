#! /bin/sh

rm ./cdmlib-commons-2.1src.zip ./cdmlib-commons-2.1.jar ./cdmlib-model-2.1src.zip \
 ./cdmlib-model-2.1.jar ./cdmlib-persistence-2.1src.zip ./cdmlib-persistence-2.1.jar \
 ./cdmlib-services-2.1src.zip ./cdmlib-persistence-2.1-tests.jar ./cdmlib-services-2.1.jar \
 ./cdmlib-io-2.1src.zip ./cdmlib-io-2.1.jar ./cdmlib-ext-2.1src.zip ./cdmlib-ext-2.1.jar > /dev/null 2>&1 


LIBDIR=~/.m2/repository/eu/etaxonomy

cp $LIBDIR/cdmlib-commons/2.1/cdmlib-commons-2.1.jar .
cp $LIBDIR/cdmlib-commons/2.1/cdmlib-commons-2.1-sources.jar ./cdmlib-commons-2.1src.zip

cp $LIBDIR/cdmlib-model/2.1/cdmlib-model-2.1.jar .
cp $LIBDIR/cdmlib-model/2.1/cdmlib-model-2.1-sources.jar ./cdmlib-model-2.1src.zip

cp $LIBDIR/cdmlib-persistence/2.1/cdmlib-persistence-2.1.jar .
cp $LIBDIR/cdmlib-persistence/2.1/cdmlib-persistence-2.1-tests.jar .
cp $LIBDIR/cdmlib-persistence/2.1/cdmlib-persistence-2.1-sources.jar ./cdmlib-persistence-2.1src.zip

cp $LIBDIR/cdmlib-services/2.1/cdmlib-services-2.1.jar .
cp $LIBDIR/cdmlib-services/2.1/cdmlib-services-2.1-sources.jar ./cdmlib-services-2.1src.zip

cp $LIBDIR/cdmlib-io/2.1/cdmlib-io-2.1.jar .
cp $LIBDIR/cdmlib-io/2.1/cdmlib-io-2.1-sources.jar ./cdmlib-io-2.1src.zip

cp $LIBDIR/cdmlib-ext/2.1/cdmlib-ext-2.1.jar .
cp $LIBDIR/cdmlib-ext/2.1/cdmlib-ext-2.1-sources.jar ./cdmlib-ext-2.1src.zip