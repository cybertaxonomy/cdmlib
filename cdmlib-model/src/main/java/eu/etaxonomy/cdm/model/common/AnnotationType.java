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

/**
 * Annotation types ...
 * @author a.mueller
 * @created 12-Nov-2008 15:37:33
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnnotationType")
@XmlRootElement(name = "AnnotationType")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class AnnotationType extends DefinedTermBase<AnnotationType> {
	private static final long serialVersionUID = 49629121282854575L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AnnotationType.class);

	protected static Map<UUID, AnnotationType> termMap = null;

	private static final UUID uuidTechnical = UUID.fromString("6a5f9ea4-1bdd-4906-89ad-6e669f982d69");
	private static final UUID uuidEditorial = UUID.fromString("e780d5fd-abfc-4025-938a-46deb751d808");

	public static AnnotationType NewInstance(String term, String label, String labelAbbrev){
		return new AnnotationType(term, label, labelAbbrev);
	}

//********************************** Constructor *******************************************************************/

	//for hibernate use only
	@Deprecated
	protected AnnotationType() {
		super(TermType.AnnotationType);
	}

	/**
	 * Constructor
	 * @param term
	 * @param label
	 */
	protected AnnotationType(String term, String label, String labelAbbrev) {
		super(TermType.AnnotationType , term, label, labelAbbrev);
	}


//************************** METHODS ********************************

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}

	protected static AnnotationType getTermByUuid(UUID uuid){
	    if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(AnnotationType.class, uuid);
        }  else {
            return termMap.get(uuid);
        }
	}


	public static final AnnotationType TECHNICAL(){
		return getTermByUuid(uuidTechnical);
	}

	public static final AnnotationType EDITORIAL(){
		return getTermByUuid(uuidEditorial);
	}

	@Override
    protected void setDefaultTerms(TermVocabulary<AnnotationType> termVocabulary) {
		termMap = new HashMap<UUID, AnnotationType>();
		for (AnnotationType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

}