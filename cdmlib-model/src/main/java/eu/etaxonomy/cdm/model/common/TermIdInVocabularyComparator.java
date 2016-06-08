// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.Comparator;

/**
 * @author k.luther
 * @date 08.06.2016
 *
 */
public class TermIdInVocabularyComparator<T extends DefinedTermBase<?>> implements Comparator<T> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(T term1, T term2) {
        String label1 = term1.getIdInVocabulary();
        String label2 = term2.getIdInVocabulary();
        return label1.compareTo(label2);
    }



}
