// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.ISectionBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 16.02.2010
 *
 */
public final class PesiTransformer {
	private static final Logger logger = Logger.getLogger(PesiTransformer.class);

	// References
	public static int REF_ARTICLE_IN_PERIODICAL = 1;
	public static int REF_PART_OF_OTHER = 2;
	public static int REF_BOOK = 3;
	public static int REF_DATABASE = 4;
	public static int REF_INFORMAL = 5;
	public static int REF_NOT_APPLICABLE = 6;
	public static int REF_WEBSITE = 7;
	public static int REF_PUBLISHED = 8;
	public static int REF_JOURNAL = 9;
	public static int REF_UNRESOLVED = 10;
	public static int REF_PUBLICATION = 11;

	public static String REF_STR_ARTICLE_IN_PERIODICAL = "Article in periodical";
	public static String REF_STR_PART_OF_OTHER = "Part of other";
	public static String REF_STR_BOOK = "Book";
	public static String REF_STR_DATABASE = "Database";
	public static String REF_STR_INFORMAL = "Informal";
	public static String REF_STR_NOT_APPLICABLE = "Not applicable";
	public static String REF_STR_WEBSITE = "Website";
	public static String REF_STR_PUBLISHED = "Published";
	public static String REF_STR_JOURNAL = "Journal";
	public static String REF_STR_UNRESOLVED = "Unresolved";
	public static String REF_STR_PUBLICATION = "Publication";
	
	// Ranks
//	public static int RANK_KINGDOM = 10; x
//	public static int RANK_SUBKINGDOM = 20;
//	public static int RANK_SUPERPHYLUM = 23;
//	public static int RANK_PHYLUM = 30;
//	public static int RANK_SUBPHYLUM = 40;
//	public static int RANK_INFRAPHYLUM = 45;
//	public static int RANK_SUPERCLASS = 50;
//	public static int RANK_CLASS = 60;
//	public static int RANK_SUBCLASS = 70;
//	public static int RANK_INFRACLASS = 80;
//	public static int RANK_SUPERORDER = 90;
//	public static int RANK_ORDER = 100;
//	public static int RANK_SUBORDER = 110;
//	public static int RANK_INFRAORDER = 120;
//	public static int RANK_SECTION = 121;
//	public static int RANK_SUBSECTION = 122;
//	public static int RANK_SUPERFAMILY = 130;
//	public static int RANK_FAMILY = 140;
//	public static int RANK_SUBFAMILY = 150;
//	public static int RANK_TRIBE = 160;
//	public static int RANK_SUBTRIBE = 170;
//	public static int RANK_GENUS = 180;
//	public static int RANK_SUBGENUS = 190;
//	public static int RANK_SPECIES = 220;
//	public static int RANK_SUBSPECIES = 230;
//	public static int RANK_NATIO = 235;
//	public static int RANK_VARIETY = 240;
//	public static int RANK_SUBVARIETY = 250;
//	public static int RANK_FORMA = 260;

	// Kingdoms
	public static int KINGDOM_NULL = 0;
	public static int KINGDOM_ANIMALIA = 2;
	public static int KINGDOM_PLANTAE = 3;
	public static int KINGDOM_FUNGI = 4;
	public static int KINGDOM_PROTOCTISTA = 5;
	public static int KINGDOM_MONERA = 6;

	// RelTaxonQualifier
	public static int IS_BASIONYM_FOR = 1;
	public static int IS_LATER_HOMONYM_OF = 2;
	public static int IS_REPLACED_SYNONYM_FOR = 3;
	public static int IS_VALIDATION_OF = 4;
	public static int IS_LATER_VALIDATION_OF = 5;
	public static int IS_TYPE_OF = 6;
	public static int IS_CONSERVED_TYPE_OF = 7;
	public static int IS_REJECTED_TYPE_OF = 8;
	public static int IS_FIRST_PARENT_OF = 9;
	public static int IS_SECOND_PARENT_OF = 10;
	public static int IS_FEMALE_PARENT_OF = 11;
	public static int IS_MALE_PARENT_OF = 12;
	public static int IS_CONSERVED_AGAINST = 13;
	public static int IS_REJECTED_IN_FAVOUR_OF = 14;
	public static int IS_TREATED_AS_LATER_HOMONYM_OF = 15;
	public static int IS_ORTHOGRAPHIC_VARIANT_OF = 16;
	public static int IS_ALTERNATIVE_NAME_FOR = 17;
	public static int HAS_SAME_TYPE_AS = 18;
	public static int IS_LECTOTYPE_OF = 61;
	public static int TYPE_NOT_DESIGNATED = 62;
	public static int IS_TAXONOMICALLY_INCLUDED_IN = 101;
	public static int IS_SYNONYM_OF = 102;
	public static int IS_MISAPPLIED_NAME_FOR = 103;
	public static int IS_PRO_PARTE_SYNONYM_OF = 104;
	public static int IS_PARTIAL_SYNONYM_OF = 105;
	public static int IS_HETEROTYPIC_SYNONYM_OF = 106;
	public static int IS_HOMOTYPIC_SYNONYM_OF = 107;
	public static int IS_PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF = 201;
	public static int IS_PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF = 202;
	public static int IS_PARTIAL_AND_HOMOTYPIC_SYNONYM_OF = 203;
	public static int IS_PARTIAL_AND_HETEROTYPIC_SYNONYM_OF = 204;

	/**
	 * Returns the {@link SourceCategory SourceCategory} representation of the given {@link ReferenceType ReferenceType} in PESI.
	 * @param reference The {@link ReferenceBase ReferenceBase}.
	 * @return The {@link SourceCategory SourceCategory} representation in PESI.
	 */
	public static Integer reference2SourceCategoryFK(ReferenceBase<?> reference) {
		if (reference == null){
			return null;
		} else if (reference.getType().equals(ReferenceType.Article)) {
			return REF_ARTICLE_IN_PERIODICAL;
		} else if (reference instanceof ISectionBase) {
			return REF_PART_OF_OTHER;
		} else if (reference.getType().equals(ReferenceType.Book)) {
			return REF_BOOK;
		} else if (reference.getType().equals(ReferenceType.Database)) {
			return REF_DATABASE;
		} else if (reference.getType().equals(ReferenceType.WebPage)) {
			return REF_WEBSITE;
		} else if (reference.getType().equals(ReferenceType.CdDvd)) {
			return REF_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.Journal)) {
			return REF_JOURNAL;
		} else if (reference.getType().equals(ReferenceType.Generic)) {
			return REF_UNRESOLVED;
		} else if (reference.getType().equals(ReferenceType.PrintSeries)) {
			return REF_PUBLISHED;
		} else if (reference.getType().equals(ReferenceType.Proceedings)) {
			return REF_PUBLISHED;
		} else if (reference.getType().equals(ReferenceType.Patent)) {
			return REF_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.PersonalCommunication)) {
			return REF_INFORMAL;
		} else if (reference.getType().equals(ReferenceType.Report)) {
			return REF_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.Thesis)) {
			return REF_NOT_APPLICABLE;
		} else {
			//TODO Figure out a way to handle this gracefully.
			logger.warn("Reference type not yet supported in PESI: "+ reference.getClass().getSimpleName());
			return null;
		}
	}
	
	/**
	 * Returns the {@link SourceCategoryCache SourceCategoryCache}.
	 * @param reference The {@link ReferenceBase ReferenceBase}.
	 * @return The {@link SourceCategoryCache SourceCategoryCache}.
	 */
	public static String getSourceCategoryCache(ReferenceBase<?> reference) {
		if (reference == null){
			return null;
		} else if (reference.getType().equals(ReferenceType.Article)) {
			return REF_STR_ARTICLE_IN_PERIODICAL;
		} else if (reference instanceof ISectionBase) {
			return REF_STR_PART_OF_OTHER;
		} else if (reference.getType().equals(ReferenceType.Book)) {
			return REF_STR_BOOK;
		} else if (reference.getType().equals(ReferenceType.Database)) {
			return REF_STR_DATABASE;
		} else if (reference.getType().equals(ReferenceType.WebPage)) {
			return REF_STR_WEBSITE;
		} else if (reference.getType().equals(ReferenceType.CdDvd)) {
			return REF_STR_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.Journal)) {
			return REF_STR_JOURNAL;
		} else if (reference.getType().equals(ReferenceType.Generic)) {
			return REF_STR_UNRESOLVED;
		} else if (reference.getType().equals(ReferenceType.PrintSeries)) {
			return REF_STR_PUBLISHED;
		} else if (reference.getType().equals(ReferenceType.Proceedings)) {
			return REF_STR_PUBLISHED;
		} else if (reference.getType().equals(ReferenceType.Patent)) {
			return REF_STR_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.PersonalCommunication)) {
			return REF_STR_INFORMAL;
		} else if (reference.getType().equals(ReferenceType.Report)) {
			return REF_STR_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.Thesis)) {
			return REF_STR_NOT_APPLICABLE;
		} else {
			//TODO Figure out a way to handle this gracefully.
			logger.warn("Reference type not yet supported in PESI: "+ reference.getClass().getSimpleName());
			return null;
		}
	}
	
//	public static Integer rank2RankId(Rank rank) {
//		if (rank == null){
//			return null;
//		}
//		else if (rank.equals(Rank.KINGDOM())) {
//			return RANK_KINGDOM;
//		} else if (rank.equals(Rank.SUBKINGDOM())) {
//			return RANK_SUBKINGDOM;
//		} else if (rank.equals(Rank.SUPERPHYLUM())) {
//			return RANK_SUPERPHYLUM;
//		} else if (rank.equals(Rank.PHYLUM())) {
//			return RANK_PHYLUM;
//		} else if (rank.equals(Rank.SUBPHYLUM())) {
//			return RANK_SUBPHYLUM;
//		} else if (rank.equals(Rank.INFRAPHYLUM())) {
//			return RANK_INFRAPHYLUM;
//		} else if (rank.equals(Rank.SUPERCLASS())) {
//			return RANK_SUPERCLASS;
//		} else if (rank.equals(Rank.CLASS())) {
//			return RANK_CLASS;
//		} else if (rank.equals(Rank.SUBCLASS())) {
//			return RANK_SUBCLASS;
//		} else if (rank.equals(Rank.INFRACLASS())) {
//			return RANK_INFRACLASS;
//		} else if (rank.equals(Rank.SUPERORDER())) {
//			return RANK_SUPERORDER;
//		} else if (rank.equals(Rank.ORDER())) {
//			return RANK_ORDER;
//		} else if (rank.equals(Rank.SUBORDER())) {
//			return RANK_SUBORDER;
//		} else if (rank.equals(Rank.INFRAORDER())) {
//			return RANK_INFRAORDER;
//		} else if (rank.equals(Rank.secti)) {
//			return RANK_;
//		}
//		
//		public static int RANK_SECTION = 121;
//		public static int RANK_SUBSECTION = 122;
//		public static int RANK_SUPERFAMILY = 130;
//		public static int RANK_FAMILY = 140;
//		public static int RANK_SUBFAMILY = 150;
//		public static int RANK_TRIBE = 160;
//		public static int RANK_SUBTRIBE = 170;
//		public static int RANK_GENUS = 180;
//		public static int RANK_SUBGENUS = 190;
//		public static int RANK_SPECIES = 220;
//		public static int RANK_SUBSPECIES = 230;
//		public static int RANK_NATIO = 235;
//		public static int RANK_VARIETY = 240;
//		public static int RANK_SUBVARIETY = 250;
//		public static int RANK_FORMA = 260;
//	}
	
	public static Integer taxRelation2RelTaxonQualifierFk (RelationshipBase<?,?,?> relationship){
//		if (relationship == null) {
//			return null;
//		} else {
//			if ((relationship.getType()).equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
//				return TAX_REL_IS_INCLUDED_IN;
//			}
//		}
		return null;
	}
}
