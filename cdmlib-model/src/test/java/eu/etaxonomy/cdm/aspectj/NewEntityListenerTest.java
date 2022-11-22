/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.aspectj;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.NewEntityListener;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;

/**
 * @author cmathew
 * @since 30 Sep 2015
 */
public class NewEntityListenerTest
        implements NewEntityListener {

    private static final Logger logger = LogManager.getLogger();
    private Object lastPropValue;

    @Override
    public void onCreate(CdmBase cdmBase) {
        logger.info("New Entity " + cdmBase + " created");
        lastPropValue = cdmBase;
    }

    @Test
    public void testPropertyChange() {
        CdmBase.setNewEntityListener(this);

        INonViralName nvn = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        Annotation newAnnotation = Annotation.NewDefaultLanguageInstance("test");
        nvn.addAnnotation(newAnnotation);
        Assert.assertEquals(newAnnotation, lastPropValue);
    }
}