/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


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
 * Controlled vocabulary to differentiate categories of areas
 * @author m.doering
 * @since 08-Nov-2007 13:06:37
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NamedAreaType")
@XmlRootElement(name = "NamedAreaType")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class NamedAreaType extends DefinedTermBase<NamedAreaType> {
	private static final long serialVersionUID = 8280172429797206548L;
	private static final Logger logger = Logger.getLogger(NamedAreaType.class);

	protected static Map<UUID, NamedAreaType> termMap = null;

	private static final UUID uuidNaturalArea = UUID.fromString("cc33167c-d366-4030-b984-6b14e4f5fd22");
	private static final UUID uuidAdministrationArea = UUID.fromString("1799f581-f425-40d6-a4db-ec2c638c0e92");


	/**
	 * Factory method
	 * @return
	 */
	public static NamedAreaType NewInstance(String term, String label, String labelAbbrev){
		logger.debug("NewInstance");
		return new NamedAreaType(term, label, labelAbbrev);
	}

// *********************** CONSTRUCTOR ******************************/

	//for hibernate use only
	@Deprecated
	protected NamedAreaType(){
		super(TermType.NamedAreaType);
	}

	private NamedAreaType(String term, String label, String labelAbbrev) {
		super(TermType.NamedAreaType, term, label, labelAbbrev);
	}

//************************** METHODS ********************************


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}


	protected static NamedAreaType getTermByUuid(UUID uuid){
	    if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(NamedAreaType.class, uuid);
        } else {
            return termMap.get(uuid);
        }
	}

	/**
	 * The boundaries are given by natural factors (mountains, valleys, climate, etc.)
	 */
	public static final NamedAreaType NATURAL_AREA(){
		return getTermByUuid(uuidNaturalArea);
	}

	/**
	 * The boundaries depend on administration (county, state, reserve, etc.)
	 */
	public static final NamedAreaType ADMINISTRATION_AREA(){
		return getTermByUuid(uuidAdministrationArea);
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<NamedAreaType> termVocabulary) {
		termMap = new HashMap<UUID, NamedAreaType>();
		for (NamedAreaType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

}
