/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.in;

import java.util.Collection;

import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.term.FeatureTree;

/**
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
public class OwlImportState extends ImportStateBase<OwlImportConfigurator, OwlImport> {

    private Collection<FeatureTree> featureTrees;

    public OwlImportState(OwlImportConfigurator config) {
        super(config);
    }

    public Collection<FeatureTree> getFeatureTrees() {
        return featureTrees;
    }

    public void setFeatureTrees(Collection<FeatureTree> featureTrees) {
        this.featureTrees = featureTrees;
    }

}
