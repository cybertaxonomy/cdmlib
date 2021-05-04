/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.term;

import java.util.Comparator;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * @author k.luther
 * @since May 4, 2021
 */
public class TermSymbol1Comparator<T extends DefinedTermBase> implements Comparator<T> {

    @Override
    public int compare(T term1, T term2) {
        String label1;
        String label2;
        if (term1.getUuid().equals(term2.getUuid())){
            return 0;
        }

        if (term1.getSymbol() == null){
            label1 = CdmUtils.Nz(term1.getTitleCache());
        }else{
            label1 = CdmUtils.Nz(term1.getSymbol());
        }
        if (term2.getSymbol() == null){
            label2 = CdmUtils.Nz(term2.getTitleCache());
        }else{
            label2 = CdmUtils.Nz(term2.getSymbol());
        }

        return  label1.compareTo(label2);
    }

}
