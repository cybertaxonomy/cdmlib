/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

/**
 * @author Andreas Kohlbecker
 * @date Jul 24, 2012
 *
 */
public class CRUDPermissions {

    final static public byte CREATE =   1;    // 00000001
    final static public byte READ = 	 1 << 2; // 00000010
    final static public byte UPDATE = 1 << 3; // 00000100
    final static public byte DELETE = 1 << 4; // 00001000

}
