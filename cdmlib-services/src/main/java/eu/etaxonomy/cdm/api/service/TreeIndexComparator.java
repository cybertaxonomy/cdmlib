// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.Comparator;

import eu.etaxonomy.cdm.model.common.ITreeNode;

/**
 * @author a.mueller
 * @date 05.07.2016
 *
 */
public class TreeIndexComparator implements Comparator<String>{

    @Override
    public int compare(String treeIndex1, String treeIndex2) {
        if (treeIndex1 == null && treeIndex2 == null){
            return 0;
        }else if (treeIndex1 == null){
            return -1;
        }else if (treeIndex2 == null){
            return 1;
        }
        String[] splits1 = treeIndex1.split(ITreeNode.separator);
        String[] splits2 = treeIndex2.split(ITreeNode.separator);


        for (int i=0; i<splits1.length; i++){
            if (splits2.length < i){
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
