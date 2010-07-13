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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;

/**
 * This (abstract) class represents printed {@link PublicationBase published references} which
 * are recurrent products of publishing companies or of research organizations.
 * In this case it is generally possible to distinguish authors, editors and
 * publishers. 
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:45
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PrintedUnitBase", propOrder = {
//		"editor",	
//		"volume",
//		"pages",
//		"inSeries",
//		"seriesPart"
})
@XmlRootElement(name = "PrintedUnitBase")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Deprecated
public abstract class PrintedUnitBase<S extends IReferenceBaseCacheStrategy> extends PublicationBase<S> implements IVolumeReference{
	private static final long serialVersionUID = 7263496796924430088L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PrintedUnitBase.class);
	
//    @XmlElement(name = "Editor")
//    @Field(index=Index.TOKENIZED)
//	private String editor;
//	
//    @XmlElement(name = "Volume")
//    @Field(index=Index.TOKENIZED)
//	private String volume;
//	
//    @XmlElement(name = "Pages")
//    @Field(index=Index.TOKENIZED)
//	private String pages;
//	
//    @XmlElement(name = "InSeries")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
//    @ManyToOne(fetch = FetchType.LAZY)
//    @IndexedEmbedded
//    @Cascade(CascadeType.SAVE_UPDATE)
//	private PrintSeries inSeries;
//	
//    @XmlElement(name = "SeriesPart")
//    @Field(index=Index.TOKENIZED)
//	private String seriesPart;

	/**
	 * Returns the printed series <i>this</i> printed unit belongs to.
	 * 
	 * @return  printed series
	 * @see 	PrintSeries
	 */
	@Transient
	public PrintSeries getInSeries(){
		if (inReference == null){
			return null;
		}
		if (! this.inReference.isInstanceOf(PrintSeries.class)){
			throw new IllegalStateException("The in-reference of a printed unit base may only be a PrintSeries");
		}
		return CdmBase.deproxy(this.inReference,PrintSeries.class);
	}

	/**
	 * @see #getInSeries()
	 */
	public void setInSeries(PrintSeries inSeries){
		this.inReference = inSeries;
	}

	/**
	 * Returns the string representing the name of the editor of <i>this</i>
	 * printed unit. An editor is mostly a person (team) who assumed the
	 * responsibility for the content of the publication as a whole without
	 * being the author of this content.<BR>
	 * 
	 * @return  the string identifying the editor of <i>this</i>
	 * 			printed unit
	 * @see 	PublicationBase#getPublisher()
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
	 * Returns the string representing the volume of <i>this</i> printed unit.<BR>
	 * 
	 * @return  the string identifying the volume of <i>this</i>
	 * 			printed unit
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
	 * Returns the string representing the pages extent of <i>this</i> printed unit.
	 * 
	 * @return  the pages string
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
	 * Returns the string representing the series part identifying <i>this</i>
	 * printed unit within the the {@link PrintSeries printed series} it belongs to.
	 * 
	 * @return  the string identifying the series part for <i>this</i>
	 * 			printed unit
	 */
	public String getSeriesPart() {
		return seriesPart;
	}

	/**
	 * @see #getSeriesPart()
	 */
	public void setSeriesPart(String seriesPart) {
		this.seriesPart = seriesPart;
	}




	/** 
	 * Clones <i>this</i> printed unit. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> printed unit
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link PublicationBase PublicationBase}.
	 * 
	 * @see PublicationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		PrintedUnitBase result = (PrintedUnitBase)super.clone();
		//no changes to: editor, inSeries, pages, volume, seriesPart
		return result;
	}
}