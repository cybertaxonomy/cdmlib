/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto.compare;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.compare.common.OrderIndexComparator;
import eu.etaxonomy.cdm.compare.taxon.TaxonNodeStatusComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author k.luther
 * @since 18.03.2010
 */
public class TaxonNodeDtoByRankAndNameComparator
        implements Serializable, Comparator<ISortableTaxonNodeDto> {

    private static final long serialVersionUID = 2596641007876609704L;


    @Override
//	public int compare(TaxonNodeCompareDto node1, TaxonNodeCompareDto node2) {
	public int compare(ISortableTaxonNodeDto node1, ISortableTaxonNodeDto node2) {
        if (node1 == null && node2 == null) {
            return 0;
        }
        else if (node1 == null) {
            return 1;
        }
        else if (node2 == null) {
            return -1;
        }
        if (node1.equals(node2)){
            return 0;
        }

        //compare status
        int nodeResult = compareStatus(node1.getStatus(), node2.getStatus());
        if (nodeResult != 0){
            return nodeResult;
        }

		Integer rankTax1 = node1.getRankOrderIndex();
		Integer rankTax2 = node2.getRankOrderIndex();

		//first compare ranks, if ranks are equal (or both null) compare names or taxon title cache if names are null
		int rankOrder = OrderIndexComparator.instance().compare(rankTax1, rankTax2);

		if (rankOrder != 0) {
		    //rankTax2.isHigher(rankTax1)
            return rankOrder;
		} else {
	    	//same rank => order by name
		    int nameCompare = sortByName(node1, node2);
			//same name string => order by taxon uuid (TODO: we could also order by taxon titleCache but it is not available here
		    if (nameCompare == 0){
			    if (node1.getTaxonUuid() == null && node2.getTaxonUuid() == null) {
			        return 0;
			    }else if (node1.getTaxonUuid() == null) {
			        return -1;
			    }else if (node2.getTaxonUuid() == null) {
			        return 1;
			    }
				return node1.getTaxonUuid().compareTo(node2.getTaxonUuid());
			}else{
				return nameCompare;
			}
		}
	}

    private int sortByName(ISortableTaxonNodeDto node1, ISortableTaxonNodeDto node2) {

        //name1
        String sortableName1 = "";
        if(node1.getTaggedTitle() != null) {
            for (TaggedText tagged: node1.getTaggedTitle()){
                if (tagged.getType().equals(TagEnum.name)){
                    sortableName1 += " " + tagged.getText();
                }
            }
        }
        sortableName1 = StringUtils.isBlank(sortableName1)? node1.getNameTitleCache(): sortableName1;

        //name2
        String sortableName2 = "";
        if(node2.getTaggedTitle() != null) {
            for (TaggedText tagged: node2.getTaggedTitle()){
                if (tagged.getType().equals(TagEnum.name)){
                    sortableName2 += " " + tagged.getText();
                }
            }
        }
        sortableName2 = StringUtils.isBlank(sortableName2)? node1.getNameTitleCache(): sortableName2;

        //compare
        int result = sortableName1.compareTo(sortableName2);
        return result;
    }

    private int compareStatus(TaxonNodeStatus status1, TaxonNodeStatus status2) {
        return TaxonNodeStatusComparator.INSTANCE().compare(status1, status2);
    }
}