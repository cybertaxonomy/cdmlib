/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;

/**
 * @author a.kohlbecker
 \* @since Jul 31, 2014
 *
 */
public class CdmTypeScannerTest {

    @Test
    public void testCdmModelTypes() throws ClassNotFoundException {

        boolean includeAbstract = true;
        boolean includeInterfaces = false;

        CdmTypeScanner scanner = new CdmTypeScanner<CdmBase>(includeAbstract, includeInterfaces);

        scanner.addIncludeFilter(new CdmAssignableTypeFilter(IdentifiableEntity.class, includeAbstract, includeInterfaces));

        Collection<Class<? extends CdmBase>> classes = scanner.scanTypesIn("eu/etaxonomy/cdm/model");
        assertTrue(classes.contains(TaxonDescription.class));
        assertTrue("abstract base classes are missing", classes.contains(DescriptionBase.class));
    }
}
