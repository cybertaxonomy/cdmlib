/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.Comparator;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * @author k.luther
 * @since 08.06.2016
 */
public class TermDtoIdInVocabularyComparator implements Comparator<TermDto> {

    @Override
    public int compare(TermDto term1, TermDto term2) {
        String label1;
        String label2;
        if (term1.getUuid().equals(term2.getUuid())){
            return 0;
        }

        if (term1.getIdInVocabulary() == null){
            label1 = CdmUtils.Nz(term1.getTitleCache());
        }else{
            label1 = CdmUtils.Nz(term1.getIdInVocabulary());
        }
        if (term2.getIdInVocabulary() == null){
            label2 = CdmUtils.Nz(term2.getTitleCache());
        }else{
            label2 = CdmUtils.Nz(term2.getIdInVocabulary());
        }

        return  label1.compareTo(label2);
    }
}
