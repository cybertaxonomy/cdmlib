// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.BookDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.CdDvdDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.GenericDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.JournalDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceBaseDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ThesisDefaultCacheStrategy;


/**
 * The reference type is used to define the type of a {@link ReferenceBase reference}.<BR>
 * When changing the type of a reference one must be careful with handling attached information.
 * E.g. changing the type of a reference from article to book section requires to either exchange
 * the in reference or to change the type of the in reference which may have further consequences.
 * @author a.mueller
 * @created 20.09.2009
 * @version 1.0
 */
@XmlEnum
public enum ReferenceType {
	//0
	@XmlEnumValue("Article")   
	Article("Article", ArticleDefaultCacheStrategy.class),
	//1
	@XmlEnumValue("Book")      
	Book("Book", BookDefaultCacheStrategy.class),
	//2
	@XmlEnumValue("Book Section")  
	BookSection("Book Section", BookSectionDefaultCacheStrategy.class),
	//3
	@XmlEnumValue("CD or DVD")
	CdDvd("CD or DVD", CdDvdDefaultCacheStrategy.class),
	//4
	@XmlEnumValue("Database")
	Database("Database", ReferenceBaseDefaultCacheStrategy.class),
	//5
	@XmlEnumValue("Generic")
	Generic("Generic", GenericDefaultCacheStrategy.class),
	//6
	@XmlEnumValue("Inproceedings")
	InProceedings("In Proceedings", ReferenceBaseDefaultCacheStrategy.class),
	//7
	@XmlEnumValue("Journal")
	Journal("Journal", JournalDefaultCacheStrategy.class),
	//8
	@XmlEnumValue("Map")
	Map("Map", ReferenceBaseDefaultCacheStrategy.class),
	//9
	@XmlEnumValue("Patent")
	Patent("Patent", ReferenceBaseDefaultCacheStrategy.class),
	//10
	@XmlEnumValue("Personal Communication")
	PersonalCommunication("Personal Communication", ReferenceBaseDefaultCacheStrategy.class),
	//11
	@XmlEnumValue("Print Series")
	PrintSeries("Print Series", ReferenceBaseDefaultCacheStrategy.class),
	//12
	@XmlEnumValue("Proceedings")
	Proceedings("Proceedings", ReferenceBaseDefaultCacheStrategy.class),
	//13
	@XmlEnumValue("Report")
	Report("Report", ReferenceBaseDefaultCacheStrategy.class),
	//14
	@XmlEnumValue("Thesis")
	Thesis("Thesis", ThesisDefaultCacheStrategy.class),
	//15
	@XmlEnumValue("Web Page")
	WebPage("Web Page", ReferenceBaseDefaultCacheStrategy.class), 
	//16
	@XmlEnumValue("Printed Unit Base")
	@Deprecated // all references are ReferenceBases this type should not longer be used. Use isPrintedUnit() for tests against this type instead.
	PrintedUnitBase("Printed Unit Base", ReferenceBaseDefaultCacheStrategy.class), 
	//17
	@XmlEnumValue("Publication Base")
	@Deprecated // all references are ReferenceBases this type should not longer be used. Use isPublication() for tests against this type instead.
	PublicationBase("Publication Base", ReferenceBaseDefaultCacheStrategy.class);
	
	
	private String readableString;
	private Class<? extends IReferenceBaseCacheStrategy> cacheStrategy;
	
	private ReferenceType(String defaultString, Class<? extends IReferenceBaseCacheStrategy> cacheStrategy){
		readableString = defaultString;
		this.cacheStrategy = cacheStrategy;
	}
	
	@Transient
	public String getMessage(){
		return getMessage(Language.DEFAULT());
	}
	public String getMessage(Language language){
		//TODO make multi-lingual
		return readableString;
	}

	public IReferenceBaseCacheStrategy getCacheStrategy(){
		switch(this){
		case Article: 
			return ArticleDefaultCacheStrategy.NewInstance();
		case Book:
			return BookDefaultCacheStrategy.NewInstance();
		case BookSection:
			return BookSectionDefaultCacheStrategy.NewInstance();
		case CdDvd:
			return CdDvdDefaultCacheStrategy.NewInstance();
		case Generic:
			return GenericDefaultCacheStrategy.NewInstance();
		case Journal:
			return JournalDefaultCacheStrategy.NewInstance();
		case Thesis:
			return ThesisDefaultCacheStrategy.NewInstance();
		}
		return ReferenceBaseDefaultCacheStrategy.NewInstance();
	}
	
	/**
	 * Returns true if references of this type have volume information.
	 */
	public boolean isVolumeReference(){
		return (this == Article || isPrintedUnit() || this == Generic);
	}
	
	/**
	 * Returns true if references of this type are publications (inheriting from
	 * {@link IPublicationBase}) and therefore have a publisher and a publication place.
	 */
	public boolean isPublication(){
		return (this == CdDvd || this == Database || this == Generic
				|| this == Journal || isPrintedUnit() ||  this == PrintSeries
				|| this == Report  || this == Thesis 
				|| this == WebPage || this == Map);			
	}
	
	/**
	 * Returns true if references of this type are printed units (inheriting from
	 * {@link IPrintedUnitBase}) and therefore may have an editor, an in-series or an string 
	 * representing the series (seriesPart).
	 */
	public boolean isPrintedUnit(){
		return (this == Book || this == Proceedings);
	}
	
	/**
	 * Returns true if references of this type are parts of other references (inheriting from
	 * {@link ISectionBase}) and therefore may have an in-reference and pages.
	 */
	public boolean isSection(){
		return (this == BookSection || this == InProceedings 
				|| isPrintedUnit() || this == Article );
	}
	
}
