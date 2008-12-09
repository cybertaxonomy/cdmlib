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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Extension types similar to dynamically defined attributes. These are not data
 * types, but rather content types like "DOI", "2nd nomenclatural reference", "3rd
 * hybrid parent" or specific local identifiers.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtensionType")
@Entity
public class ExtensionType extends DefinedTermBase {
	private static final long serialVersionUID = -7761963794004133427L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExtensionType.class);

	public ExtensionType() {
		super();
	}
	public ExtensionType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}


	public static final ExtensionType XML_FRAGMENT(){
		return null;
	}

	public static final ExtensionType RDF_FRAGMENT(){
		return null;
	}

}