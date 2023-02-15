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
import java.util.List;

import eu.etaxonomy.cdm.format.common.TypedLabel;

/**
 * @author a.mueller
 * @date 11.01.2023
 */
public class FactDto extends FactDtoBase {

    private List<TypedLabel> typedLabel = new ArrayList<>();

    private LocalDateTime lastUpdated;

// ****************** GETTER / SETTER ***********************/



    public List<TypedLabel> getTypedLabel() {
        return typedLabel;
    }
    public void addTypedLabel(TypedLabel typedLabel) {
        this.typedLabel.add(typedLabel);
    }


    @Override
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    @Override
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}