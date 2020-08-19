/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.temporal.in;

import eu.etaxonomy.cdm.io.fact.in.FactExcelImportStateBase;

/**
 * State for taxon based temporal data import.
 *
 * @author a.mueller
 * @since 15.07.2020
 */
public class TemporalDataExcelImportState<CONFIG extends TemporalDataExcelImportConfigurator>
        extends FactExcelImportStateBase<CONFIG>{

    public TemporalDataExcelImportState(CONFIG config) {
        super(config);
    }
}
