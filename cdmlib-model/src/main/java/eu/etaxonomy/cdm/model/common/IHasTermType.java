/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

/**
 * @author a.mueller
 * @since 22.01.2019
 *
 */
public interface IHasTermType {

    public TermType getTermType();

    public static void checkTermTypeNull(IHasTermType term) {
        if (term.getTermType()== null){
            throw new IllegalArgumentException("Term types must not be null");
        }
    }
    public static void checkTermTypes(IHasTermType term, IHasTermType term2) {
        if (term != null && term2 != null){
            checkTermTypeNull(term);
            checkTermTypeNull(term2);
            if (term.getTermType()!= term.getTermType()){
                throw new IllegalArgumentException("Term types must match");
            }
        }
    }
}
