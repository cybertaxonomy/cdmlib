/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.out;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

import eu.etaxonomy.cdm.io.common.ExportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;

/**
 *
 * @author pplitzner
 * @since May 2, 2019
 *
 */
public class StructureTreeOwlExportState extends ExportStateBase<StructureTreeOwlExportConfigurator, IExportTransformer, File>{

    private Model model;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    public StructureTreeOwlExportState(StructureTreeOwlExportConfigurator config) {
        super(config);
        model = OwlUtil.createModel();
    }

    public Model getModel() {
        return model;
    }

}
