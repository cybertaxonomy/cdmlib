/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;


import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Controlled vocabulary to differentiate categories of areas
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:37
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NamedAreaType")
@XmlRootElement(name = "NamedAreaType")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class NamedAreaType extends DefinedTermBase<NamedAreaType> {
	private static final long serialVersionUID = 8280172429797206548L;
	private static final Logger logger = Logger.getLogger(NamedAreaType.class);

	private static final UUID uuidNaturalArea = UUID.fromString("cc33167c-d366-4030-b984-6b14e4f5fd22");
	private static final UUID uuidAdministrationArea = UUID.fromString("1799f581-f425-40d6-a4db-ec2c638c0e92");
	private static NamedAreaType NATURAL_AREA;
	private static NamedAreaType ADMINISTRATION_AREA;

	
	
	/**
	 * Factory method
	 * @return
	 */
	public static NamedAreaType NewInstance(String term, String label, String labelAbbrev){
		logger.debug("NewInstance");
		return new NamedAreaType(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	public NamedAreaType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
	
	public NamedAreaType(){
	}	
	
	/**
	 * The boundaries are given by natural factors (mountains, valleys, climate, etc.)
	 */
	public static final NamedAreaType NATURAL_AREA(){
		return NATURAL_AREA;
	}

	/**
	 * The boundaries depend on administration (county, state, reserve, etc.)
	 */
	public static final NamedAreaType ADMINISTRATION_AREA(){
		return ADMINISTRATION_AREA;
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<NamedAreaType> termVocabulary) {
		NamedAreaType.ADMINISTRATION_AREA = termVocabulary.findTermByUuid(NamedAreaType.uuidAdministrationArea);
		NamedAreaType.NATURAL_AREA = termVocabulary.findTermByUuid(NamedAreaType.uuidNaturalArea);
	}
	
}