// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.occurrence.CollectionDefaultCacheStrategy;

/**
 * Instances of this class represent a collection for primary biodiversity data.
 * Collections may be part of other collections and may belong to an institution.
 * Collection inherits from
 *
 * @author m.doering
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
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.Collection")
@Audited
@Configurable
@Table(appliesTo="Collection", indexes = { @org.hibernate.annotations.Index(name = "collectionTitleCacheIndex", columnNames = { "titleCache" }) })
public class Collection extends IdentifiableMediaEntity<IIdentifiableEntityCacheStrategy<Collection>> implements Cloneable{
	private static final long serialVersionUID = -7833674897174732255L;
	private static final Logger logger = Logger.getLogger(Collection.class);

	@XmlElement(name = "Code")
	@Field(analyze = Analyze.NO)
    //TODO Val #3379
//	@NullOrNotEmpty
	@Length(max = 255)
	private String code;

	@XmlElement(name = "CodeStandard")
	@Field(analyze = Analyze.NO)
    //TODO Val #3379
//	@NullOrNotEmpty
	@Length(max = 255)
	private String codeStandard;

	@XmlElement(name = "Name")
	@Field
    //TODO Val #3379
//	@NullOrNotEmpty
	@Length(max = 255)
	private String name;

	@XmlElement(name = "TownOrLocation")
	@Field
    //TODO Val #3379
//	@NullOrNotEmpty
	@Length(max = 255)
	private String townOrLocation;

	@XmlElement(name = "Institution")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@IndexedEmbedded
	private Institution institute;

	@XmlElement(name = "SuperCollection")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Collection superCollection;

// ************** FACTORY METHODS *************************/

	/**
	 * Factory method
	 * @return
	 */
	public static Collection NewInstance(){
		return new Collection();
	}

// ******************** CONSTRUCTOR *************************/

	/**
	 * Constructor
	 */
	protected Collection() {
		super();
		this.cacheStrategy = new CollectionDefaultCacheStrategy();
	}

// ******************* GETTER / SETTER ************************/



	/**
	 * The {@link Institution institution} this collection belongs to.
	 * @see #getSuperCollection()
	 * @return the institute
	 */
	public Institution getInstitute(){
		return this.institute;
	}

	/**
	 * @see #getInstitute()
	 * @param institute    institute
	 */
	public void setInstitute(Institution institute){
		this.institute = institute;
	}

	/**
	 * The code for this collection. The standard this code belongs to
	 * is given by the {@link #getCodeStandard() code standard}.
	 * The code is NOT the {@link #getName()name} of the collection.
	 *
	 * @see #getCodeStandard()
	 * @see #getName()
	 * @return the code
	 */
	public String getCode(){
		return this.code;
	}

	/**
	 * @see #getCode()
	 * @param code the code
	 */
	public void setCode(String code){
		this.code = StringUtils.isBlank(code)? null : code;
	}

	/**
	 * The standard used for the given {@link #getCode() collection code}
	 * @return the code standard
	 */
	public String getCodeStandard(){
		return this.codeStandard;
	}

	/**
	 * @see #getCodeStandard()
	 * @see #getCode()
	 * @param codeStandard    codeStandard
	 */
	public void setCodeStandard(String codeStandard){
		this.codeStandard = StringUtils.isBlank(codeStandard)? null : codeStandard;
	}

	/**
	 * The name of this collection
	 * @return the name
	 */
	public String getName(){
		return this.name;
	}

	/**
	 * @see #getName()
	 * @param name    name
	 */
	public void setName(String name){
		this.name = StringUtils.isBlank(name)? null : name;
	}

	/**
	 * The town or location where this collection is to be found.
	 * @return the town or location
	 */
	public String getTownOrLocation(){
		return this.townOrLocation;
	}

	/**
	 * @see #getTownOrLocation
	 * @param townOrLocation    townOrLocation
	 */
	public void setTownOrLocation(String townOrLocation){
		this.townOrLocation = StringUtils.isBlank(townOrLocation)? null : townOrLocation;
	}

	/**
	 * The collection <code>this</code> collection is part of.
	 * @return
	 */
	public Collection getSuperCollection() {
		return superCollection;
	}

	/**
	 * @see #getSuperCollection()
	 * @param superCollection
	 */
	public void setSuperCollection(Collection superCollection) {
		this.superCollection = superCollection;
	}


//*********** CLONE **********************************/

	/**
	 * Clones <i>this</i> collection. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> collection
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link IdentifiableMediaEntity IdentifiableMediaEntity}.
	 *
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
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



// *********************** toString() **************************************

	@Override
	public String toString() {
		if (StringUtils.isNotBlank(code) || StringUtils.isNotBlank(name)){
			return "Collection [id= "+ getId() +", + code=" + CdmUtils.Nz(code) + ", name=" + CdmUtils.Nz(name) + "]";
		}else{
			return super.toString();
		}
	}
}