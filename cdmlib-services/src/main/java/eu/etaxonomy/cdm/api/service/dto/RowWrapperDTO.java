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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author pplitzner
 * @since 16.04.2018
 *
 */
public abstract class RowWrapperDTO <T extends DescriptionBase> implements Serializable {

    private static final long serialVersionUID = -7817164423660563673L;

    protected T description;

    private TaxonNodeDto taxonNode;
    private Map<Feature, DescriptionElementBase> featureToElementMap;

    public RowWrapperDTO(T description, TaxonNodeDto taxonNode) {
        this.taxonNode = taxonNode;
        this.featureToElementMap = new HashMap<>();
        this.description = description;
        Set<DescriptionElementBase> elements = description.getElements();
        for (DescriptionElementBase descriptionElementBase : elements) {
            Feature feature = descriptionElementBase.getFeature();
            featureToElementMap.put(feature, descriptionElementBase);
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

    public T getDescription() {
        return description;
    }

    public TaxonNodeDto getTaxonNode() {
        return taxonNode;
    }

    public DescriptionElementBase getDataValueForFeature(Feature feature){
        DescriptionElementBase descriptionElementBase = featureToElementMap.get(feature);
        return descriptionElementBase;
    }

    public void setDataValueForFeature(Feature feature, List<State> states){
        /* Only CategoricalData is handled here because for QuantitativeData the value
         * is set in the ModifyListener of the swt.Text in the CellEditor
         * for each StatisticalMeasure. So no need to set it again here.
         */
        DescriptionElementBase descriptionElementBase = featureToElementMap.get(feature);
        if(descriptionElementBase!=null && descriptionElementBase.isInstanceOf(CategoricalData.class)){
            CategoricalData categoricalData = HibernateProxyHelper.deproxy(descriptionElementBase, CategoricalData.class);
            categoricalData.setStateDataOnly(states);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((featureToElementMap == null) ? 0 : featureToElementMap.hashCode());
        result = prime * result + ((taxonNode == null) ? 0 : taxonNode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RowWrapperDTO other = (RowWrapperDTO) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (featureToElementMap == null) {
            if (other.featureToElementMap != null) {
                return false;
            }
        } else if (!featureToElementMap.equals(other.featureToElementMap)) {
            return false;
        }
        if (taxonNode == null) {
            if (other.taxonNode != null) {
                return false;
            }
        } else if (!taxonNode.equals(other.taxonNode)) {
            return false;
        }
        return true;
    }

}
