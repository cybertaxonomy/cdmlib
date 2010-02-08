/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.common.Language;

import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * This class represents common or vernacular names for {@link Taxon taxa}.
 * Only {@link TaxonDescription taxon descriptions} may contain common names.
 * Common names vary not only according to the {@link Language language} but also sometimes
 * according to {@link TaxonDescription#getGeoScopes() geospatial areas}. Furthermore there might be several
 * distinct common names in one language and in the same geospatial area to
 * designate the same taxon. Therefore using a {@link MultilanguageText multilanguage text}
 * would not have been adequate.
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:17
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommonTaxonName", propOrder = {
    "name",
    "language"
})
@XmlRootElement(name = "CommonTaxonName")
@Entity
@Audited
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
public class CommonTaxonName extends DescriptionElementBase {
	private static final long serialVersionUID = 2643808051976643339L;
	private static final Logger logger = Logger.getLogger(CommonTaxonName.class);
	
	@XmlElement(name = "Name")
	@Field(index = Index.TOKENIZED)
	private String name;
	
	@XmlElement(name = "Language")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded(depth = 2)
	private Language language;

	/**
	 * Class constructor: creates a new empty common name instance.
	 * The corresponding {@link Feature feature} is set to {@link Feature#COMMON_NAME() COMMON_NAME}.
	 */
	protected CommonTaxonName(){
		super(Feature.COMMON_NAME());
	}
	
	/**
	 * Creates a common name instance with the given name string and the given
	 * {@link Language language}. The corresponding {@link Feature feature} is set to
	 * {@link Feature#COMMON_NAME() COMMON_NAME}.
	 * 
	 * @param name		the name string 
	 * @param language	the language of the name string
	 */
	public static CommonTaxonName NewInstance(String name, Language language){
		logger.debug("NewInstance");
		CommonTaxonName result = new CommonTaxonName();
		result.setName(name);
		result.setLanguage(language);
		return result;
	}
	
	
	
	
	/**
	 * Deprecated because {@link Feature feature} should always be {@link Feature#COMMON_NAME() COMMON_NAME}
	 * for all common name instances.
	*/
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#setFeature(eu.etaxonomy.cdm.model.description.Feature)
	 */
	@Override
	@Deprecated 
	public void setFeature(Feature feature) {
		super.setFeature(feature);
	}

	/** 
	 * Returns the {@link Language language} used for <i>this</i> common name.
	 */
	public Language getLanguage(){
		return this.language;
	}
	/** 
	 * @see	#getLanguage()
	 */
	public void setLanguage(Language language){
		this.language = language;
	}

	/** 
	 * Returns the name string of <i>this</i> common name.
	 */
	public String getName(){
		return this.name;
	}

	/** 
	 * @see	#getName()
	 */
	public void setName(String name){
		this.name = name;
	}

}