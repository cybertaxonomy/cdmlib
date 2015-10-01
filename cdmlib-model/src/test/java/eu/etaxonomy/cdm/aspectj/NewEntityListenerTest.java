// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.aspectj;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.NewEntityListener;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author cmathew
 * @date 30 Sep 2015
 *
 */
public class NewEntityListenerTest implements NewEntityListener {
    static Logger logger = Logger.getLogger(NewEntityListenerTest.class);
    private Object lastPropValue;

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.NewEntityListener#onCreate(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public void onCreate(CdmBase cdmBase) {
        logger.warn("New Entity " + cdmBase + " created");
        lastPropValue = cdmBase;
    }

    @Test
    public void testPropertyChange() {
        NonViralName.setNewEntityListener(this);
        NonViralName b = NonViralName.NewInstance(Rank.SPECIES());
        Annotation newAnnotation = Annotation.NewDefaultLanguageInstance("test");
        b.addAnnotation(newAnnotation);
        Assert.assertEquals(newAnnotation, lastPropValue);
    }

}
