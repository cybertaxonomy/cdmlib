/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.description.DistributionAggregation.AggregationMode;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.LogicFilter;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;

/**
 * @author a.mueller
 * @since 05.11.2019
 */
public class DistributionAggregationConfiguration extends DescriptionAggregationConfiguration {

    private AggregationMode aggregationMode;

    private List<NamedArea> superAreas;

    private TermCollection<PresenceAbsenceTerm, TermNode> statusOrder;

//    private Rank lowerRank;
//    private Rank upperRank;

//    private Classification classification;

    public static DistributionAggregationConfiguration NewInstance(AggregationMode aggregationMode, List<NamedArea> superAreas,
            TaxonNodeFilter filter, IProgressMonitor monitor){
        return new DistributionAggregationConfiguration(aggregationMode, superAreas, filter, monitor);
    }

    public static DistributionAggregationConfiguration NewInstance(AggregationMode aggregationMode, List<NamedArea> superAreas,
            TaxonNodeFilter filter, TermTree<PresenceAbsenceTerm> statusOrder,  IProgressMonitor monitor){
        DistributionAggregationConfiguration result = new DistributionAggregationConfiguration(aggregationMode, superAreas, filter, monitor);
        result.setStatusOrder(statusOrder);
        return result;
    }


// ************************ CONSTRUCTOR *****************************/

    private DistributionAggregationConfiguration(AggregationMode aggregationMode, List<NamedArea> superAreas,
            TaxonNodeFilter filter, IProgressMonitor monitor) {
        this.aggregationMode = aggregationMode;
        this.superAreas = superAreas;
        this.setTaxonNodeFilter(filter);
        setMonitor(monitor);
    }

// ******************* GETTER / SETTER ****************************/

    public AggregationMode getAggregationMode() {
        return aggregationMode;
    }
    public void setAggregationMode(AggregationMode aggregationMode) {
        this.aggregationMode = aggregationMode;
    }

    public List<NamedArea> getSuperAreas() {
        return superAreas;
    }
    public void setSuperAreas(List<NamedArea> superAreas) {
        this.superAreas = superAreas;
    }

    public UUID getLowerRank() {
        LogicFilter<Rank> rankMin = getTaxonNodeFilter().getRankMin();
        return rankMin == null ? null: rankMin.getUuid();
    }

    public UUID getUpperRank() {
        LogicFilter<Rank> rankMax = getTaxonNodeFilter().getRankMax();
        return rankMax == null ? null: rankMax.getUuid();
    }

    public TermCollection<PresenceAbsenceTerm, TermNode> getStatusOrder() {
        return statusOrder;
    }
    public void setStatusOrder(TermCollection<PresenceAbsenceTerm, TermNode> statusOrder) {
        this.statusOrder = statusOrder;
    }

//    public Classification getClassification() {
//        return classification;
//    }
//    public void setClassification(Classification classification) {
//        this.classification = classification;
//    }

}
