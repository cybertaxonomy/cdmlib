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
import javax.persistence.MapKeyJoinColumn;
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
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.IMultiLanguageTextHolder;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.taxon.Taxon;

// FIXME
/**
 * This class represents interactions between the described {@link Taxon taxon}
 * and a second one (for instance a parasite, a prey or a hybrid parent).
 * Only {@link TaxonDescription taxon descriptions} may contain taxon interactions.
 * The interaction itself is described by a {@link MultilanguageText multilanguage text}.
 * <P>
 * This class corresponds to:  <ul>
 * <li> NaturalLanguageDescriptionType (partially) according to the SDD schema
 * <li> Associations according to the TDWG ontology
 * </ul>
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonInteraction", propOrder = {
    "description",
    "taxon2"
})
@XmlRootElement(name = "TaxonInteraction")
@Entity
@Audited
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
public class TaxonInteraction extends DescriptionElementBase implements IMultiLanguageTextHolder, Cloneable{
	private static final long serialVersionUID = -5014025677925668627L;
	private static final Logger logger = Logger.getLogger(TaxonInteraction.class);

	@XmlElement(name = "Description")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
	@MapKeyJoinColumn(name="description_mapkey_id")
    @JoinTable(name = "TaxonInteraction_LanguageString")  //to distinguish from other DescriptionElementBase_LanguageString
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    private Map<Language,LanguageString> description = new HashMap<Language,LanguageString>();

	@XmlElement(name = "Taxon2")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Taxon taxon2;

	/**
	 * Class constructor: creates a new empty taxon interaction instance.
	 */
	public TaxonInteraction() {
		super(null);
	}

	/**
	 * Creates a new empty taxon interaction instance.
	 */
	public static TaxonInteraction NewInstance(){
		return new TaxonInteraction();
	}

	/**
	 * Creates a new empty taxon interaction instance and also sets the feature
	 *
	 * @param feature
	 * @return
	 */
	public static TaxonInteraction NewInstance(Feature feature){
		TaxonInteraction taxonInteraction = new TaxonInteraction();
		if(feature.isSupportsTaxonInteraction()){
			taxonInteraction.setFeature(feature);
		}
		return taxonInteraction;
	}


	/**
	 * Returns the second {@link Taxon taxon} involved in <i>this</i> taxon interaction.
	 * The first taxon is the taxon described in the corresponding
	 * {@link TaxonDescription taxon description}.
	 */
	public Taxon getTaxon2(){
		return this.taxon2;
	}
	/**
	 * @see	#getTaxon2()
	 */
	public void setTaxon2(Taxon taxon2){
		this.taxon2 = taxon2;
	}

	/**
	 * Returns the {@link MultilanguageText multilanguage text} used to describe
	 * <i>this</i> taxon interaction. The different {@link LanguageString language strings}
	 * contained in the multilanguage text should all have the same meaning.
	 */
	public Map<Language,LanguageString> getDescriptions(){
		return this.description;
	}

	/**
	 * Returns the description string in the given {@link Language language}
	 *
	 * @param language	the language in which the description string looked for is formulated
	 * @see				#getDescriptions()
	 */
	public String getDescription(Language language){
	   Hibernate.initialize(description);
		LanguageString languageString = description.get(language);
		if (languageString == null){
			return null;
		}else{
			return languageString.getText();
		}
	}

	/**
	 * Adds a translated {@link LanguageString text in a particular language}
	 * to the {@link MultilanguageText multilanguage text} used to describe
	 * <i>this</i> taxon interaction.
	 *
	 * @param description	the language string describing the taxon interaction
	 * 						in a particular language
	 * @see    	   			#getDescription()
	 * @see    	   			#putDescription(String, Language)
	 * @deprecated			should follow the put semantic of maps, this method will be removed in v4.0
	 * 						Use the {@link #putDescription(LanguageString) putDescription} method instead
	 */
	@Deprecated
	public void addDescription(LanguageString description){
		this.putDescription(description);
	}

	/**
	 * Adds a translated {@link LanguageString text in a particular language}
	 * to the {@link MultilanguageText multilanguage text} used to describe
	 * <i>this</i> taxon interaction.
	 *
	 * @param description	the language string describing the taxon interaction
	 * 						in a particular language
	 * @see    	   			#getDescription()
	 * @see    	   			#putDescription(String, Language)
	 */
	public void putDescription(LanguageString description){
		this.description.put(description.getLanguage(),description);
	}
	/**
	 * Creates a {@link LanguageString language string} based on the given text string
	 * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text}
	 * used to describe <i>this</i> taxon interaction.
	 *
	 * @param text		the string describing the taxon interaction
	 * 					in a particular language
	 * @param language	the language in which the text string is formulated
	 * @see    	   		#getDescription()
	 * @see    	   		#putDescription(LanguageString)
	 */
	public void putDescription(Language language, String text){
		this.description.put(language, LanguageString.NewInstance(text, language));
	}

	/**
	 * Creates a {@link LanguageString language string} based on the given text string
	 * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text}
	 * used to describe <i>this</i> taxon interaction.
	 *
	 * @param text		the string describing the taxon interaction
	 * 					in a particular language
	 * @param language	the language in which the text string is formulated
	 * @see    	   		#getDescription()
	 * @see    	   		#addDescription(LanguageString)
	 * @deprecated 		should follow the put semantic of maps, this method will be removed in v4.0
	 * 					Use the {@link #putDescription(Language, String) putDescription} method instead
	 */
	@Deprecated
	public void addDescription(String text, Language language){
		this.putDescription(language, text);
	}
	/**
	 * Removes from the {@link MultilanguageText multilanguage text} used to describe
	 * <i>this</i> taxon interaction the one {@link LanguageString language string}
	 * with the given {@link Language language}.
	 *
	 * @param  lang	the language in which the language string to be removed
	 * 				has been formulated
	 * @see     	#getDescription()
	 */
	public void removeDescription(Language lang){
		this.description.remove(lang);
	}


//*********************************** CLONE *****************************************/

	/**
	 * Clones <i>this</i> taxon interaction. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> taxon interaction by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {

		try {
			TaxonInteraction result = (TaxonInteraction)super.clone();

			//description
			result.description = new HashMap<Language, LanguageString>();
			for (Language language : getDescriptions().keySet()){
				//TODO clone needed? See also IndividualsAssociation
				LanguageString newLanguageString = (LanguageString)getDescriptions().get(language).clone();
				result.description.put(language, newLanguageString);
			}


			return result;
			//no changes to: taxon2
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}