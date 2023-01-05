/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdm2cdm;

/**
 * The mode how users should be handled during import.
 *
 * Note: This is not in the common package as CDM2CDM allows more modes (e.g. "original" then usual imports).
 *       But still should be discussed.
 *
 * @author a.mueller
 * @date 02.09.2022
 */
public enum UserImportMode {
    NONE,
    ORIGINAL,
//    CURRENT_USER,    not yet supported
//    ADMIN,
//    IMPORT_USER
    ;
}