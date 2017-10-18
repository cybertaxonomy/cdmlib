/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

/**
 * @author a.kohlbecker
 * @since 18.10.2017
 *
 */
public class CdmAuthorityParsingException extends Exception {

    private static final long serialVersionUID = -1458716979023979164L;

    public CdmAuthorityParsingException(String authority){
        super(authority);
    }

}
