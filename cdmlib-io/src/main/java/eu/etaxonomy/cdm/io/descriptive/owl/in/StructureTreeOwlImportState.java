/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.in;

import com.hp.hpl.jena.rdf.model.Model;

import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;

/**
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
public class StructureTreeOwlImportState extends ImportStateBase<StructureTreeOwlImportConfigurator, StructureTreeOwlImport> {

    private Model model;

    protected StructureTreeOwlImportState(StructureTreeOwlImportConfigurator config) {
        super(config);
        model = OwlUtil.createModel();
    }

    public Model getModel() {
        return model;
    }

}
