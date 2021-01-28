/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.common;

import java.util.Comparator;

/**
 * @author k.luther
 * @since 07.12.2018
 */
public class OrderIndexComparator implements Comparator<Integer> {

    private static OrderIndexComparator instance;

    public static final OrderIndexComparator instance(){
        if(instance == null){
            instance = new OrderIndexComparator();
        }
        return instance;
    }

    @Override
    public int compare(Integer orderIndex1, Integer orderIndex2) {
       if (orderIndex1 == orderIndex2){
           return 0;
       }
       if (orderIndex1 == null){
           return 1;
       }
       if (orderIndex2 == null){
           return -1;
       }
       if (orderIndex1<orderIndex2){
           return -1;
       }else{
           return 1;
       }
    }
}