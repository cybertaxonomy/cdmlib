/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLight;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author k.luther
 * @since 21.05.2019
 */
public class OrderHelper {

    int orderIndex;
    UUID taxonUuid;
    List<OrderHelper> children;

    public OrderHelper(UUID uuid){

        this.taxonUuid = uuid;
    }

    public int getOrderIndex() {
        return orderIndex;
    }
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public UUID getTaxonUuid() {
        return taxonUuid;
    }

    public List<OrderHelper> getChildren() {
        return children;
    }
    public void setChildren(List<OrderHelper> children) {
        this.children = children;
    }

    public void addChild(OrderHelper child) {
        if (children == null){
            children = new ArrayList<>();
        }
        this.children.add(child);
    }

    public void addChildren(List<OrderHelper> children) {
        if (children == null){
            return;
        }
        if (this.children == null){
           this.children = new ArrayList<>();
        }
        for (OrderHelper child: children){
            this.children.add(child);
        }
    }
}