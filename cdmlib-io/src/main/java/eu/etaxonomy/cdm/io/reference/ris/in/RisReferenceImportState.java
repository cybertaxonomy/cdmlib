/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.reference.ris.in;

import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author a.mueller
 * @date 11.05.2017
 *
 */
public class RisReferenceImportState
        extends ImportStateBase<RisReferenceImportConfigurator, RisReferenceImport>{

    /**
     * @param config
     */
    protected RisReferenceImportState(RisReferenceImportConfigurator config) {
        super(config);
    }

}
