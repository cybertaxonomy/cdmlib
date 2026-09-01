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
        //same UUID

        if (o1.getTaxonNodeUuid().equals(o2.getTaxonNodeUuid())){
            return 0;
        }

        //Rank
        Rank rankName1 = Rank.UNKNOWN_RANK();
        if (o1.getRank() != null){
            rankName1 = o1.getRank();
        }
        Rank rankName2 = Rank.UNKNOWN_RANK();
        if (o2.getRank() != null){
            rankName2 = o2.getRank();
        }

        //first compare ranks, if ranks are equal (or both null) compare names or taxon title cache if names are null
        // TODO can't we use DefinedTermBase.performCompareTo here?
        if (rankName1 == null && rankName2 != null){
            return 1;
        }else if(rankName2 == null && rankName1 != null){
            return -1;
        }else if (rankName1 != null && rankName1.compareTo(rankName2)>0){
            return -1;
        }else if (rankName1 == null && rankName2 == null || rankName1.equals(rankName2)) {
            if (o1.getTaxonTitleCache() != null && o2.getTaxonTitleCache() != null){
                //same rank, order by titleCache
                int result = o1.getTaxonTitleCache().compareTo(o2.getTaxonTitleCache());
                if (result == 0){
                    return o1.getTaxonNodeUuid().compareTo(o2.getTaxonNodeUuid());
                }else{
                    return result;
                }
            }
        }else{
            return 1;
        }
        return 0;
    }
}