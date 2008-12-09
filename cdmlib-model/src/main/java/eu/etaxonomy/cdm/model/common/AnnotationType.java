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

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Annotation types ...
 * @author a.mueller
 * @version 1.0
 * @created 12-Nov-2008 15:37:33
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnnotationType")
@XmlRootElement(name = "AnnotationType")
@Entity
public class AnnotationType extends DefinedTermBase {
	private static final long serialVersionUID = 49629121282854575L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AnnotationType.class);

	private static final UUID uuidTechnical = UUID.fromString("6a5f9ea4-1bdd-4906-89ad-6e669f982d69");
	private static final UUID uuidEditorial = UUID.fromString("34204192-b41d-4857-a1d4-28992bef2a2a");
	
	public static AnnotationType NewInstance(String term, String label, String labelAbbrev){
		return new AnnotationType(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 * @param term
	 * @param label
	 */
	public AnnotationType() {
		super();
	}
	
	/**
	 * Constructor
	 * @param term
	 * @param label
	 */
	protected AnnotationType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
	

	public static final AnnotationType getByUuid(UUID uuid){
		return (AnnotationType) findByUuid(uuid);
	}	


	public static final AnnotationType TECHNICAL(){
		return getByUuid(uuidTechnical);
	}

	public static final AnnotationType EDITORIAL(){
		return getByUuid(uuidEditorial);
	}

}