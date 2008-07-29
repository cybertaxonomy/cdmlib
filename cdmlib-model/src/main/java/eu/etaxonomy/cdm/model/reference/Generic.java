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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.cache.reference.GenericDefaultCacheStrategy;

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
@Entity
public class Generic extends StrictReferenceBase implements INomenclaturalReference, Cloneable {
	static Logger logger = Logger.getLogger(Generic.class);
	private String publisher;
	private String placePublished;
	private String editor;
	private String series;
	private String volume;
	private String pages;
	private NomenclaturalReferenceHelper nomRefBase = NomenclaturalReferenceHelper.NewInstance(this);

	
	/** 
	 * Class constructor: creates a new empty generic reference instance
	 * only containing the {@link strategy.cache.reference.GenericDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see strategy.cache.reference.GenericDefaultCacheStrategy
	 */
	public Generic(){
		super();
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
	 * Returns the string representing the name of the publisher of <i>this</i>
	 * generic reference. A publisher is mostly an institution or a private
	 * company which assumed the global responsibility for the publication
	 * process.<BR>
	 * If there is a publisher then the generic reference must be some kind of
	 * {@link PublicationBase publication}.
	 * 
	 * @return  the string identifying the publisher of <i>this</i>
	 * 			generic reference
	 * @see 	#getEditor()
	 */
	public String getPublisher(){
		return this.publisher;
	}

	/**
	 * @see #getPublisher()
	 */
	public void setPublisher(String publisher){
		this.publisher = publisher;
	}

	/**
	 * Returns the string representing the name of the place (mostly the city)
	 * where <i>this</i> generic reference has been published.<BR>
	 * If there is a published place then the generic reference must be some
	 * kind of {@link PublicationBase publication}.
	 * 
	 * @return  the string identifying the publication place of <i>this</i>
	 * 			generic reference
	 */
	public String getPlacePublished(){
		return this.placePublished;
	}

	/**
	 * @see #getPlacePublished()
	 */
	public void setPlacePublished(String placePublished){
		this.placePublished = placePublished;
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
	 * including authors, corresponding to <i>this</i> generic reference.
	 * 
	 * @see  NomenclaturalReferenceHelper#getCitation()
	 */
	@Transient
	public String getCitation(){
		return nomRefBase.getCitation();
	}

	/**
	 * Returns a formatted string containing the entire citation used for
	 * nomenclatural purposes based on <i>this</i> generic reference - including
	 * (abbreviated) title and (abbreviated) author string - and on the given
	 * details.
	 * 
	 * @param  microReference	the string with the details (generally pages)
	 * 							within t<i>this</i> generic reference
	 * @return					the formatted string representing the
	 * 							nomenclatural citation
	 * @see  					NomenclaturalReferenceHelper#getNomenclaturalCitation(String)
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		return nomRefBase.getNomenclaturalCitation(microReference);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.ReferenceBase#generateTitle()
	 */
	/**
	 * Generates, according to the {@link strategy.cache.reference.GenericDefaultCacheStrategy default cache strategy}
	 * assigned to <i>this</i> generic reference, a string that identifies <i>this</i>
	 * reference and returns it. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> generic reference
	 * @see  	#getCitation()
	 * @see  	NomenclaturalReferenceHelper#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 */
	@Override
	public String generateTitle(){
		return nomRefBase.generateTitle();
	}
	
//*********** CLONE **********************************/	
	

	/** 
	 * Clones <i>this</i> generic reference. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> generic
	 * reference by modifying only some of the attributes.<BR>
	 * This method overrides the {@link StrictReferenceBase#clone() method} from StrictReferenceBase.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	public Generic clone(){
		Generic result = (Generic)super.clone();
		//no changes to: editor, pages, placePublished,publisher, series, volume
		return result;
	}

}