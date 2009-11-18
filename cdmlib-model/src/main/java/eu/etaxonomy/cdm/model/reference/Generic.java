/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.strategy.cache.reference.GenericDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy;

/**
 * This class represents all references which cannot be clearly assigned to a
 * particular {@link StrictReferenceBase reference} subclass. Therefore attributes which are
 * characteristic for a unique reference subclass are not necessary here.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * terms (from PublicationTypeTerm): <ul>
 * <li> "Generic"
 * <li> "Artwork"
 * <li> "AudiovisualMaterial"
 * <li> "ComputerProgram"
 * <li> "Determination"
 * <li> "Commentary"
 * <li> "SubReference"
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:26
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Generic", propOrder = {
//		"editor",
//		"volume",
//		"pages",
//		"series"
})
@XmlRootElement(name = "Generic")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Configurable
@Deprecated
public class Generic extends PublicationBase<INomenclaturalReferenceCacheStrategy<Generic>> implements INomenclaturalReference, IVolumeReference, Cloneable {
	private static final long serialVersionUID = -2547067957118035042L;
	private static final Logger logger = Logger.getLogger(Generic.class);

//    @XmlElement(name = "Editor")
//    @Field(index=Index.TOKENIZED)
//	private String editor;
//	
//    @XmlElement(name = "Series")
//    @Field(index=Index.TOKENIZED)
//	private String series;
//	
//    @XmlElement(name = "Volume")
//    @Field(index=Index.TOKENIZED)
//	private String volume;
//	
//    @XmlElement(name = "Pages")
//    @Field(index=Index.TOKENIZED)
//	private String pages;
//	
	/** 
	 * Class constructor: creates a new empty generic reference instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.reference.GenericDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.GenericDefaultCacheStrategy
	 */
	public Generic(){
		super();
		this.type = ReferenceType.Generic;
		this.cacheStrategy = GenericDefaultCacheStrategy.NewInstance();
	}
	
	
	/** 
	 * Creates a new empty generic reference instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.reference.GenericDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #Generic()
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.GenericDefaultCacheStrategy
	 */
	public static Generic NewInstance(){
		return new Generic();
	}
	
	/**
	 * Returns the string representing the name of the editor of <i>this</i>
	 * generic reference. An editor is mostly a person (team) who assumed the
	 * responsibility for the content of the publication as a whole without
	 * being the author of this content.<BR>
	 * If there is an editor then the generic reference must be some
	 * kind of {@link PrintedUnitBase physical printed unit}.
	 * 
	 * @return  the string identifying the editor of <i>this</i>
	 * 			generic reference
	 * @see 	#getPublisher()
	 */
	public String getEditor(){
		return this.editor;
	}

	/**
	 * @see #getEditor()
	 */
	public void setEditor(String editor){
		this.editor = editor;
	}

	/**
	 * Returns the string representing the series (for instance for books or
	 * within journals) - and series part - in which <i>this</i> generic reference
	 * was published.<BR>
	 * If there is a series then the generic reference must be some
	 * kind of {@link PrintedUnitBase physical printed unit} or an {@link Article article}.
	 * 
	 * @return  the string identifying the series for <i>this</i>
	 * 			generic reference
	 */
	public String getSeries(){
		return this.series;
	}

	/**
	 * @see #getSeries()
	 */
	public void setSeries(String series){
		this.series = series;
	}

	/**
	 * Returns the string representing the volume (for instance for books or
	 * within journals) in which <i>this</i> generic reference was published.<BR>
	 * If there is a volume then the generic reference must be some
	 * kind of {@link PrintedUnitBase physical printed unit} or an {@link Article article}.
	 * 
	 * @return  the string identifying the volume for <i>this</i>
	 * 			generic reference
	 */
	public String getVolume(){
		return this.volume;
	}

	/**
	 * @see #getVolume()
	 */
	public void setVolume(String volume){
		this.volume = volume;
	}

	/**
	 * Returns the string representing the page(s) where the content of
	 * <i>this</i> generic reference is located.<BR>
	 * If there is a pages information then the generic reference must be some
	 * kind of {@link PrintedUnitBase physical printed unit} or an {@link Article article}.
	 * 
	 * @return  the string containing the pages corresponding to <i>this</i>
	 * 			generic reference
	 */
	public String getPages(){
		return this.pages;
	}

	/**
	 * @see #getPages()
	 */
	public void setPages(String pages){
		this.pages = pages;
	}

	/**
	 * Returns a formatted string containing the entire reference citation,
	 * including authors, corresponding to <i>this</i> generic reference.<BR>
	 * This method overrides the generic and inherited getCitation method
	 * from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see  #getNomenclaturalCitation(String)
	 * @see  StrictReferenceBase#getCitation()
	 */
//	@Override
//	@Transient
//	public String getCitation(){
//		return nomRefBase.getCitation();
//	}

	/**
	 * Returns a formatted string containing the entire citation used for
	 * nomenclatural purposes based on <i>this</i> generic reference - including
	 * (abbreviated) title but not authors - and on the given
	 * details.
	 * 
	 * @param  microReference	the string with the details (generally pages)
	 * 							within <i>this</i> generic reference
	 * @return					the formatted string representing the
	 * 							nomenclatural citation
	 * @see  					#getCitation()
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for "+ this.getClass() + ": " + this.getUuid());
			return null;
		}else{
			return cacheStrategy.getNomenclaturalCitation(this,microReference);
		}
	}

	/**
	 (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.ReferenceBase#generateTitle()
	 * Generates, according to the {@link strategy.cache.reference.GenericDefaultCacheStrategy default cache strategy}
	 * assigned to <i>this</i> generic reference, a string that identifies <i>this</i>
	 * reference and returns it. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited generateTitle method
	 * from {@link ReferenceBase ReferenceBase}.
	 *
	 * @return  the string identifying <i>this</i> generic reference
	 * @see  	#getCitation()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
	 */
//	@Override
//	public String generateTitle(){
//		return nomRefBase.generateTitle();
//	}
	
	


	/** 
	 * Clones <i>this</i> generic reference. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> generic
	 * reference by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Generic clone(){
		Generic result = (Generic)super.clone();
		result.cacheStrategy = GenericDefaultCacheStrategy.NewInstance();
		//no changes to: editor, pages, placePublished,publisher, series, volume
		return result;
	}

}