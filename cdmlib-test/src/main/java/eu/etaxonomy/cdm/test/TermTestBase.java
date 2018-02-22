/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test;

import org.junit.BeforeClass;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.kohlbecker
 * @since Feb 21, 2018
 *
 */
public class TermTestBase {

    @BeforeClass
    public final static void intitializeTermsIfneeded() {
        DefaultTermInitializer defaultTermInitializer = new DefaultTermInitializer();
        if(Language.DEFAULT() == null){
            defaultTermInitializer.initialize();
        }
    }
}
