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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IDefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.media.Media;
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
 * The reference type is used to define the type of a {@link Reference reference}.<BR>
 * When changing the type of a reference one must be careful with handling attached information.
 * E.g. changing the type of a reference from article to book section requires to either exchange
 * the in reference or to change the type of the in reference which may have further consequences.
 * @author a.mueller
 * @created 20.09.2009
 * @version 1.0
 */
@XmlEnum
public enum ReferenceType implements IDefinedTerm<ReferenceType>, Serializable{
	//0
	@XmlEnumValue("Article")
	Article(UUID.fromString("fddfb343-f652-4f33-b6cb-7c94daa2f1ec"), "Article", ArticleDefaultCacheStrategy.class),
	//1
	@XmlEnumValue("Book")
	Book(UUID.fromString("9280876c-accb-4c47-873d-46bbf4296f18"), "Book", BookDefaultCacheStrategy.class),
	//2
	@XmlEnumValue("Book Section")
	BookSection(UUID.fromString("b197435d-deec-46fa-9c66-e0e6c44c57fb"), "Book Section", BookSectionDefaultCacheStrategy.class),
	//3
	@XmlEnumValue("CD or DVD")
	CdDvd(UUID.fromString("7d7c9f56-d6fd-45aa-852f-b965afe08ec0"), "CD or DVD", CdDvdDefaultCacheStrategy.class),
	//4
	@XmlEnumValue("Database")
	Database(UUID.fromString("a36dbaec-0536-4a20-9fbc-e1b10ba35ea6"), "Database", ReferenceBaseDefaultCacheStrategy.class),
	//5
	@XmlEnumValue("Generic")
	Generic(UUID.fromString("df149dd8-f2b4-421c-b478-acc4cce63f25"), "Generic", GenericDefaultCacheStrategy.class),
	//6
	@XmlEnumValue("Inproceedings")
	InProceedings(UUID.fromString("a84dae35-6708-4c3d-8bb6-41b989947fa2"), "In Proceedings", ReferenceBaseDefaultCacheStrategy.class),
	//7
	@XmlEnumValue("Journal")
	Journal(UUID.fromString("d8675c58-41cd-44fb-86be-e966bd4bc747"), "Journal", JournalDefaultCacheStrategy.class),
	//8
	@XmlEnumValue("Map")
	Map(UUID.fromString("f4acc990-a277-4d80-9192-bc04be4b1cab"), "Map", ReferenceBaseDefaultCacheStrategy.class),
	//9
	@XmlEnumValue("Patent")
	Patent(UUID.fromString("e44e0e6b-a721-417c-9b03-01926ea0bf56"), "Patent", ReferenceBaseDefaultCacheStrategy.class),
	//10
	@XmlEnumValue("Personal Communication")
	PersonalCommunication(UUID.fromString("4ba5607e-1b9d-473c-89dd-8f1c2d27ae50"), "Personal Communication", ReferenceBaseDefaultCacheStrategy.class),
	//11
	@XmlEnumValue("Print Series")
	PrintSeries(UUID.fromString("d455f30d-2685-4f57-804a-3df5ba4e0888"), "Print Series", ReferenceBaseDefaultCacheStrategy.class),
	//12
	@XmlEnumValue("Proceedings")
	Proceedings(UUID.fromString("cd934865-cb25-41f1-a155-f344ccb0c57f"), "Proceedings", ReferenceBaseDefaultCacheStrategy.class),
	//13
	@XmlEnumValue("Report")
	Report(UUID.fromString("4d5459b8-b65b-47cb-9579-2fe7be360d04"), "Report", ReferenceBaseDefaultCacheStrategy.class),
	//14
	@XmlEnumValue("Thesis")
	Thesis(UUID.fromString("cd054393-4f5e-4842-b820-b820e5732d72"), "Thesis", ThesisDefaultCacheStrategy.class),
	//15
	@XmlEnumValue("Web Page")
	WebPage(UUID.fromString("1ed8b0df-0532-40ea-aef6-ee4361341165"), "Web Page", ReferenceBaseDefaultCacheStrategy.class);

	private static final Logger logger = Logger.getLogger(ReferenceType.class);

	private String readableString;
	private Class<? extends IReferenceBaseCacheStrategy> cacheStrategy;
	private UUID uuid;

	private ReferenceType(UUID uuid, String defaultString, Class<? extends IReferenceBaseCacheStrategy> cacheStrategy){
		this.uuid = uuid;
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
        default:
            return ReferenceBaseDefaultCacheStrategy.NewInstance();
		}
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


	@Override
    public ReferenceType readCsvLine(Class<ReferenceType> termClass,
			List<String> csvLine, java.util.Map<UUID, DefinedTermBase> terms) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
    public void writeCsvLine(CSVWriter writer, ReferenceType term) {
		logger.warn("write csvLine not yet implemented");
	}


	@Override
    public UUID getUuid() {
		return this.uuid;
	}


	@Override
    public ReferenceType getByUuid(UUID uuid) {
		for (ReferenceType referenceType : ReferenceType.values()){
			if (referenceType.getUuid().equals(uuid)){
				return referenceType;
			}
		}
		return null;
	}


	@Override
    public ReferenceType getKindOf() {
		return null;
	}


	@Override
    public Set<ReferenceType> getGeneralizationOf() {
		return new HashSet<ReferenceType>();
	}


	@Override
    public ReferenceType getPartOf() {
		return null;
	}


	@Override
    public Set<ReferenceType> getIncludes() {
		return new HashSet<ReferenceType>();
	}


	@Override
    public Set<Media> getMedia() {
		return new HashSet<Media>();
	}
	
	@Override
	public String getIdInVocabulary() {
		return this.toString();
	}

	@Override
	public void setIdInVocabulary(String idInVocabulary) {
		//not applicable
	}

}
