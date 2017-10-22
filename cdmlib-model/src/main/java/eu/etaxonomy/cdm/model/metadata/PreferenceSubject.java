/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @date 03.06.2016
 *
 */
public class PreferenceSubject {

    public static final String ROOT = "/";
    public static final String SEP = "/";
    public static final String VAADIN = "vaadin";

    private String subject;

    public static PreferenceSubject NewDatabaseInstance(){
        return new PreferenceSubject(ROOT);
    }

    public static PreferenceSubject NewInstance(Classification classification){
        return NewInstance(classification.getRootNode());
    }

    public static PreferenceSubject NewInstance(TaxonNode taxonNode){
        String result = ROOT + "TaxonNode[" + taxonNode.treeIndex() + "]" + SEP;
        return new PreferenceSubject(result);
    }

    public static PreferenceSubject NewVaadinInstance(){
        return new PreferenceSubject(ROOT +  VAADIN + SEP);
    }


// *****************************************************/

    private PreferenceSubject(String subject){
        this.subject = subject;
    }


    @Override
    public String toString() {
        return subject;
    }
}
