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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * This class represents associations between the described
 * {@link SpecimenOrObservationBase specimen or observation}
 * and a second one (for instance a host).
 * Only {@link SpecimenDescription specimen descriptions} may contain individuals association.
 * The association itself is described by a {@link MultilanguageText multilanguage text}.
 * <P>
 * This class corresponds (partially) to NaturalLanguageDescriptionType
 * according to the SDD schema.
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:28
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IndividualsAssociation", propOrder = {
    "description",
    "associatedSpecimenOrObservation"
})
@XmlRootElement(name = "IndividualsAssociation")
@Entity
@Audited
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
public class IndividualsAssociation extends DescriptionElementBase {
	private static final long serialVersionUID = -4117554860254531809L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IndividualsAssociation.class);
	
	@XmlElement(name = "Description")
	@XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "IndividualAssociation_LanguageString")
	private Map<Language,LanguageString> description = new HashMap<Language,LanguageString>();
	
	@XmlElement(name = "AssociatedSpecimenOrObservation")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private SpecimenOrObservationBase associatedSpecimenOrObservation;

	/** 
	 * Class constructor: creates a new empty individuals association instance.
	 */
	protected IndividualsAssociation(){
		super(null);
	}
	
	/** 
	 * Creates a new empty individuals association instance.
	 */
	public static IndividualsAssociation NewInstance(){
		return new IndividualsAssociation();
	}
	

	/** 
	 * Returns the second {@link SpecimenOrObservationBase specimen or observation}
	 * involved in <i>this</i> individuals association.
	 * The first specimen or observation is the specimen or observation
	 * described in the corresponding {@link SpecimenDescription specimen description}.
	 */
	public SpecimenOrObservationBase getAssociatedSpecimenOrObservation() {
		return associatedSpecimenOrObservation;
	}
	/**
	 * @see	#getAssociatedSpecimenOrObservation() 
	 */
	public void setAssociatedSpecimenOrObservation(
			SpecimenOrObservationBase associatedSpecimenOrObservation) {
		this.associatedSpecimenOrObservation = associatedSpecimenOrObservation;
	}

	
	/** 
	 * Returns the {@link MultilanguageText multilanguage text} used to describe
	 * <i>this</i> individuals association. The different {@link LanguageString language strings}
	 * contained in the multilanguage text should all have the same meaning.
	 */
	public Map<Language,LanguageString> getDescription(){
		return this.description;
	}
	
	/**
	 * Adds a translated {@link LanguageString text in a particular language}
	 * to the {@link MultilanguageText multilanguage text} used to describe
	 * <i>this</i> individuals association.
	 * 
	 * @param description	the language string describing the individuals association
	 * 						in a particular language
	 * @see    	   			#getDescription()
	 * @see    	   			#addDescription(String, Language)
	 */
	public void addDescription(LanguageString description){
		this.description.put(description.getLanguage(),description);
	}
	/**
	 * Creates a {@link LanguageString language string} based on the given text string
	 * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text} 
	 * used to describe <i>this</i> individuals association.
	 * 
	 * @param text		the string describing the individuals association
	 * 					in a particular language
	 * @param language	the language in which the text string is formulated
	 * @see    	   		#getDescription()
	 * @see    	   		#addDescription(LanguageString)
	 */
	public void addDescription(String text, Language language){
		this.description.put(language, LanguageString.NewInstance(text, language));
	}
	/** 
	 * Removes from the {@link MultilanguageText multilanguage text} used to describe
	 * <i>this</i> individuals association the one {@link LanguageString language string}
	 * with the given {@link Language language}.
	 *
	 * @param  language	the language in which the language string to be removed
	 * 					has been formulated
	 * @see     		#getDescription()
	 */
	public void removeDescription(Language language){
		this.description.remove(language);
	}

}
