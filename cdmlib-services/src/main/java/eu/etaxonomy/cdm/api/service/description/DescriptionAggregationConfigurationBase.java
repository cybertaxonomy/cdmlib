/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import java.io.Serializable;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;

/**
 * Configurator base class for all {@link DescriptionAggregationBase description aggregations}.

 * @author a.mueller
 * @since 03.11.2019
 */
public abstract class DescriptionAggregationConfigurationBase implements Serializable {

    private static final long serialVersionUID = -7914819539239986722L;

    private DescriptiveDataSet dataset;
    private TaxonNodeFilter taxonNodeFilter;
    private boolean aggregateToHigherRanks;

    private IProgressMonitor monitor;

// ****************** GETTER / SETTER *****************/

    public IProgressMonitor getMonitor() {
        return monitor;
    }
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    public DescriptiveDataSet getDataset() {
        return dataset;
    }
    public void setDataset(DescriptiveDataSet dataset) {
        this.dataset = dataset;
    }

    public boolean isAggregateToHigherRanks() {
        return aggregateToHigherRanks;
    }
    public void setAggregateToHigherRanks(boolean aggregateToHigherRanks) {
        this.aggregateToHigherRanks = aggregateToHigherRanks;
    }

    public TaxonNodeFilter getTaxonNodeFilter() {
        return taxonNodeFilter;
    }
    public void setTaxonNodeFilter(TaxonNodeFilter taxonNodeFilter) {
        this.taxonNodeFilter = taxonNodeFilter;
    }
}
