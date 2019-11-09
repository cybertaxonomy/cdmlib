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

import eu.etaxonomy.cdm.api.service.description.DistributionAggregation.AggregationMode;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author a.mueller
 * @since 05.11.2019
 */
public class DistributionAggregationConfiguration extends DescriptionAggregationConfiguration {

    private AggregationMode aggregationMode;

    private List<NamedArea> superAreas;

    private Rank lowerRank;
    private Rank upperRank;

    private Classification classification;

    public static DistributionAggregationConfiguration NewInstance(AggregationMode aggregationMode, List<NamedArea> superAreas,
            Rank lowerRank, Rank upperRank, Classification classification, IProgressMonitor monitor){
        return new DistributionAggregationConfiguration(aggregationMode, superAreas, lowerRank, upperRank, classification, monitor);
    }

// ************************ CONSTRUCTOR *****************************/

    private DistributionAggregationConfiguration(AggregationMode aggregationMode, List<NamedArea> superAreas,
            Rank lowerRank, Rank upperRank, Classification classification, IProgressMonitor monitor) {
        this.aggregationMode = aggregationMode;
        this.superAreas = superAreas;
        this.lowerRank = lowerRank;
        this.upperRank = upperRank;
        this.classification = classification;
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

    public Rank getLowerRank() {
        return lowerRank;
    }
    public void setLowerRank(Rank lowerRank) {
        this.lowerRank = lowerRank;
    }

    public Rank getUpperRank() {
        return upperRank;
    }
    public void setUpperRank(Rank upperRank) {
        this.upperRank = upperRank;
    }

    public Classification getClassification() {
        return classification;
    }
    public void setClassification(Classification classification) {
        this.classification = classification;
    }

}
