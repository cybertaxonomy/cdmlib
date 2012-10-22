/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

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

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:50
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RightsTerm")
@XmlRootElement(name = "RightsTerm")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class RightsTerm extends DefinedTermBase<RightsTerm> {
	private static final long serialVersionUID = -5823263624000932116L;
	private static final Logger logger = Logger.getLogger(RightsTerm.class);

	protected static Map<UUID, RightsTerm> termMap = null;		

	
	/**
	 * Factory method
	 * @return
	 */
	public static RightsTerm NewInstance(){
		logger.debug("NewInstance");
		return new RightsTerm();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static RightsTerm NewInstance(String text, String label, String labelAbbrev){
		return new RightsTerm(text, label, labelAbbrev);
	}
	
	/**
	 * Default Constructor
	 */
	public RightsTerm() {
	}

	/**
	 * Constructor
	 */
	public RightsTerm(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	

// ************************************* MTEHODS ***************************************************/	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}
	
	protected static RightsTerm getTermByUuid(UUID uuid){
		if (termMap == null){
			return null;  //better return null then initialize the termMap in an unwanted way 
		}
		return (RightsTerm)termMap.get(uuid);
	}
	
	/**
	 * http://purl.org/dc/terms/accessRights
	 */
	public static final RightsTerm ACCESS_RIGHTS(){
		return getTermByUuid(uuidAccessRights);
	}

	public static final RightsTerm COPYRIGHT(){
		return getTermByUuid(uuidCopyright);
	}

	public static final RightsTerm LICENSE(){
		return getTermByUuid(uuidLicense);
	}
	
	private static final UUID uuidLicense = UUID.fromString("67c0d47e-8985-1014-8845-c84599f9992c");
	private static final UUID uuidCopyright = UUID.fromString("d1ef838e-b195-4f28-b8eb-0d3be080bd37");
	private static final UUID uuidAccessRights = UUID.fromString("a50b4def-b3ac-4508-b50a-e0f249e3a1d7");


	@Override
	protected void setDefaultTerms(TermVocabulary<RightsTerm> termVocabulary) {
		termMap = new HashMap<UUID, RightsTerm>();
		for (RightsTerm term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), (RightsTerm)term);
		}	
	}

}