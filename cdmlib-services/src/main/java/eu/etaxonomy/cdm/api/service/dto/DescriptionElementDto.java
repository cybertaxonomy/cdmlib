/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.UUID;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.persistence.dto.FeatureDto;

/**
 * @author k.luther
 * @since Aug 18, 2021
 */
public class DescriptionElementDto implements Serializable {

    private static final long serialVersionUID = -1841517205424631763L;

    private FeatureDto featureDto;
    private UUID elementUuid;

    public DescriptionElementDto(FeatureDto feature){
        this.setFeatureDto(feature);
    }

    public DescriptionElementDto(Feature feature){
        this.setFeatureDto(feature);
    }

    public DescriptionElementDto(UUID elementUuid, FeatureDto feature){

        this.setFeatureDto(feature);
        setElementUuid(elementUuid);
    }

    public FeatureDto getFeatureDto() {
        return featureDto;
    }

    public UUID getFeatureUuid() {
        return featureDto.getUuid();
    }

    public void setFeatureDto(Feature feature) {
        feature = HibernateProxyHelper.deproxy(feature, Feature.class);
        this.featureDto = FeatureDto.fromFeature(feature);
    }

    public void setFeatureDto(FeatureDto feature) {
        this.featureDto = feature;
    }

    public UUID getElementUuid() {
        return elementUuid;
    }

    public void setElementUuid(UUID elementUuid) {
        this.elementUuid = elementUuid;
    }


}
