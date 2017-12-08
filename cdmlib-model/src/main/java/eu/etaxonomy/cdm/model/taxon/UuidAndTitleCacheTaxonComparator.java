/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.io.Serializable;
import java.util.Comparator;
import java.util.UUID;

import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author k.luther
 * @date 06.12.2017
 *
 */
public class UuidAndTitleCacheTaxonComparator implements Serializable, Comparator<Object[]>{


    private static final long serialVersionUID = 6000794425983318091L;


    @Override
    public int compare(Object[] o1, Object[] o2) {
        //same UUID

        if (o1[0].equals(o2[0])){
            return 0;
        }

        //Rank
        Rank rankTax1 = (Rank)o1[3];
        Rank rankTax2 = (Rank)o2[3];

        String titleCache1 = (String)o1[2];
        String titleCache2 = (String)o2[2];
        //first compare ranks, if ranks are equal (or both null) compare names or taxon title cache if names are null
        if (rankTax1 == null && rankTax2 != null){
            return 1;
        }else if(rankTax2 == null && rankTax1 != null){
            return -1;
        }else if (rankTax1 != null && rankTax1.isHigher(rankTax2)){
            return -1;
        }else if (rankTax1 == null && rankTax2 == null || rankTax1.equals(rankTax2)) {
            if (titleCache1 != null && titleCache2 != null){
                //same rank, order by titleCache
                int result = titleCache1.compareTo(titleCache2);
                if (result == 0){
                    return ((UUID)o1[0]).compareTo((UUID)o2[0]);
                }else{
                    return result;
                }
            }
        }else{
            //rankTax2.isHigher(rankTax1)
            return 1;
        }
        return 0;
        }

}
