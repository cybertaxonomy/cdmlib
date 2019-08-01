/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.generate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;

/**
 * @author a.mueller
 * @since 26.07.2019
 */
public class PolytomousKeyGeneratorConfigurator {

    private boolean merge=true; // if this boolean is set to true, branches of the tree will be merged if the corresponding states can be used together without decreasing their score

    private boolean useDependencies = true; // if this boolean is true, the dependencies are taken into account

    private DescriptiveDataSet dataSet;

    /**
     * If true allows the generator to merge branches if the corresponding states can be used together without diminishing their score.
     * If false prevent the generator from merging branches (apart from those leading to the same set of taxa)
     */
    public boolean isMerge() {
        return merge;
    }

    public void setMerge(boolean merge) {
        this.merge = merge;
    }

    /**
     * If true allows the generator to use the dependencies given by the function "setDependencies".
     * If false prevents the generator from using dependencies.
     */
    public boolean isUseDependencies() {
        return useDependencies;
    }

    public void setUseDependencies(boolean useDependencies) {
        this.useDependencies = useDependencies;
    }

    public DescriptiveDataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DescriptiveDataSet dataSet) {
        this.dataSet = dataSet;
    }

    /**
     * @return
     */
    public Set<DescriptionBase<?>> getDescriptions() {
        Set<DescriptionBase<?>> result = (Set)dataSet.getDescriptions();
        if (result == null || result.isEmpty()){
            if (dataSet.getTaxonSubtreeFilter() != null){
                throw new RuntimeException("TaxonSubtreeFilter not yet implemented");
            }
        }
        return result;
    }
    public Set<TaxonDescription> getTaxonDescriptions() {
        Set<TaxonDescription> result = new HashSet<>();
        for (DescriptionBase<?> desc: getDescriptions()){
            if (desc.isInstanceOf(TaxonDescription.class)){
                result.add(CdmBase.deproxy(desc, TaxonDescription.class));
            }
        }
        return result;
    }

    /**
     * @return
     */
    public List<Feature> getFeatures() {
        @SuppressWarnings("unchecked")
        List<Feature> result = (List<Feature> )dataSet.getDescriptiveSystem().asTermList();
        return result;
    }

}
