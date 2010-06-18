/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

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
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class AnnotationType extends DefinedTermBase<AnnotationType> {
	private static final long serialVersionUID = 49629121282854575L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AnnotationType.class);

	protected static Map<UUID, AnnotationType> termMap = null;		
	
	private static final UUID uuidTechnical = UUID.fromString("6a5f9ea4-1bdd-4906-89ad-6e669f982d69");
	private static final UUID uuidEditorial = UUID.fromString("e780d5fd-abfc-4025-938a-46deb751d808");
	private static final UUID uuidTreeIndex = UUID.fromString("28f4e205-1d02-4d3a-8288-775ea8413009");

	public static AnnotationType NewInstance(String term, String label, String labelAbbrev){
		return new AnnotationType(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 * @param term
	 * @param label
	 */
	public AnnotationType() {
	}
	
	/**
	 * Constructor
	 * @param term
	 * @param label
	 */
	protected AnnotationType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	
//************************** METHODS ********************************
	
	protected static AnnotationType getTermByUuid(UUID uuid){
		if (termMap == null){
			return null;  //better return null then initialize the termMap in an unwanted way 
		}
		return (AnnotationType)termMap.get(uuid);
	}
	
	
	public static final AnnotationType TECHNICAL(){
		return getTermByUuid(uuidTechnical);
	}

	public static final AnnotationType EDITORIAL(){
		return getTermByUuid(uuidEditorial);
	}
	
	public static final AnnotationType TREEINDEX(){
		return getTermByUuid(uuidTreeIndex);
	}

	protected void setDefaultTerms(TermVocabulary<AnnotationType> termVocabulary) {
		termMap = new HashMap<UUID, AnnotationType>();
		for (AnnotationType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), (AnnotationType)term);
		}	
	}

}