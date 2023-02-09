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
import java.util.UUID;

/**
 * @author a.mueller
 * @date 10.01.2023
 */
public class FeatureDto extends LabeledEntityDto {

    private List<IFactDto> facts = new ArrayList<>();

    public FeatureDto(UUID uuid, int id, String label) {
        super(uuid, id, label);
    }

// *********************** GETTER / ADDER ********************************/

    public List<IFactDto> getFacts() {
        return facts;
    }
    public void addFact(IFactDto factDto) {
        facts.add(factDto);
    }
}