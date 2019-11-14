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

/**
 * Configurator base class for all {@link DescriptionAggregationBase description aggregations}.
 * @author a.mueller
 * @since 03.11.2019
 */
public abstract class DescriptionAggregationConfigurationBase<TASK extends DescriptionAggregationBase> implements Serializable {

    private static final long serialVersionUID = -7914819539239986722L;

    private TaxonNodeFilter taxonNodeFilter;

    private boolean aggregateToHigherRanks;

    private IProgressMonitor monitor;

    private boolean doClearExistingDescription = false;

//******************* CONSTRUCTOR **********************/

    protected DescriptionAggregationConfigurationBase(TaxonNodeFilter filter, IProgressMonitor monitor) {
        this.taxonNodeFilter = filter;
        this.monitor = monitor;
    }

// ********************** METHODS ***************************/

    public abstract TASK getTaskInstance();

// ****************** GETTER / SETTER *****************/

    public IProgressMonitor getMonitor() {
        return monitor;
    }
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
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

    public boolean isDoClearExistingDescription() {
        return doClearExistingDescription;
    }

    public void setDoClearExistingDescription(boolean doClearExistingDescription) {
        this.doClearExistingDescription = doClearExistingDescription;
    }
}
