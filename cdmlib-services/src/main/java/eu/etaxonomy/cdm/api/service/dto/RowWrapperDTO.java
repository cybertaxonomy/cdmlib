// $Id$
/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author pplitzner
 * @date 16.04.2018
 *
 */
public class RowWrapperDTO implements Serializable {

    private static final long serialVersionUID = -7817164423660563673L;

    private DescriptionBase description;

    private SpecimenOrObservationBase specimen;
    private TaxonNode taxonNode;
    private FieldUnit fieldUnit;
    private String identifier;
    private NamedArea country;
    private Map<Feature, DescriptionElementBase> featureToElementMap;

    public RowWrapperDTO(DescriptionBase description, TaxonNode taxonNode, FieldUnit fieldUnit, String identifier,
            NamedArea country) {
        this.taxonNode = taxonNode;
        this.fieldUnit = fieldUnit;
        this.identifier = identifier;
        this.country = country;
        this.featureToElementMap = new HashMap<>();
        if(description!=null){
            setDescription(description);
        }
    }

    public QuantitativeData addQuantitativeData(Feature feature){
        QuantitativeData data = QuantitativeData.NewInstance(feature);
        description.addElement(data);
        featureToElementMap.put(feature, data);
        return data;
    }

    public CategoricalData addCategoricalData(Feature feature){
        CategoricalData data = CategoricalData.NewInstance(feature);
        description.addElement(data);
        featureToElementMap.put(feature, data);
        return data;
    }

    public DescriptionBase getSpecimenDescription() {
        return description;
    }

    public SpecimenOrObservationBase getSpecimen() {
        return specimen;
    }

    public TaxonNode getTaxonNode() {
        return taxonNode;
    }

    public FieldUnit getFieldUnit() {
        return fieldUnit;
    }

    public String getIdentifier() {
        return identifier;
    }

    public NamedArea getCountry() {
        return country;
    }

    public void setDescription(DescriptionBase description) {
        this.description = description;
        this.specimen = description.getDescribedSpecimenOrObservation();
        Set<DescriptionElementBase> elements = description.getElements();
        for (DescriptionElementBase descriptionElementBase : elements) {
            Feature feature = descriptionElementBase.getFeature();
            featureToElementMap.put(feature, descriptionElementBase);
        }
    }

    public Object getDataValueForFeature(Feature feature){
        DescriptionElementBase descriptionElementBase = featureToElementMap.get(feature);
        return descriptionElementBase;
    }

    public void setDataValueForFeature(Feature feature, Object newValue){
        /* Only CategoricalData is handled here because for QuantitativeData the value
         * is set in the ModifyListener of the swt.Text in the CellEditor
         * for each StatisticalMeasure. So no need to set it again here.
         */
        DescriptionElementBase descriptionElementBase = featureToElementMap.get(feature);
        if(descriptionElementBase!=null && descriptionElementBase.isInstanceOf(CategoricalData.class) && newValue instanceof Collection){
            CategoricalData categoricalData = HibernateProxyHelper.deproxy(descriptionElementBase, CategoricalData.class);
            categoricalData.setStateDataOnly(new ArrayList<>((Collection<State>) newValue));
        }
    }
}
