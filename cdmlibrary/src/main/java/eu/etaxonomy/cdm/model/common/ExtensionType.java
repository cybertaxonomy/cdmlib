/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

/**
 * Extension types similar to dynamically defined attributes. These are not data
 * types, but rather content types like "DOI", "2nd nomenclatural reference", "3rd
 * hybrid parent" or specific local identifiers.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:13
 */
public class ExtensionType extends DefinedTermBase {
	static Logger logger = Logger.getLogger(ExtensionType.class);

	public static final ExtensionType XML_FRAGMENT(){
		return null;
	}

	public static final ExtensionType RDF_FRAGMENT(){
		return null;
	}

}