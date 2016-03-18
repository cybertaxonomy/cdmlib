/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.util.Comparator;
import java.util.Iterator;

/**
 * Comparator to compare {@link TaxonNode taxon nodes} by its user defined ordering
 * @author k.luther
 */
public class TaxonNaturalComparator implements Comparator<TaxonNode> {

    public TaxonNaturalComparator(){
		super();

	}

	@Override
	public int compare(TaxonNode node1, TaxonNode node2) {
//		TaxonNode node1 = null;
//		TaxonNode node2 = null;
//		if (taxon1.getTaxonNodes().size()>0 && taxon2.getTaxonNodes().size()>0){
//			for (TaxonNode node: taxon1.getTaxonNodes()){
//				if (node.getClassification().equals(classification)){
//					node1= node;
//					break;
//				}
//			}
//			for (TaxonNode node: taxon2.getTaxonNodes()){
//				if (node.getClassification().equals(classification)){
//					node2= node;
//					break;
//				}
//			}
//		}else{
//			return 0;
//		}
		if (node1.isAncestor(node2)) {
            return 1;
        }
		if (node2.isAncestor(node1)) {
            return -1;
        }
		if (node1.equals(node2)) {
            return 0;
        }

		String[] splitNode1 = node1.treeIndex().split("#");
		String[] splitNode2 = node2.treeIndex().split("#");
		if (splitNode1.length < splitNode2.length) {
            return 1;
        } else if (splitNode2.length < splitNode1.length) {
            return -1;
        }

		if (node1.getParent().equals(node2.getParent())){
			return node1.getSortIndex().compareTo(node2.getSortIndex());
		}
		String lastEqualAncestorTreeIndex = null;

		Iterator<TaxonNode> ancestorsNode1 = node1.getAncestors().iterator();
		Iterator<TaxonNode> ancestorsNode2 = node2.getAncestors().iterator();
		for (int i = 0; i < splitNode1.length; i++){
			if (!splitNode1[i].equals(splitNode2[i])){
				// take the last equal ancestor and compare the sortindex
				if (lastEqualAncestorTreeIndex != null){
					TaxonNode lastEqualTreeIndexAncestorNode1 = null;
					TaxonNode lastEqualTreeIndexAncestorNode2 = null;
					while (ancestorsNode1.hasNext()){
						TaxonNode next1 = ancestorsNode1.next();
						if (next1.treeIndex().equals(lastEqualAncestorTreeIndex)){
							lastEqualTreeIndexAncestorNode1 = next1;
						}
					}
					while (ancestorsNode2.hasNext()){
						TaxonNode next2 = ancestorsNode2.next();
						if (next2.treeIndex().equals(lastEqualAncestorTreeIndex)){
							lastEqualTreeIndexAncestorNode2 = next2;
						}
					}
					return lastEqualTreeIndexAncestorNode1.getSortIndex().compareTo(lastEqualTreeIndexAncestorNode2.getSortIndex());

				}
			}
			lastEqualAncestorTreeIndex = splitNode1[i];
		}

		return 0;
	}

}
