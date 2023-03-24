/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.commonname.in;

import eu.etaxonomy.cdm.io.fact.in.FactExcelImportStateBase;

/**
 * @author a.mueller
 * @since 24.01.2023
 */
public class CommonNameExcelImportState
        extends FactExcelImportStateBase<CommonNameExcelImportConfigurator>{

    public CommonNameExcelImportState(CommonNameExcelImportConfigurator config) {
        super(config);
    }
}