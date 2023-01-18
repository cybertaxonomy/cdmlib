/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.format.common.TypedLabel;

/**
 * @author a.mueller
 * @date 11.01.2023
 */
public class FactDto {

    private List<TypedLabel> typedLabel = new ArrayList<>();



    public List<TypedLabel> getTypedLabel() {
        return typedLabel;
    }
    public void setTypedLabel(List<TypedLabel> typedLabel) {
        this.typedLabel = typedLabel;
    }
}