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

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.common.IEnumTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.NewDefaultReferenceCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.old.ArticleDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.old.BookDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.old.BookSectionDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.old.CdDvdDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.old.GenericDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.old.JournalDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.old.ReferenceDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.old.ThesisDefaultCacheStrategy;


/**
 * The reference type is used to define the type of a {@link Reference reference}.<BR>
 * When changing the type of a reference one must be careful with handling attached information.
 * E.g. changing the type of a reference from article to book section requires to either exchange
 * the in reference or to change the type of the in reference which may have further consequences.
 *
 * @author a.mueller
 * @created 20.09.2009
 */

//TODO hierarchies, see http://dev.e-taxonomy.eu/trac/ticket/3619
@XmlEnum
public enum ReferenceType implements IEnumTerm<ReferenceType>, Serializable{


	/**
	 * A reference of type section is a part-of another reference. Section is a generalized type for all
	 * references which are expected to be part of another reference (e.g. an article which is part of a journal,
	 * a book section which is part of a book) or which may have an in-reference
	 * such as books which may be part of a print series or websites which may be part of other websites.
	 * <BR>
	 * However, section as concrete type should only be used if no more specific type is available.
	 * This is usually the case for parts of other sections such parts of articles, parts of book sections, or
	 * similar cases).
	 *
	 * @see ISectionBase
	 */
	@XmlEnumValue("Section")
	Section(UUID.fromString("98035142-ca82-46c5-bbef-ad225f668644"), "Section", "SEC", null, ReferenceDefaultCacheStrategy.class),

	//0
	/**
	 * Article in a journal.
	 * Article is a specialization of {@link #Section}.
	 */
	@XmlEnumValue("Article")
	Article(UUID.fromString("fddfb343-f652-4f33-b6cb-7c94daa2f1ec"), "Article", "ART", Section, ArticleDefaultCacheStrategy.class),
	//1
	@XmlEnumValue("Book")
	Book(UUID.fromString("9280876c-accb-4c47-873d-46bbf4296f18"), "Book", "BK", Section, BookDefaultCacheStrategy.class),
	//2
	/**
	 * A part in a book, e.g. a chapter.
	 * BookSection is a specialization of {@link #Section}
	 */
	@XmlEnumValue("Book Section")
	BookSection(UUID.fromString("b197435d-deec-46fa-9c66-e0e6c44c57fb"), "Book Section", "BS", Section, BookSectionDefaultCacheStrategy.class),
	//3
	@XmlEnumValue("CD or DVD")
	CdDvd(UUID.fromString("7d7c9f56-d6fd-45aa-852f-b965afe08ec0"), "CD or DVD", "CD", null, CdDvdDefaultCacheStrategy.class),
	//4
	@XmlEnumValue("Database")
	Database(UUID.fromString("a36dbaec-0536-4a20-9fbc-e1b10ba35ea6"), "Database", "DB", null, ReferenceDefaultCacheStrategy.class),
	//5
	@XmlEnumValue("Generic")
	Generic(UUID.fromString("df149dd8-f2b4-421c-b478-acc4cce63f25"), "Generic", "GEN", null, GenericDefaultCacheStrategy.class),
	//6
	@XmlEnumValue("Inproceedings")
	InProceedings(UUID.fromString("a84dae35-6708-4c3d-8bb6-41b989947fa2"), "In Proceedings", "IPR", Section, ReferenceDefaultCacheStrategy.class),
	//7
	@XmlEnumValue("Journal")
	Journal(UUID.fromString("d8675c58-41cd-44fb-86be-e966bd4bc747"), "Journal", "JOU", null, JournalDefaultCacheStrategy.class),
	//8
	@XmlEnumValue("Map")
	Map(UUID.fromString("f4acc990-a277-4d80-9192-bc04be4b1cab"), "Map", "MAP", null, ReferenceDefaultCacheStrategy.class),
	//9
	@XmlEnumValue("Patent")
	Patent(UUID.fromString("e44e0e6b-a721-417c-9b03-01926ea0bf56"), "Patent", "PAT", null, ReferenceDefaultCacheStrategy.class),
	//10
	@XmlEnumValue("Personal Communication")
	PersonalCommunication(UUID.fromString("4ba5607e-1b9d-473c-89dd-8f1c2d27ae50"), "Personal Communication", "PEC", null, ReferenceDefaultCacheStrategy.class),
	//11
	@XmlEnumValue("Print Series")
	PrintSeries(UUID.fromString("d455f30d-2685-4f57-804a-3df5ba4e0888"), "Print Series", "SER", null, ReferenceDefaultCacheStrategy.class),
	//12
	@XmlEnumValue("Proceedings")
	Proceedings(UUID.fromString("cd934865-cb25-41f1-a155-f344ccb0c57f"), "Proceedings", "PRO", Section, ReferenceDefaultCacheStrategy.class),
	//13
	@XmlEnumValue("Report")
	Report(UUID.fromString("4d5459b8-b65b-47cb-9579-2fe7be360d04"), "Report", "REP", null, ReferenceDefaultCacheStrategy.class),
	//14
	@XmlEnumValue("Thesis")
	Thesis(UUID.fromString("cd054393-4f5e-4842-b820-b820e5732d72"), "Thesis", "THE", null, ThesisDefaultCacheStrategy.class),
	//15
	@XmlEnumValue("Web Page")
	WebPage(UUID.fromString("1ed8b0df-0532-40ea-aef6-ee4361341165"), "Web Page", "WEB", null, ReferenceDefaultCacheStrategy.class),

	;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ReferenceType.class);

	private Class<? extends IReferenceCacheStrategy> cacheStrategy;

	private ReferenceType(UUID uuid, String defaultString, String key, ReferenceType parent, Class<? extends IReferenceCacheStrategy> cacheStrategy){
		this.cacheStrategy = cacheStrategy;
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
	}


	public IReferenceCacheStrategy getCacheStrategy(){
//		if (true){
		    return NewDefaultReferenceCacheStrategy.NewInstance();
//		}
//	    switch(this){
//		case Article:
//			return ArticleDefaultCacheStrategy.NewInstance();
//		case Book:
//			return BookDefaultCacheStrategy.NewInstance();
//		case BookSection:
//			return BookSectionDefaultCacheStrategy.NewInstance();
//		case CdDvd:
//			return CdDvdDefaultCacheStrategy.NewInstance();
//		case Generic:
//			return GenericDefaultCacheStrategy.NewInstance();
//		case Journal:
//			return JournalDefaultCacheStrategy.NewInstance();
//		case Thesis:
//			return ThesisDefaultCacheStrategy.NewInstance();
//		case Section:
//			return SectionDefaultCacheStrategy.NewInstance();
//		case WebPage:
//			return WebPageDefaultCacheStrategy.NewInstance();
//        default:
//            return ReferenceDefaultCacheStrategy.NewInstance();
//		}
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
	 * {@link ISection}) and therefore may have an in-reference and pages.
	 */
	public boolean isSection(){
//		return (this == BookSection || this == InProceedings
//				|| isPrintedUnit() || this == Article );
		return this == Section || isKindOf(Section);
	}

// *************************** DELEGATE **************************************/

	private static EnumeratedTermVoc<ReferenceType> delegateVoc;
	private IEnumTerm<ReferenceType> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(ReferenceType.class);
	}

	@Override
	public String getKey(){return delegateVocTerm.getKey();}

	@Override
    public String getMessage(){return delegateVocTerm.getMessage();}

	@Override
    public String getMessage(Language language){return delegateVocTerm.getMessage(language);}

	@Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

	@Override
    public ReferenceType getKindOf() {return delegateVocTerm.getKindOf();}

	@Override
    public Set<ReferenceType> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(ReferenceType ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<ReferenceType> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

	public static ReferenceType getByKey(String key){return delegateVoc.getByKey(key);}
    public static ReferenceType getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}


}
