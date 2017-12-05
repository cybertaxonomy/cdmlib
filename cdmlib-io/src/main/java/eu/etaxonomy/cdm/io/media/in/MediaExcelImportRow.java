/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.media.in;

import java.util.UUID;

/**
 * Data holder class for Media taxon imports
 * @author a.mueller
 * @date 02.11.2017
 *
 */
public class MediaExcelImportRow {
    private UUID taxonUuid;
    private String fullTaxonName;
    private String fileName;
    private String copyright;
    private String artist;

}
