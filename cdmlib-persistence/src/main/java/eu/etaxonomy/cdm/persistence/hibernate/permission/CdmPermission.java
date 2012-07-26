/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate.permission;

/**
 * @author k.luther
 * @date 06.07.2011
 *
 */
public enum CdmPermission {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    DENYALL,
    ADMIN;
}
