/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto.compare;

import java.io.Serializable;
import java.util.Comparator;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeQueryResult;

/**
 * @author k.luther
 * @since 06.12.2017
 */
public class SortableTaxonNodeQueryResultComparator
        implements Serializable, Comparator<SortableTaxonNodeQueryResult>{

    private static final long serialVersionUID = 6000794425983318091L;

    @Override
    public int compare(SortableTaxonNodeQueryResult o1, SortableTaxonNodeQueryResult o2) {

        //same taxon node
        if (o1.getTaxonNodeUuid().equals(o2.getTaxonNodeUuid())){
            return 0;
        }

        //Rank
        Rank rank1 = o1.getRank() != null? o1.getRank(): Rank.UNKNOWN_RANK();
        Rank rank2 = o2.getRank() != null? o2.getRank(): Rank.UNKNOWN_RANK();

        //first compare ranks, if ranks are equal compare names or taxon title cache if names are null
        // TODO can't we use DefinedTermBase.performCompareTo here?
        if (rank2.compareTo(rank1) != 0){
            return rank2.compareTo(rank1);
        }else {
            String titleCache1 = CdmUtils.Nz(o1.getTaxonTitleCache());
            String titleCache2 = CdmUtils.Nz(o2.getTaxonTitleCache());
            //same rank, order by titleCache
            int result = titleCache1.compareTo(titleCache2);
            if (result == 0){
                return o1.getTaxonNodeUuid().compareTo(o2.getTaxonNodeUuid());
            }else{
                return result;
            }
        }
    }
}