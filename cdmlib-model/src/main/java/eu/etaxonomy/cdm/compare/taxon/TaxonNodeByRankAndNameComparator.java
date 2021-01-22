/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.taxon;

import java.io.Serializable;
import java.util.List;

import eu.etaxonomy.cdm.compare.common.OrderIndexComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author k.luther
 * @since 18.03.2010
 */
public class TaxonNodeByRankAndNameComparator extends TaxonNodeByNameComparator implements Serializable {

    private static final long serialVersionUID = 2596641007876609704L;

	@Override
    public int compare(TaxonNode node1, TaxonNode node2) {
	    boolean node1Excluded = node1.isExcluded();
        boolean node2Excluded = node2.isExcluded();
        boolean node1Unplaced = node1.isUnplaced();
        boolean node2Unplaced = node2.isUnplaced();

        if (node1.getUuid().equals(node2.getUuid())){
            return 0;
        }
        //They should both be put to the end (first unplaced then excluded)
        if (node2Excluded && !node1Excluded){
            return -1;
        }
        if (node2Unplaced && !(node1Unplaced || node1Excluded)){
            return -1;
        }

        if (node1Excluded && !node2Excluded){
            return 1;
        }
        if (node1Unplaced && !(node2Unplaced || node2Excluded)){
            return 1;
        }

        if (node1Unplaced && node2Excluded){
            return -1;
        }
        if (node2Unplaced && node1Excluded){
            return 1;
        }

        Integer rankTax1 = getRankOrder(node1);
        Integer rankTax2 = getRankOrder(node2);

        //first compare ranks, if ranks are equal (or both null) compare names or taxon title cache if names are null
        int rankOrder = OrderIndexComparator.instance().compare(rankTax1, rankTax2);

        if (rankOrder == 0) {
            List<TaggedText> taggedText1 = null;
            List<TaggedText> taggedText2 = null;
            if (node1.getTaxon() != null ){
                taggedText1 = node1.getTaxon().getName() != null? node1.getTaxon().getName().getTaggedName() : node1.getTaxon().getTaggedTitle();
            }
            if (node2.getTaxon() != null ){
                taggedText2 = node2.getTaxon().getName() != null? node2.getTaxon().getName().getTaggedName() : node2.getTaxon().getTaggedTitle();
            }
            if (taggedText1 != null && taggedText2 != null){
                //same rank, order by name
                String sortableName1 = "";
                for (TaggedText tagged: taggedText1){
                    if (tagged.getType().equals(TagEnum.name)){
                        sortableName1 += " " + tagged.getText();
                    }
                }

                String sortableName2 = "";
                for (TaggedText tagged: taggedText2){
                    if (tagged.getType().equals(TagEnum.name)){
                        sortableName2 += " " + tagged.getText();
                    }
                }
                int result = sortableName1.compareTo(sortableName2);
                if (result == 0){
                    return node1.getUuid().compareTo(node2.getUuid());
                }else{
                    return result;
                }
            }else {
                //this is maybe not 100% correct, we need to compare name cases, but it is a very rare case
                return node1.getUuid().compareTo(node2.getUuid());
            }
        }else{
            //rankTax2.isHigher(rankTax1)
            return rankOrder;
        }
    }

    private Integer getRankOrder(TaxonNode node) {
        if (node.getNullSafeRank() != null){
            return node.getNullSafeRank().getOrderIndex();
        }else{
            return null;
        }
    }

    public String getTaxonTitle(TaxonBase<?> taxon, TaxonNode node) {
        return (taxon == null) ? node.getUuid().toString(): taxon.getTitleCache();
    }
}
