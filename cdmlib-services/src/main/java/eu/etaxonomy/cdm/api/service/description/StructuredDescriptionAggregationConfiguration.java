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

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;

/**
 * @author a.mueller
 * @since 12.11.2019
 */
public class StructuredDescriptionAggregationConfiguration
        extends DescriptionAggregationConfigurationBase<StructuredDescriptionAggregation> {

    private static final long serialVersionUID = 7485291596888612932L;

    private UUID datasetUuid;


    //TODO merge with DistributionAggregationConfiguration.aggregationMode
    private boolean aggregateToHigherRanks;

    boolean includeDefault = true;
    boolean includeLiterature = false;

// ******************* FACTORY ***************************************/

    public static StructuredDescriptionAggregationConfiguration NewInstance(
            TaxonNodeFilter filter, IProgressMonitor monitor){
        return new StructuredDescriptionAggregationConfiguration(filter, null, monitor, null, null);
    }


    public static StructuredDescriptionAggregationConfiguration NewInstance(TaxonNodeFilter filter,
            IProgressMonitor monitor, Boolean includeDefault, Boolean includeLiterature){
        return new StructuredDescriptionAggregationConfiguration(filter, null, monitor, includeDefault, includeLiterature);
    }

// ******************* CONSTRUCTOR ***********************************/

    protected StructuredDescriptionAggregationConfiguration(TaxonNodeFilter filter,
            List<AggregationMode> aggregationModes, IProgressMonitor monitor, Boolean includeDefault, Boolean includeLiterature) {
        super(filter, monitor, aggregationModes);
        if (includeDefault != null){
            this.includeDefault = includeDefault;
        }
        if (includeLiterature != null){
            this.includeLiterature = includeLiterature;
        }
    }

// ******************** METHOD **************************************/

    @Override
    public StructuredDescriptionAggregation getTaskInstance() {
        return new StructuredDescriptionAggregation();
    }

// *********************** GETTER / SETTER ****************************/

    //TODO remove
//    public boolean isAggregateToHigherRanks() {
//        return getAggregationModes().contains(AggregationMode.ToParent);
//    }
    public boolean isAggregateToHigherRanks() {
        return this.aggregateToHigherRanks;
    }
    public void setAggregateToHigherRanks(boolean aggregateToHigherRanks) {
        this.aggregateToHigherRanks = aggregateToHigherRanks;
    }

    public boolean isIncludeDefault() {
        return includeDefault;
    }
    public void setIncludeDefault(boolean includeDefault) {
        this.includeDefault = includeDefault;
    }

    public boolean isIncludeLiterature() {
        return includeLiterature;
    }
    public void setIncludeLiterature(boolean includeLiterature) {
        this.includeLiterature = includeLiterature;
    }

    public UUID getDatasetUuid() {
        return datasetUuid;
    }
    public void setDatasetUuid(UUID datasetUuid) {
        this.datasetUuid = datasetUuid;
    }
}
