/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The class representing the restriction concerning the life stage for
 * the applicability of {@link TaxonDescription taxon descriptions}. The life stage of a
 * {@link SpecimenOrObservationBase specimen or observation}
 * does not belong to a {@link SpecimenDescription specimen description} but is an attribute of
 * the specimen itself.
 * 
 * @author m.doering
 * @created 08-Nov-2007 13:06:53
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Stage")
@XmlRootElement(name = "Stage")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class Stage extends Scope {
	private static final long serialVersionUID = 2338810516765894760L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Stage.class);

	private static Map<UUID, Stage> termMap = null;
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty life stage instance.
	 * 
	 * @see #Stage(String, String, String)
	 */
	public Stage(){
	}

	/** 
	 * Class constructor: creates a new life stage instance with a description
	 * (in the {@link Language#DEFAULT() default language}), a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new life stage to be created 
	 * @param	label  		 the string identifying the new life stage to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new life stage to be created
	 * @see 				 #Stage()
	 */
	public Stage(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	//********* METHODS **************************************/
	/** 
	 * Creates a new empty life stage instance.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	public static Stage NewInstance(){
		return new Stage();
	}
	
	/** 
	 * Creates a new life stage instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new life stage to be created 
	 * @param	label  		 the string identifying the new life stage to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new life stage to be created
	 * @see 				 #NewInstance()
	 */
	public static Stage NewInstance(String term, String label, String labelAbbrev){
		return new Stage(term, label, labelAbbrev);
	}
	

	
//************************** METHODS ********************************
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}

	
	@Override
	protected void setDefaultTerms(TermVocabulary<Modifier> termVocabulary) {
		termMap = new HashMap<UUID, Stage>();
		for (Modifier term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), (Stage)term);  //TODO casting
		}
	}
}