/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;

/**
 * @author a.mueller
 * @since 12.11.2019
 */
public class StructuredDescriptionAggregationConfiguration
        extends DescriptionAggregationConfigurationBase<StructuredDescriptionAggregation> {

    private static final long serialVersionUID = 7485291596888612932L;

    private DescriptiveDataSet dataset;


    //TODO merge with DistributionAggregationConfiguration.aggregationMode
    private boolean aggregateToHigherRanks;

    boolean includeDefault = true;
    boolean includeLiterature = false;

// ******************* FACTORY ***************************************/

    public static StructuredDescriptionAggregationConfiguration NewInstance(
            TaxonNodeFilter filter, IProgressMonitor monitor){
        return new StructuredDescriptionAggregationConfiguration(filter, monitor, null, null);
    }


    public static StructuredDescriptionAggregationConfiguration NewInstance(TaxonNodeFilter filter,
            IProgressMonitor monitor, Boolean includeDefault, Boolean includeLiterature){
        return new StructuredDescriptionAggregationConfiguration(filter, monitor, includeDefault, includeLiterature);
    }

// ******************* CONSTRUCTOR ***********************************/

    protected StructuredDescriptionAggregationConfiguration(TaxonNodeFilter filter,
            IProgressMonitor monitor, Boolean includeDefault, Boolean includeLiterature) {
        super(filter, monitor);
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


    public boolean isAggregateToHigherRanks() {
        return aggregateToHigherRanks;
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

    public DescriptiveDataSet getDataset() {
        return dataset;
    }
    public void setDataset(DescriptiveDataSet dataset) {
        this.dataset = dataset;
    }
}
