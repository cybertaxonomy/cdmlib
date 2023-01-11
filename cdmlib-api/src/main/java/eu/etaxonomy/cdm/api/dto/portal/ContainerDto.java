/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Container to handle list and sets in a DTO.
 *
 * TODO make pageable
 *
 * @author a.mueller
 * @date 07.01.2023
 */
public class ContainerDto<T extends CdmBaseDto> {

    private int count;

    private boolean orderRelevant;

    private Collection<T> collection = new ArrayList<>();

    //computed from updated of all relevant data
    //uses java.time.XXX  to have less dependencies
    //TODO or should we use jodatime
    private LocalDateTime lastUpdated;

// ***************** GETTER / SETTER *********************/

    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

    public boolean isOrderRelevant() {
        return orderRelevant;
    }
    public void setOrderRelevant(boolean orderRelevant) {
        this.orderRelevant = orderRelevant;
    }

    public Collection<T> getCollection() {
        return collection;
    }
    public void addItem(T item) {
        this.collection.add(item);
        count++;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


}
