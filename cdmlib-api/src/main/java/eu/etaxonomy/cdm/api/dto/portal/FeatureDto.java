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

/**
 * @author a.mueller
 * @date 10.01.2023
 */
public class FeatureDto extends LabeledEntityDto {

    private List<FactDto> facts = new ArrayList<>();

    public List<FactDto> getFacts() {
        return facts;
    }
    public void addFact(FactDto factDto) {
        facts.add(factDto);
    }
}