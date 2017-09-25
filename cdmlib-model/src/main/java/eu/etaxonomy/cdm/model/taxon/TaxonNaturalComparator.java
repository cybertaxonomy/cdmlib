/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Comparator to compare {@link TaxonNode taxon nodes} by its user defined ordering
 *
 * @author k.luther
 */
public class TaxonNaturalComparator implements Comparator<TaxonNode> {

    public TaxonNaturalComparator(){
		super();

	}

	@SuppressWarnings("null")
    @Override
	public int compare(TaxonNode node1, TaxonNode node2) {
	   // System.out.println("compare node 1: "+ node1.getTaxon().getTitleCache() + " - node 2: " + node2.getTaxon().getTitleCache());
	    if (node1.equals(node2)) {
	        return 0;
        }
	    // if we do not check for null for the treeIndex we always return 1 if one of the nodes have no treeIndex
	    if (node1.treeIndex() == null){
	        return 1;
	    }
	    if (node2.treeIndex() == null){
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
		List<TaxonNode> ancestorAndNode= new ArrayList<>();
		ancestorAndNode.add(node1);
		ancestorAndNode.addAll(node1.getAncestors());
		java.util.Collections.sort(ancestorAndNode, new TreeIndexComparator());


		List<TaxonNode> ancestorAndNode2= new ArrayList<>();
        ancestorAndNode2.add(node2);
        ancestorAndNode2.addAll(node2.getAncestors());
        java.util.Collections.sort(ancestorAndNode2, new TreeIndexComparator());

		for (int i = 0; i < splitNode1.length; i++){
			if (!splitNode1[i].equals(splitNode2[i])){
				// take the last equal ancestor and compare the sortindex
				if (lastEqualAncestorTreeIndex != null){
					TaxonNode lastEqualTreeIndexAncestorNode1 = null;
					TaxonNode lastEqualTreeIndexAncestorNode2 = null;
					for (TaxonNode next1 :ancestorAndNode){

						if (next1.treeIndex().equals(lastEqualAncestorTreeIndex+"#"+splitNode1[i]+ "#") ){
						    lastEqualTreeIndexAncestorNode1 = next1;
						}
					}
					for (TaxonNode next2 :ancestorAndNode2){

						if (next2.treeIndex().equals(lastEqualAncestorTreeIndex+"#"+splitNode2[i]+ "#")){
						    lastEqualTreeIndexAncestorNode2 = next2;
						}
					}
					return lastEqualTreeIndexAncestorNode1.getSortIndex().compareTo(lastEqualTreeIndexAncestorNode2.getSortIndex());
				}
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
