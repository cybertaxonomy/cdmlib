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

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * This class represents possible modulations for the validity of
 * information pieces ({@link DescriptionElementBase} description elements).
 * It can cover probability ("perhaps"), frequency ("often") intensity ("very"),
 * timing ("spring") and other domains. Its instances can be grouped to build
 * different controlled {@link TermVocabulary term vocabularies}.
 * <P>
 * This class corresponds to GeneralModifierNLDType according to
 * the SDD schema.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:35
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Modifier")
@XmlRootElement(name = "Modifier")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class Modifier extends OrderedTermBase<Modifier> {
	private static final long serialVersionUID = -2491833848163461951L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Modifier.class);
	
	protected static Map<UUID, Modifier> termMap = null;
	
	/** 
	 * Class constructor: creates a new empty modifier instance.
	 * 
	 * @see #Modifier(String, String, String)
	 */
	public Modifier(){
	}
	

	/** 
	 * Class constructor: creates a new modifier with a description
	 * (in the {@link Language#DEFAULT() default language}), a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new modifier to be created 
	 * @param	label  		 the string identifying the new modifier to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new modifier to be created
	 * @see 				 #Modifier()
	 */
	public Modifier(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	/** 
	 * Creates a new empty modifier instance.
	 * 
	 * @see #Modifier(String, String, String)
	 */
	public static Modifier NewInstance(){
		return new Modifier();
	}


	@Override
	protected void setDefaultTerms(TermVocabulary<Modifier> termVocabulary) {
		termMap = new HashMap<UUID, Modifier>();
		for (Modifier term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}
	
}