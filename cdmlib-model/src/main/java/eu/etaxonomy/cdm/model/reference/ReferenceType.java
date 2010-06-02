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
 * @author a.mueller
 * @created 20.09.2009
 * @version 1.0
 */
@XmlEnum
public enum ReferenceType {
	@XmlEnumValue("Article")
	Article("Article", ArticleDefaultCacheStrategy.class),
	@XmlEnumValue("Book")
	Book("Book", BookDefaultCacheStrategy.class),
	@XmlEnumValue("Book Section")
	BookSection("Book Section", BookSectionDefaultCacheStrategy.class),
	@XmlEnumValue("CD or DVD")
	CdDvd("CD or DVD", CdDvdDefaultCacheStrategy.class),
	@XmlEnumValue("Database")
	Database("Database", ReferenceBaseDefaultCacheStrategy.class),
	@XmlEnumValue("Generic")
	Generic("Generic", GenericDefaultCacheStrategy.class),
	@XmlEnumValue("Inproceedings")
	InProceedings("Inproceedings", ReferenceBaseDefaultCacheStrategy.class),
	@XmlEnumValue("Journal")
	Journal("Journal", JournalDefaultCacheStrategy.class),
	@XmlEnumValue("Map")
	Map("Map", ReferenceBaseDefaultCacheStrategy.class),
	@XmlEnumValue("Patent")
	Patent("Patent", ReferenceBaseDefaultCacheStrategy.class),
	@XmlEnumValue("Personal Communication")
	PersonalCommunication("Personal Communication", ReferenceBaseDefaultCacheStrategy.class),
	@XmlEnumValue("Print Series")
	PrintSeries("Print Series", ReferenceBaseDefaultCacheStrategy.class),
	@XmlEnumValue("Proceedings")
	Proceedings("Proceedings", ReferenceBaseDefaultCacheStrategy.class),
	@XmlEnumValue("Report")
	Report("Report", ReferenceBaseDefaultCacheStrategy.class),
	@XmlEnumValue("Thesis")
	Thesis("Thesis", ThesisDefaultCacheStrategy.class),
	@XmlEnumValue("Web Page")
	WebPage("Web Page", ReferenceBaseDefaultCacheStrategy.class), 
	@XmlEnumValue("Printed Unit Base")
	@Deprecated // all references are ReferenceBases this type should not longer be used. Use isPrintedUnit() for tests against this type instead.
	PrintedUnitBase("Printed Unit Base", ReferenceBaseDefaultCacheStrategy.class), 
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
	
	public boolean isVolumeReference(){
		return (isPrintedUnit() || this == Generic || this == Article);
	}
	
	public boolean isPublication(){
		return (this == CdDvd || this == Database || this == Generic
				|| this == Journal || this == Map || this == Book
				|| this == Proceedings || this == PrintSeries
				|| this == Report || this == Thesis || this == WebPage);			
	}
	
	public boolean isPrintedUnit(){
		return (this == Book || this == Proceedings);
	}
	
	public boolean isSection(){
		return (this == BookSection || this == InProceedings);
	}
	
}
