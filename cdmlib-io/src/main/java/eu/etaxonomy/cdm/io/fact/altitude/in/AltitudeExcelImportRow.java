/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.altitude.in;

import java.util.UUID;

/**
 * Data holder class for altitude import for taxa.
 *
 * @author a.mueller
 * @since 28.05.2020
 */
public class AltitudeExcelImportRow {
    private UUID taxonUuid;
    private String fullTaxonName;
    private String fileName;
    private String copyright;
    private String artist;

}
