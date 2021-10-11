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

    boolean includeDefault = true;
    boolean includeLiterature = true;
    /**
     * If source mode is {@link AggregationSourceMode#DESCRIPTION} descriptions
     * are cloned as sources. This parameter defines if aggregated descriptions
     * being the sources for further aggregation should also be cloned or
     * can be handled as stable as usually they are not changing overtime
     * or stability is not a requirement.
     * TODO maybe we want to move it to base class
     * TODO maybe we need the same for non-aggregated descriptions
     * (generell or specific for specimen, literature and/or default descriptions).
     */
    boolean cloneAggregatedSourceDescriptions = false;

    private MissingMinimumMode missingMinimumMode = MissingMinimumMode.MinToZero;
    private MissingMaximumMode missingMaximumMode = MissingMaximumMode.MaxToMin;

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
        setWithinTaxonSourceMode(AggregationSourceMode.DESCRIPTION);  //default mode for structured descriptions
        setToParentSourceMode(AggregationSourceMode.TAXON);  //default mode for structured descriptions
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

    public MissingMinimumMode getMissingMinimumMode() {
        return missingMinimumMode;
    }
    public void setMissingMinimumMode(MissingMinimumMode missingMinimumMode) {
        this.missingMinimumMode = missingMinimumMode;
    }

    public MissingMaximumMode getMissingMaximumMode() {
        return missingMaximumMode;
    }
    public void setMissingMaximumMode(MissingMaximumMode missingMaximumMode) {
        this.missingMaximumMode = missingMaximumMode;
    }

    public boolean isCloneAggregatedSourceDescriptions() {
        // TODO Auto-generated method stub
        return false;
    }
}
