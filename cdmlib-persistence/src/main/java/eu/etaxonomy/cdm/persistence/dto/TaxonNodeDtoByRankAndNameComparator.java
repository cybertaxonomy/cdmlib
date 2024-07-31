/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;
import java.util.Comparator;

import eu.etaxonomy.cdm.compare.common.OrderIndexComparator;
import eu.etaxonomy.cdm.compare.taxon.TaxonNodeStatusComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author k.luther
 * @since 18.03.2010
 */
public class TaxonNodeDtoByRankAndNameComparator
        implements Serializable, Comparator<TaxonNodeDto> {

    private static final long serialVersionUID = 2596641007876609704L;

	@Override
    public int compare(TaxonNodeDto node1, TaxonNodeDto node2) {
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

		if (rankOrder == 0) {
			if (node1.getTaggedTitle() != null && node2.getTaggedTitle() != null){
				//same rank, order by name
			    String sortableName1 = "";
			    for (TaggedText tagged: node1.getTaggedTitle()){
			        if (tagged.getType().equals(TagEnum.name)){
			            sortableName1 += " " + tagged.getText();
			        }
			    }

			    String sortableName2 = "";
                for (TaggedText tagged: node2.getTaggedTitle()){
                    if (tagged.getType().equals(TagEnum.name)){
                        sortableName2 += " " + tagged.getText();
                    }
                }
				int result = sortableName1.compareTo(sortableName2);
				if (result == 0){
				    if (node1.getTaxonUuid() == null && node2.getTaxonUuid() == null) {
				        return 0;
				    }else if (node1.getTaxonUuid() == null) {
				        return -1;
				    }else if (node2.getTaxonUuid() == null) {
				        return 1;
				    }
					return node1.getTaxonUuid().compareTo(node2.getTaxonUuid());
				}else{
					return result;
				}
			}else {
				//this is maybe not 100% correct, we need to compare name cases, but it is a very rare case, in this case the AbbrevTitleCache cont
				return node1.getNameTitleCache().compareTo(node2.getNameTitleCache());
			}
		}else{
			//rankTax2.isHigher(rankTax1)
			return rankOrder;
		}
	}

    private int compareStatus(TaxonNodeStatus status1, TaxonNodeStatus status2) {
        return TaxonNodeStatusComparator.INSTANCE().compare(status1, status2);
    }

    public String getTaxonTitle(TaxonBase<?> taxon, TaxonNode node) {
        return (taxon == null) ? node.getUuid().toString(): taxon.getTitleCache();
    }
}