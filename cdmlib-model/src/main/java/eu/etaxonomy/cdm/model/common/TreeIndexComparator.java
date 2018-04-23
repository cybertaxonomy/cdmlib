/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.Comparator;

/**
 * @author a.mueller
 \* @since 05.07.2016
 *
 * Comparator for tree indexes.
 * Compares the tree indexes node by node, sorted by node number.
 * If one index is shorter than the other one but
 */
public class TreeIndexComparator implements Comparator<TreeIndex>{

    @Override
    public int compare(TreeIndex treeIndex1, TreeIndex treeIndex2) {
        if (treeIndex1 == null && treeIndex2 == null){
            return 0;
        }else if (treeIndex1 == null){
            return -1;
        }else if (treeIndex2 == null){
            return 1;
        }
        if (treeIndex1.equals(treeIndex2)){
            return 0;
        }

        String[] splits1 = treeIndex1.toString().split(ITreeNode.separator);
        String[] splits2 = treeIndex2.toString().split(ITreeNode.separator);


        for (int i=0; i < splits1.length; i++){
            if (splits2.length <= i){
                return 1;
            }
            int c = splits1[i].compareTo(splits2[i]);
            if (c != 0){
                return c;
            }
        }
        return -1;

    }

}
