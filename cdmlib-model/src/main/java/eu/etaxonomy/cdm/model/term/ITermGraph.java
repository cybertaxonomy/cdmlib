/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.util.Set;

/**
 * @author a.mueller
 * @since 11.03.2019
 *
 */
public interface ITermGraph<TERM extends DefinedTermBase, REL extends TermRelationBase> {

    /**
     * @return
     */
    Set<REL> getTermRelations();

    //not yet pulic
//    /**
//     * @param termRelations
//     */
//    void setTermRelations(Set<REL> termRelations);

}
