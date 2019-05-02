/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.XmlExportState;

/**
 *
 * @author pplitzner
 * @since May 2, 2019
 *
 */
public class StructureTreeOwlExportState extends XmlExportState<StructureTreeOwlExportConfigurator>{

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(StructureTreeOwlExportState.class);

    /**
     * @param config
     */
    public StructureTreeOwlExportState(StructureTreeOwlExportConfigurator config) {
        super(config);
    }



}
