/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.UUID;

/**
 * A DTO holding the term information as well as all the facts
 * of this feature type. So it is not a pure term DTO representing
 * only the term.
 *
 * @author a.mueller
 * @date 10.01.2023
 */
public class FeatureDto extends LabeledEntityDto {

    private ContainerDto<IFactDto> facts;

    private ContainerDto<FeatureDto> subFeatures;

    public FeatureDto(UUID uuid, int id, String label) {
        super(uuid, id, label);
    }

// *********************** GETTER / ADDER ********************************/

    public ContainerDto<IFactDto> getFacts() {
        return facts;
    }
    public void addFact(IFactDto factDto) {
        if (facts == null) {
            facts = new ContainerDto<>();
        }
        facts.addItem(factDto);
    }

    public ContainerDto<FeatureDto> getSubFeatures() {
        return subFeatures;
    }
    public void setSubFeatures(ContainerDto<FeatureDto> subFeatures) {
        this.subFeatures = subFeatures;
    }
}