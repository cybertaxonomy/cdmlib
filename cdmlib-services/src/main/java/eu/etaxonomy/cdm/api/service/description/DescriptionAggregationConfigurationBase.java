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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;

/**
 * Configuration base class for all {@link DescriptionAggregationBase description aggregations}.
 *
 * @author a.mueller
 * @since 03.11.2019
 */
public abstract class DescriptionAggregationConfigurationBase<TASK extends DescriptionAggregationBase> implements Serializable {

    private static final long serialVersionUID = -7914819539239986722L;

    private TaxonNodeFilter taxonNodeFilter;

    private SourceMode toParentSourceMode = SourceMode.DESCRIPTION;
    private SourceMode withinTaxonSourceMode = SourceMode.ALL_SAMEVALUE;

    private List<AggregationMode> aggregationModes;

    private boolean doClearExistingDescription = false;
    private boolean doReuseDescriptions = false;
    private boolean doReuseDescriptionElements = false;
    private boolean doReuseSources = false;

    private IProgressMonitor monitor;

    private EnumSet<OriginalSourceType> aggregatingSourceTypes = EnumSet.of(
            OriginalSourceType.PrimaryTaxonomicSource, OriginalSourceType.PrimaryMediaSource);

// ************************** ENUMS ******************************/

    public enum AggregationMode {
        WithinTaxon,
        ToParent;
//        public boolean isByRank() {
//           return this==byRanks || this == byAreasAndRanks;
//        }
//        public boolean isByArea() {
//            return this==byAreas || this == byAreasAndRanks;
//         }
        public static List<AggregationMode> byAreasAndRanks(){
            return Arrays.asList(new AggregationMode[]{AggregationMode.WithinTaxon, AggregationMode.ToParent});
        }
        public static List<AggregationMode> byAreas(){
            return Arrays.asList(new AggregationMode[]{AggregationMode.WithinTaxon});
        }
        public static List<AggregationMode> byRanks(){
            return Arrays.asList(new AggregationMode[]{AggregationMode.ToParent});
        }

    }

    public enum SourceMode {
        NONE,
        ALL,
        ALL_SAMEVALUE,
        DESCRIPTION,
        TAXON;
     }

//******************* CONSTRUCTOR **********************/

    protected DescriptionAggregationConfigurationBase(TaxonNodeFilter filter,
            IProgressMonitor monitor, List<AggregationMode> aggregationModes) {
        this.taxonNodeFilter = filter;
        this.aggregationModes = aggregationModes;
        if(aggregationModes == null){
            aggregationModes = Arrays.asList(new AggregationMode[]{AggregationMode.WithinTaxon, AggregationMode.ToParent});
        }
        this.monitor = monitor;
    }

// ********************** METHODS ***************************/

    public abstract TASK getTaskInstance();

// ****************** GETTER / SETTER *****************/

    public List<AggregationMode> getAggregationModes() {
        return aggregationModes;
    }
    public void setAggregationMode(List<AggregationMode> aggregationModes) {
        this.aggregationModes = aggregationModes;
    }

    public IProgressMonitor getMonitor() {
        return monitor;
    }
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
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

    public EnumSet<OriginalSourceType> getAggregatingSourceTypes() {
        return aggregatingSourceTypes;
    }
    public void setAggregatingSourceTypes(EnumSet<OriginalSourceType> aggregatingSourceTypes) {
        this.aggregatingSourceTypes = aggregatingSourceTypes;
    }
    public void addAggregatingSourceTypes(OriginalSourceType sourceTypeToAdd){
        this.aggregatingSourceTypes.add(sourceTypeToAdd);
    }
    public boolean removeAggregatingSourceTypes(OriginalSourceType sourceTypeToRemove){
        return this.aggregatingSourceTypes.remove(sourceTypeToRemove);
    }

    public SourceMode getToParentSourceMode() {
        return toParentSourceMode;
    }
    public void setToParentSourceMode(SourceMode toParentSourceMode) {
        this.toParentSourceMode = toParentSourceMode;
    }

    public SourceMode getWithinTaxonSourceMode() {
        return withinTaxonSourceMode;
    }
    public void setWithinTaxonSourceMode(SourceMode withinTaxonSourceMode) {
        this.withinTaxonSourceMode = withinTaxonSourceMode;
    }
}
