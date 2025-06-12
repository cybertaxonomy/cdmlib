/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.Comparator;

/**
 * @author k.luther
 * @since 07.02.2018
 */
public class OrderedTermDtoComparator implements Comparator<TermDto>{

    @Override
    public int compare(TermDto o1, TermDto o2) {
        if (o1.getOrderIndex() != null && o2.getOrderIndex() != null){
            if (o1.getVocabularyUuid().equals(o2.getVocabularyUuid())){
                if (o1.getOrderIndex() > o2.getOrderIndex()) {
                    return -1;
                }
                if (o1.getOrderIndex()< o2.getOrderIndex()) {
                    return 1;
                }
                return 0;
            }else{
                return o1.getVocabularyDto().getLabel().compareTo(o2.getVocabularyDto().getLabel());
            }
        }else{
            throw new IllegalStateException("One of the compared terms are not 'order relevant'");
        }
    }
}