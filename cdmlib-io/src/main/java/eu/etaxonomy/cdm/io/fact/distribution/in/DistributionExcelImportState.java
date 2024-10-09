/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.distribution.in;

import eu.etaxonomy.cdm.io.fact.in.FactExcelImportStateBase;

/**
 * @author a.mueller
 * @since 08.10.2024
 */
public class DistributionExcelImportState
        extends FactExcelImportStateBase<DistributionExcelImportConfigurator>{

    public DistributionExcelImportState(DistributionExcelImportConfigurator config) {
        super(config);
    }
}