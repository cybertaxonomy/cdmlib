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

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author m.doering
 * @created 08-Nov-2007 13:06:50
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RightsType")
@XmlRootElement(name = "RightsType")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class RightsType extends DefinedTermBase<RightsType> {
	private static final long serialVersionUID = -5823263624000932116L;
	private static final Logger logger = Logger.getLogger(RightsType.class);

	protected static Map<UUID, RightsType> termMap = null;


	/**
	 * Factory method
	 * @return
	 */
	public static RightsType NewInstance(){
		logger.debug("NewInstance");
		return new RightsType();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static RightsType NewInstance(String text, String label, String labelAbbrev){
		return new RightsType(text, label, labelAbbrev);
	}

//***************** CONSTRUCTOR **************************************/

	//for javassist only
	@Deprecated
	protected RightsType() {
		super(TermType.RightsType);
	}

	/**
	 * Constructor
	 */
	private RightsType(String term, String label, String labelAbbrev) {
		super(TermType.RightsType, term, label, labelAbbrev);
	}



// ************************************* MTEHODS ***************************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}

	protected static RightsType getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(RightsType.class, uuid);
        } else {
            return termMap.get(uuid);
        }
	}

	/**
	 * http://purl.org/dc/terms/accessRights
	 */
	public static final RightsType ACCESS_RIGHTS(){
		return getTermByUuid(uuidAccessRights);
	}

	public static final RightsType COPYRIGHT(){
		return getTermByUuid(uuidCopyright);
	}

	public static final RightsType LICENSE(){
		return getTermByUuid(uuidLicense);
	}

	private static final UUID uuidLicense = UUID.fromString("67c0d47e-8985-1014-8845-c84599f9992c");
	private static final UUID uuidCopyright = UUID.fromString("d1ef838e-b195-4f28-b8eb-0d3be080bd37");
	private static final UUID uuidAccessRights = UUID.fromString("a50b4def-b3ac-4508-b50a-e0f249e3a1d7");


	@Override
	protected void setDefaultTerms(TermVocabulary<RightsType> termVocabulary) {
		termMap = new HashMap<UUID, RightsType>();
		for (RightsType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

}