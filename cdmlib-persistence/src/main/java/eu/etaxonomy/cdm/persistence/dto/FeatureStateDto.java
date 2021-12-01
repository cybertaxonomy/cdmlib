/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.description.FeatureState;

/**
 * @author k.luther
 * @since 29.11.2021
 */
public class FeatureStateDto extends UuidAndTitleCache<FeatureState>{

    private static final long serialVersionUID = -240581482613107317L;

    private FeatureDto feature;
    private TermDto state;

    /**
     * @param uuid
     * @param titleCache
     */
    public FeatureStateDto(UUID uuid, FeatureDto feature, TermDto state) {
        super(uuid, "");
        this.feature = feature;
        this.state = state;
    }
    /**
     * @return the feature
     */
    public FeatureDto getFeature() {
        return feature;
    }

    /**
     * @param feature the feature to set
     */
    public void setFeature(FeatureDto feature) {
        this.feature = feature;
    }

    /**
     * @return the state
     */
    public TermDto getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(TermDto state) {
        this.state = state;
    }




}
