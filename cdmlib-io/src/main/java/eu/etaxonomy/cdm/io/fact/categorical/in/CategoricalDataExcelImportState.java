/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.categorical.in;

import eu.etaxonomy.cdm.io.fact.in.FactExcelImportStateBase;

/**
 * State for taxon based categorical data import.
 *
 * @author a.mueller
 * @since 06.07.2020
 */
public class CategoricalDataExcelImportState
        extends FactExcelImportStateBase<CategoricalDataExcelImportConfigurator>{

    public CategoricalDataExcelImportState(CategoricalDataExcelImportConfigurator config) {
        super(config);
    }
}
