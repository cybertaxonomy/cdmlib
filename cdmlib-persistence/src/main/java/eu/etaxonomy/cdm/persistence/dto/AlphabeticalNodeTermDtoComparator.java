/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author k.luther
 * @since Mar 1, 2021
 */
public class AlphabeticalNodeTermDtoComparator implements Serializable, Comparator<TermNodeDto> {


    private static final long serialVersionUID = 7161955220917404023L;

    @Override
    public int compare(TermNodeDto o1, TermNodeDto o2) {
        if (o1 == o2 ){
            return 0;
        }
        if (o1.getTerm() == o2.getTerm()){
            return 0;
        }

        if (o1 == null){
            return -1;
        }
        if (o2 == null){
            return 1;
        }

        if (o1.getTerm() == null){
            return -1;
        }
        if (o2.getTerm() == null){
            return 1;
        }

        if (o1.getTerm() != null && o1.getTerm().getTitleCache() == null && o2.getTerm() != null && o2.getTerm().getTitleCache() != null){
            return -1;
        }
        if (o2.getTerm().getTitleCache() == null && o1.getTerm().getTitleCache() != null){
            return 1;
        }
        return o1.getTerm().getTitleCache().compareTo(o2.getTerm().getTitleCache());

    }

}
