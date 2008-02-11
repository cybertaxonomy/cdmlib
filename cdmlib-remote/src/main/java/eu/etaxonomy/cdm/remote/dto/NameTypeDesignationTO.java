/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 05.02.2008 14:59:28
 *
 */
public class NameTypeDesignationTO extends ReferencedEntityBaseTO {
	
	private boolean isRejectedType;
	private boolean isConservedType;
	private NameSTO typeSpecies; 
}
