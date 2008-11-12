/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.media.IdentifyableMediaEntity;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:16
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Collection", propOrder = {
	"name",
    "code",
    "codeStandard",
    "townOrLocation",
    "institute",
    "superCollection"
})
@XmlRootElement(name = "Collection")
@Entity
@Table(appliesTo="Collection", indexes = { @Index(name = "collectionTitleCacheIndex", columnNames = { "titleCache" }) })
public class Collection extends IdentifyableMediaEntity implements Cloneable{
	private static final Logger logger = Logger.getLogger(Collection.class);
	
	@XmlElement(name = "Code")
	private String code;
	
	@XmlElement(name = "CodeStandard")
	private String codeStandard;
	
	@XmlElement(name = "Name")
	private String name;
	
	@XmlElement(name = "TownOrLocation")
	private String townOrLocation;
	
	@XmlElement(name = "Institution")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Institution institute;
	
	@XmlElement(name = "SuperCollection")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Collection superCollection;
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static Collection NewInstance(){
		return new Collection();
	}
	
	/**
	 * Constructor
	 */
	protected Collection() {
		super();
	}

	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Institution getInstitute(){
		return this.institute;
	}

	/**
	 * 
	 * @param institute    institute
	 */
	public void setInstitute(Institution institute){
		this.institute = institute;
	}

	public String getCode(){
		return this.code;
	}

	/**
	 * 
	 * @param code    code
	 */
	public void setCode(String code){
		this.code = code;
	}

	public String getCodeStandard(){
		return this.codeStandard;
	}

	/**
	 * 
	 * @param codeStandard    codeStandard
	 */
	public void setCodeStandard(String codeStandard){
		this.codeStandard = codeStandard;
	}

	public String getName(){
		return this.name;
	}

	/**
	 * 
	 * @param name    name
	 */
	public void setName(String name){
		this.name = name;
	}

	public String getTownOrLocation(){
		return this.townOrLocation;
	}

	/**
	 * 
	 * @param townOrLocation    townOrLocation
	 */
	public void setTownOrLocation(String townOrLocation){
		this.townOrLocation = townOrLocation;
	}

	@Override
	public String generateTitle(){
		return "";
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Collection getSuperCollection() {
		return superCollection;
	}

	public void setSuperCollection(Collection superCollection) {
		this.superCollection = superCollection;
	}

	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> collection. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> collection
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link IdentifyableMediaEntity IdentifyableMediaEntity}.
	 * 
	 * @see eu.etaxonomy.cdm.model.media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Collection clone(){
		try{
			Collection result = (Collection)super.clone();
			//superCollection
			result.setSuperCollection(this.superCollection);
			//institute
			result.setInstitute(this.institute);
			//no changes to: code, codeStandard, name, townOrLocation
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
		
	
	
}