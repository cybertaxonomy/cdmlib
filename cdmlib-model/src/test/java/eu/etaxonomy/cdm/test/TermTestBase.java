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

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;

/**
 * This is a copy of the same class in cdmlib-test.
 * Needed here and there because cdmlib-test is not available here
 * and cdmlib-model/test is not available elsewhere.
 *
 * @author a.kohlbecker
 * @since Feb 21, 2018
 */
public abstract class TermTestBase {

    @BeforeClass
    public final static void intitializeTermsIfNeeded() {
        if(Language.DEFAULT() == null){
            new DefaultTermInitializer().initialize();
        }
    }
}