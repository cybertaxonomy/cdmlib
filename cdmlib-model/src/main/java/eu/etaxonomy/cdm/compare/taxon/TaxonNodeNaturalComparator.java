/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.taxon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * Comparator to compare {@link TaxonNode taxon nodes} by its user defined ordering
 *
 * @author k.luther
 */
public class TaxonNodeNaturalComparator implements Comparator<TaxonNode> {

	@Override
	public int compare(TaxonNode node1, TaxonNode node2) {
	    if (node1.equals(node2)) {
	        return 0;
        }
	    if (node1.treeIndex() == null){
	        if (node2.treeIndex() == null){
	            return 0;
	        }else{
	            return 1;
	        }
	    }else if (node2.treeIndex() == null){
            return -1;
        }

	    if (node1.isAncestor(node2)) {
            return -1;
        }
		if (node2.isAncestor(node1)) {
            return 1;
        }

		String[] splitNode1 = node1.treeIndex().split("#");
		String[] splitNode2 = node2.treeIndex().split("#");

		if (node1.getParent().equals(node2.getParent())){
			return node1.getSortIndex().compareTo(node2.getSortIndex());
		}
		String lastEqualAncestorTreeIndex = "";

		List<TaxonNode> ancestorAndNode1= new ArrayList<>();
		ancestorAndNode1.add(node1);
		ancestorAndNode1.addAll(node1.getAncestorList());
		Collections.sort(ancestorAndNode1, new TreeIndexComparator());

		List<TaxonNode> ancestorAndNode2= new ArrayList<>();
        ancestorAndNode2.add(node2);
        ancestorAndNode2.addAll(node2.getAncestorList());
        Collections.sort(ancestorAndNode2, new TreeIndexComparator());

		for (int i = 0; i < splitNode1.length; i++){
			if (!splitNode1[i].equals(splitNode2[i])){
			    // take the last equal ancestor and compare the sortindex
				TaxonNode lastEqualTreeIndexAncestorNode1 = null;
				TaxonNode lastEqualTreeIndexAncestorNode2 = null;
				for (TaxonNode next1 :ancestorAndNode1){
					if (next1.treeIndex().equals(lastEqualAncestorTreeIndex+"#"+splitNode1[i]+ "#") ){
					    lastEqualTreeIndexAncestorNode1 = next1;
					}
				}
				for (TaxonNode next2 :ancestorAndNode2){
					if (next2.treeIndex().equals(lastEqualAncestorTreeIndex+"#"+splitNode2[i]+ "#")){
					    lastEqualTreeIndexAncestorNode2 = next2;
					}
				}
				if (lastEqualTreeIndexAncestorNode1 != null && lastEqualTreeIndexAncestorNode2 != null) {
				    return lastEqualTreeIndexAncestorNode1.getSortIndex().compareTo(lastEqualTreeIndexAncestorNode2.getSortIndex());
				} //TODO do we need "else" here?
			}
			if (!splitNode1[i].equals("")){
			    lastEqualAncestorTreeIndex = lastEqualAncestorTreeIndex+"#"+splitNode1[i];
			}
		}
		return 0;
	}

	private final class TreeIndexComparator implements Comparator<TaxonNode> {
	    @Override
	    public int compare(TaxonNode node1,TaxonNode node2){
	        return node1.treeIndex().compareTo(node2.treeIndex());
	    }
	}
}
