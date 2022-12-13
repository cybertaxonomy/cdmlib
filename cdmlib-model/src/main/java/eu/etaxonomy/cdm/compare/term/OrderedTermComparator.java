/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.term;

import java.util.Comparator;

import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * @author k.luther
 * @since 07.02.2018
 */
public class OrderedTermComparator <T extends DefinedTermBase<T>> implements Comparator<T>{

    @Override
    public int compare(T o1, T o2) {
        if (o1.isOrderRelevant() && o2.isOrderRelevant()){
            if (o1.getVocabulary().equals(o2.getVocabulary())){
                return - o1.compareTo(o2);
            }else{
                return o1.getVocabulary().getTitleCache().compareTo(o2.getVocabulary().getTitleCache());
            }
        }else{
            throw new IllegalStateException("One of the compared terms are not 'order relevant'");
        }
    }
}