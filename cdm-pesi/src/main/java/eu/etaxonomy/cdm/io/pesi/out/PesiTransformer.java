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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.common.mapping.out.ExportTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.pesi.erms.ErmsTransformer;
import eu.etaxonomy.cdm.io.pesi.faunaEuropaea.FaunaEuropaeaTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author e.-m.lee
 * @author a.mueller (update)
 * @date 16.02.2010
 *
 */
public final class PesiTransformer extends ExportTransformerBase implements IExportTransformer{
	private static final Logger logger = Logger.getLogger(PesiTransformer.class);

	public static final String AUCT_STRING = "auct.";
	
	
	//source identifiers
	public static final int SOURCE_EM = 1;
	public static final int SOURCE_FE = 2;
	public static final int SOURCE_IF = 3;
	public static final int SOURCE_ERMS = 4;
	
	//sourceRefUUIDs
	public static final UUID uuidSourceRefEuroMed = UUID.fromString("0603a84a-f024-4454-ab92-9e2ac0139126");
	public static final UUID uuidSourceRefFaunaEuropaea = UUID.fromString("6786d863-75d4-4796-b916-c1c3dff4cb70");
	public static final UUID uuidSourceRefErms = UUID.fromString("7744bc26-f914-42c4-b54a-dd2a030a8bb7");
	public static final UUID uuidSourceRefIndexFungorum = UUID.fromString("8de25d27-7d40-47f4-af3b-59d64935a843");
	public static final UUID uuidSourceRefAuct = UUID.fromString("5f79f96c-c100-4cd8-b78e-2b2dacf09a23");
	
	public static final String SOURCE_STR_EM = "E+M";
	public static final String SOURCE_STR_FE = "FaEu";
	public static final String SOURCE_STR_IF = "IF";
	public static final String SOURCE_STR_ERMS = "ERMS";

	// status keys
	public static int QUALITY_STATUS_CHECKED_EDITOR_ERMS_1_1 = 0;
	public static int QUALITY_STATUS_ADD_BY_DBMT= 2;
	public static int QUALITY_STATUS_CHECKED_EDITOR = 3;
	public static int QUALITY_STATUS_EDITED_BY_DBMT = 4;
	
	// marker type
	public static final UUID uuidMarkerGuidIsMissing = UUID.fromString("24e70843-05e2-44db-954b-84df0d23ea20");
	public static final UUID uuidMarkerTypeHasNoLastAction = UUID.fromString("99652d5a-bc92-4251-b57d-0fec4d258ab7");
//	public static final UUID uuidMarkerFossil = UUID.fromString("761ce108-031a-4e07-b444-f8d757070312");
	
	
	//extension type uuids
	public static final UUID cacheCitationUuid = UUID.fromString("29656168-32d6-4301-9067-d57c63be5c67");
	//public static final UUID expertUserIdUuid = UUID.fromString("e25813d3-c67c-4585-9aa0-970fafde50b4");
	//public static final UUID speciesExpertUserIdUuid = UUID.fromString("6d42abd8-8894-4980-ae07-e918affd4172");
	public static final UUID expertNameUuid = BerlinModelTransformer.uuidExpertName;
	public static final UUID speciesExpertNameUuid = BerlinModelTransformer.uuidSpeciesExpertName; 
	public static final UUID lastActionDateUuid = UUID.fromString("8d0a7d81-bb83-4576-84c3-8c906ef039b2");
	public static final UUID lastActionUuid = UUID.fromString("bc20d5bc-6161-4279-9499-89ea26ce5f6a");
	public static final UUID taxCommentUuid = UUID.fromString("8041a752-0479-4626-ab1b-b266b751f816");
	public static final UUID fauCommentUuid = UUID.fromString("054f773a-41c8-4ad5-83e3-981320c1c126");
	public static final UUID fauExtraCodesUuid = UUID.fromString("b8c7e77d-9869-4787-bed6-b4b302dbc5f5");

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
	public static String REF_STR_UNRESOLVED = "unresolved";
	
	// NameStatus
	public static int NAME_ST_NOM_INVAL = 1;
	public static int NAME_ST_NOM_ILLEG = 2;
	public static int NAME_ST_NOM_NUD = 3;
	public static int NAME_ST_NOM_REJ = 4;
	public static int NAME_ST_NOM_REJ_PROP = 5;
	public static int NAME_ST_NOM_UTIQUE_REJ = 6;
	public static int NAME_ST_NOM_UTIQUE_REJ_PROP = 7;
	public static int NAME_ST_NOM_CONS = 8;
	public static int NAME_ST_NOM_CONS_PROP = 9;
	public static int NAME_ST_ORTH_CONS = 10;
	public static int NAME_ST_ORTH_CONS_PROP = 11;
	public static int NAME_ST_NOM_SUPERFL = 12;
	public static int NAME_ST_NOM_AMBIG = 13;
	public static int NAME_ST_NOM_PROVIS = 14;
	public static int NAME_ST_NOM_DUB = 15;
	public static int NAME_ST_NOM_NOV = 16;
	public static int NAME_ST_NOM_CONFUS = 17;
	public static int NAME_ST_NOM_ALTERN = 18;
	public static int NAME_ST_COMB_INVAL = 19;
	public static int NAME_ST_LEGITIMATE = 20; // PESI specific from here
	public static int NAME_ST_COMB_INED = 21;
	public static int NAME_ST_COMB_AND_STAT_INED = 22;
	public static int NAME_ST_NOM_AND_ORTH_CONS = 23;
	public static int NAME_ST_NOM_NOV_INED = 24;
	public static int NAME_ST_SP_NOV_INED = 25;
	public static int NAME_ST_ALTERNATE_REPRESENTATION = 26;
	public static int NAME_ST_TEMPORARY_NAME = 27;
	public static int NAME_ST_SPECIES_INQUIRENDA = 28;

	// TaxonStatus
	public static int T_STATUS_ACCEPTED = 1;
	public static int T_STATUS_SYNONYM = 2;
	public static int T_STATUS_PARTIAL_SYN = 3;
	public static int T_STATUS_PRO_PARTE_SYN = 4;
	public static int T_STATUS_UNRESOLVED = 5;
	public static int T_STATUS_ORPHANED = 6;
	public static int T_STATUS_UNACCEPTED = 7;
	public static int T_STATUS_NOT_ACCEPTED = 8;
	
	// TypeDesginationStatus //	 -> not a table anymore
	public static int TYPE_BY_ORIGINAL_DESIGNATION = 1;
	public static int TYPE_BY_SUBSEQUENT_DESIGNATION = 2;
	public static int TYPE_BY_MONOTYPY = 3;
	public static String TYPE_STR_BY_ORIGINAL_DESIGNATION = "Type by original designation";
	public static String TYPE_STR_BY_SUBSEQUENT_DESIGNATION = "Type by subsequent designation";
	public static String TYPE_STR_BY_MONOTYPY = "Type by monotypy";
	
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
	public static int IS_INFERRED_EPITHET_FOR = 301;
	public static int IS_INFERRED_GENUS_FOR = 302;
	public static int IS_POTENTIAL_COMBINATION_FOR = 303;

		
	//namespaces
	public static String STR_NAMESPACE_NOMINAL_TAXON = "Nominal taxon from TAX_ID:";
	public static String STR_NAMESPACE_INFERRED_EPITHET = "Inferred epithet from TAX_ID:";
	public static String STR_NAMESPACE_INFERRED_GENUS = "Inferred genus from TAX_ID:";
	public static String STR_NAMESPACE_POTENTIAL_COMBINATION = "Potential combination from TAX_ID:";


	// Kingdoms
	public static int KINGDOM_NULL = 0;
	public static int KINGDOM_ANIMALIA = 2;
	public static int KINGDOM_PLANTAE = 3;
	public static int KINGDOM_FUNGI = 4;
	public static int KINGDOM_PROTOZOA = 5;
	public static int KINGDOM_BACTERIA = 6;
	public static int KINGDOM_CHROMISTA = 7;

	// Animalia Ranks
	public static int Animalia_Kingdom = 10;
	public static int Animalia_Subkingdom = 20;
	public static int Animalia_Superphylum = 23;
	public static int Animalia_Phylum = 30;
	public static int Animalia_Subphylum = 40;
	public static int Animalia_Infraphylum = 45;
	public static int Animalia_Superclass = 50;
	public static int Animalia_Class = 60;
	public static int Animalia_Subclass = 70;
	public static int Animalia_Infraclass = 80;
	public static int Animalia_Superorder = 90;
	public static int Animalia_Order = 100;
	public static int Animalia_Suborder = 110;
	public static int Animalia_Infraorder = 120;
	public static int Animalia_Section = 121;
	public static int Animalia_Subsection = 122;
	public static int Animalia_Superfamily = 130;
	public static int Animalia_Family = 140;
	public static int Animalia_Subfamily = 150;
	public static int Animalia_Tribe = 160;
	public static int Animalia_Subtribe = 170;
	public static int Animalia_Genus = 180;
	public static int Animalia_Subgenus = 190;
	public static int Animalia_Species =220;
	public static int Animalia_Subspecies = 230;
	public static int Animalia_Natio = 235;
	public static int Animalia_Variety = 240;
	public static int Animalia_Subvariety = 250;
	public static int Animalia_Forma = 260;

	// Plantae Ranks
	public static int Plantae_Kingdom = 10;
	public static int Plantae_Subkingdom = 20;
	public static int Plantae_Division = 30;
	public static int Plantae_Subdivision = 40;
	public static int Plantae_Class = 60;
	public static int Plantae_Subclass = 70;
	public static int Plantae_Order = 100;
	public static int Plantae_Suborder = 110;
	public static int Plantae_Family = 140;
	public static int Plantae_Subfamily = 150;
	public static int Plantae_Tribe	= 160;
	public static int Plantae_Subtribe = 170;
	public static int Plantae_Genus = 180;
	public static int Plantae_Subgenus = 190;
	public static int Plantae_Section = 200;
	public static int Plantae_Subsection = 210;
	public static int Plantae_Series = 212;
	public static int Plantae_Subseries	= 214;
	public static int Plantae_Aggregate	= 216;
	public static int Plantae_Coll_Species = 218;
	public static int Plantae_Species = 220;
	public static int Plantae_Grex = 225;
	public static int Plantae_Subspecies = 230;
	public static int Plantae_Proles = 232;
	public static int Plantae_Race = 234;
	public static int Plantae_Convarietas = 236;
	public static int Plantae_Variety = 240;
	public static int Plantae_Subvariety = 250;
	public static int Plantae_Forma	= 260;
	public static int Plantae_Subforma = 270;
	public static int Plantae_Forma_spec = 275;
	public static int Plantae_Taxa_infragen = 280;
	public static int Plantae_Taxa_infraspec = 285;
	
	// Fungi Ranks
	public static int Fungi_Kingdom = 10;
	public static int Fungi_Subkingdom = 20;
	public static int Fungi_Division = 30;
	public static int Fungi_Subdivision = 40;
	public static int Fungi_Class	= 60;
	public static int Fungi_Subclass = 70;
	public static int Fungi_Order	= 100;
	public static int Fungi_Suborder = 110;
	public static int Fungi_Family = 140;
	public static int Fungi_Subfamily = 150;
	public static int Fungi_Tribe = 160;
	public static int Fungi_Subtribe = 170;
	public static int Fungi_Genus = 180;
	public static int Fungi_Subgenus = 190;
	public static int Fungi_Section = 200;
	public static int Fungi_Subsection = 210;
	public static int Fungi_Species = 220;
	public static int Fungi_Subspecies = 230;
	public static int Fungi_Variety = 240;
	public static int Fungi_Subvariety = 250;
	public static int Fungi_Forma	= 260;
	public static int Fungi_Subforma = 270;
	
	//Protozoa Ranks
	public static int Protozoa_Kingdom = 10;
	public static int Protozoa_Subkingdom = 20;
	public static int Protozoa_Phylum = 30;
	public static int Protozoa_Subphylum = 40;
	public static int Protozoa_Superclass = 50;
	public static int Protozoa_Class	= 60;
	public static int Protozoa_Subclass = 70;
	public static int Protozoa_Infraclass = 80;
	public static int Protozoa_Superorder = 90;
	public static int Protozoa_Order	= 100;
	public static int Protozoa_Suborder = 110;
	public static int Protozoa_Infraorder = 120;
	public static int Protozoa_Superfamily = 130;
	public static int Protozoa_Family = 140;
	public static int Protozoa_Subfamily = 150;
	public static int Protozoa_Tribe	= 160;
	public static int Protozoa_Subtribe = 170;
	public static int Protozoa_Genus	= 180;
	public static int Protozoa_Subgenus = 190;
	public static int Protozoa_Species = 220;
	public static int Protozoa_Subspecies = 230;
	public static int Protozoa_Variety = 240;
	public static int Protozoa_Forma	= 260;
	
	// Bacteria Ranks
	public static int Bacteria_Kingdom = 10;
	public static int Bacteria_Subkingdom = 20;
	public static int Bacteria_Phylum = 30;
	public static int Bacteria_Subphylum	= 40;
	public static int Bacteria_Superclass = 50;
	public static int Bacteria_Class	= 60;
	public static int Bacteria_Subclass = 70;
	public static int Bacteria_Infraclass = 80;
	public static int Bacteria_Superorder = 90;
	public static int Bacteria_Order	= 100;
	public static int Bacteria_Suborder = 110;
	public static int Bacteria_Infraorder = 120;
	public static int Bacteria_Superfamily = 130;
	public static int Bacteria_Family = 140;
	public static int Bacteria_Subfamily	= 150;
	public static int Bacteria_Tribe	= 160;
	public static int Bacteria_Subtribe = 170;
	public static int Bacteria_Genus	= 180;
	public static int Bacteria_Subgenus = 190;
	public static int Bacteria_Species = 220;
	public static int Bacteria_Subspecies = 230;
	public static int Bacteria_Variety = 240;
	public static int Bacteria_Forma	= 260;

	// Chromista Ranks
	public static int Chromista_Kingdom = 10;
	public static int Chromista_Subkingdom = 20;
	public static int Chromista_Infrakingdom = 25;
	public static int Chromista_Phylum = 30;
	public static int Chromista_Subphylum = 40;
	public static int Chromista_Superclass = 50;
	public static int Chromista_Class = 60;
	public static int Chromista_Subclass = 70;
	public static int Chromista_Infraclass = 80;
	public static int Chromista_Superorder = 90;
	public static int Chromista_Order = 100;
	public static int Chromista_Suborder = 110;
	public static int Chromista_Infraorder = 120;
	public static int Chromista_Superfamily	= 130;
	public static int Chromista_Family = 140;
	public static int Chromista_Subfamily = 150;
	public static int Chromista_Tribe = 160;
	public static int Chromista_Subtribe = 170;
	public static int Chromista_Genus = 180;
	public static int Chromista_Subgenus = 190;
	public static int Chromista_Section = 200;
	public static int Chromista_Subsection = 210;
	public static int Chromista_Species	= 220;
	public static int Chromista_Subspecies = 230;
	public static int Chromista_Variety	= 240;
	public static int Chromista_Subvariety = 250;
	public static int Chromista_Forma = 260;
	
	// NoteCategory
	public static int NoteCategory_description = 1;
	public static int NoteCategory_ecology = 4;
	public static int NoteCategory_phenology	= 5;
	public static int NoteCategory_general_distribution_euromed = 10;
	public static int NoteCategory_general_distribution_world = 11;
	public static int NoteCategory_Common_names = 12;
	public static int NoteCategory_Occurrence = 13;
	public static int NoteCategory_Maps =14;
	public static int NoteCategory_Link_to_maps = 20;
	public static int NoteCategory_Link_to_images = 21;
	public static int NoteCategory_Link_to_taxonomy = 22;
	public static int NoteCategory_Link_to_general_information = 23;
	public static int NoteCategory_undefined_link = 24;
	public static int NoteCategory_Editor_Braces = 249;
	public static int NoteCategory_Editor_Brackets = 250;
	public static int NoteCategory_Editor_Parenthesis = 251;
	public static int NoteCategory_Inedited = 252;
	public static int NoteCategory_Comments_on_editing_process = 253;
	public static int NoteCategory_Publication_date = 254;
	public static int NoteCategory_Morphology = 255;
	public static int NoteCategory_Acknowledgments = 257;
	public static int NoteCategory_Original_publication = 258;
	public static int NoteCategory_Type_locality	= 259;
	public static int NoteCategory_Environment = 260;
	public static int NoteCategory_Spelling = 261;
	public static int NoteCategory_Systematics = 262;
	public static int NoteCategory_Remark = 263;
	public static int NoteCategory_Date_of_publication = 264;
	public static int NoteCategory_Additional_information = 266;
	public static int NoteCategory_Status = 267;
	public static int NoteCategory_Nomenclature = 268;
	public static int NoteCategory_Homonymy = 269;
	public static int NoteCategory_Taxonomy = 270;
	public static int NoteCategory_Taxonomic_status = 272;
	public static int NoteCategory_Authority	= 273;
	public static int NoteCategory_Identification = 274;
	public static int NoteCategory_Validity = 275;
	public static int NoteCategory_Classification = 276;
	public static int NoteCategory_Distribution = 278;
	public static int NoteCategory_Synonymy = 279;
	public static int NoteCategory_Habitat = 280;
	public static int NoteCategory_Biology = 281;
	public static int NoteCategory_Diagnosis	= 282;
	public static int NoteCategory_Host = 283;
	public static int NoteCategory_Note = 284;
	public static int NoteCategory_Rank = 285;
	public static int NoteCategory_Taxonomic_Remark = 286;
	public static int NoteCategory_Taxonomic_Remarks = 287;

	public static int NoteCategory_Conservation_Status= 301;
	public static int NoteCategory_Use = 302;
	public static int NoteCategory_Comments = 303;

	
	// Language
	public static int Language_Albanian = 1;
	public static int Language_Arabic = 2;
	public static int Language_Armenian = 3;
	public static int Language_Azerbaijan = 4;
	public static int Language_Belarusian = 5;
	public static int Language_Bulgarian = 6;
	public static int Language_Catalan = 7;
	public static int Language_Croat = 8;
	public static int Language_Czech = 9;
	public static int Language_Danish = 10;
	public static int Language_Dutch = 11;
	public static int Language_English = 12;
	public static int Language_Euskera = 13;
	public static int Language_Estonian = 14;
	public static int Language_Finnish = 15;
	public static int Language_French = 16;
	public static int Language_Georgian = 17;
	public static int Language_German = 18;
	public static int Language_Greek = 19;
	public static int Language_Hungarian = 20;
	public static int Language_Icelandic = 21;
	public static int Language_Irish_Gaelic = 22;
	public static int Language_Israel_Hebrew = 23;
	public static int Language_Italian = 24;
	public static int Language_Latvian = 25;
	public static int Language_Lithuanian = 26;
	public static int Language_Macedonian = 27;
	public static int Language_Maltese = 28;
	public static int Language_Moldovian = 29;
	public static int Language_Norwegian = 30;
	public static int Language_Polish = 31;
	public static int Language_Portuguese = 32;
	public static int Language_Roumanian = 33;
	public static int Language_Russian = 34;
	public static int Language_Russian_Caucasian = 35;
	public static int Language_Russian_Altaic_kalmyk_oirat = 36;
	public static int Language_Russian_Altaic_karachay_balkar = 37;
	public static int Language_Russian_Altaic_kumyk = 38;
	public static int Language_Russian_Altaic_nogai = 39;
	public static int Language_Russian_Altaic_north_azerbaijani = 40;
	public static int Language_Russian_Indo_european_russian = 41;
	public static int Language_Russian_Indo_european_kalmyk_oirat = 42;
	public static int Language_Russian_Indo_european_osetin = 43;
	public static int Language_Russian_North_caucasian_abaza = 44;
	public static int Language_Russian_North_caucasian_adyghe = 45;
	public static int Language_Russian_North_caucasian_chechen = 46;
	public static int Language_Russian_North_caucasian_kabardian = 47;
	public static int Language_Russian_North_caucasian_lak = 48;
	public static int Language_Russian_North_caucasian_avar = 49;
	public static int Language_Russian_North_caucasian_in = 50;
	public static int Language_Russian_Uralic_chuvash = 51;
	public static int Language_Russian_Uralic_udmurt = 52;
	public static int Language_Serbian = 53;
	public static int Language_Slovak = 54;
	public static int Language_Slovene = 55;
	public static int Language_Spanish_Castillian = 56;
	public static int Language_Swedish = 57;
	public static int Language_Turkish = 58;
	public static int Language_Ukraine = 59;
	public static int Language_Welsh = 60;
	public static int Language_Corsican = 61;

	// FossilStatus
	public static int FOSSILSTATUS_RECENT_ONLY = 1;
	public static int FOSSILSTATUS_FOSSIL_ONLY = 2;
	public static int FOSSILSTATUS_RECENT_FOSSIL = 3;
	public static String STR_FOSSIL_ONLY = "fossil only";  //still used for Index Fungorum
	
	// SourceUse
	public static int ORIGINAL_DESCRIPTION = 1;
	public static int BASIS_OF_RECORD = 2;
	public static int ADDITIONAL_SOURCE = 3;
	public static int SOURCE_OF_SYNONYMY = 4;
	public static int REDESCRIPTION = 5;
	public static int NEW_COMBINATION_REFERENCE = 6;
	public static int STATUS_SOURCE = 7;
	public static int NOMENCLATURAL_REFERENCE = 8;
	public static String STR_NOMENCLATURAL_REFERENCE = "nomenclatural reference";

	// Area
	public static int AREA_EAST_AEGEAN_ISLANDS = 1;
	public static int AREA_GREEK_EAST_AEGEAN_ISLANDS = 2;
	public static int AREA_TURKISH_EAST_AEGEAN_ISLANDS = 3;
	public static int AREA_ALBANIA = 4;
	public static int AREA_AUSTRIA_WITH_LIECHTENSTEIN = 5;
	public static int AREA_AUSTRIA = 6;
	public static int AREA_LIECHTENSTEIN = 7;
	public static int AREA_AZORES = 8;
	public static int AREA_CORVO = 9;
	public static int AREA_FAIAL = 10;
	public static int AREA_GRACIOSA = 11;
	public static int AREA_SAO_JORGE = 12;
	public static int AREA_FLORES = 13;
	public static int AREA_SAO_MIGUEL = 14;
	public static int AREA_PICO = 15;
	public static int AREA_SANTA_MARIA = 16;
	public static int AREA_TERCEIRA = 17;
	public static int AREA_BELGIUM_WITH_LUXEMBOURG = 18;
	public static int AREA_BELGIUM = 19;
	public static int AREA_LUXEMBOURG = 20;
	public static int AREA_BOSNIA_HERZEGOVINA = 21;
	public static int AREA_BALEARES = 22;
	public static int AREA_IBIZA_WITH_FORMENTERA = 23;
	public static int AREA_MALLORCA = 24;
	public static int AREA_MENORCA = 25;
	public static int AREA_GREAT_BRITAIN = 26;
	public static int AREA_BALTIC_STATES_ESTONIA_LATVIA_LITHUANIA_AND_KALININGRAD_REGION = 27;
	public static int AREA_BULGARIA = 28;
	public static int AREA_BELARUS = 29;
	public static int AREA_CANARY_ISLANDS = 30;
	public static int AREA_GRAN_CANARIA = 31;
	public static int AREA_FUERTEVENTURA_WITH_LOBOS = 32;
	public static int AREA_GOMERA = 33;
	public static int AREA_HIERRO = 34;
	public static int AREA_LANZAROTE_WITH_GRACIOSA = 35;
	public static int AREA_LA_PALMA = 36;
	public static int AREA_TENERIFE = 37;
	public static int AREA_MONTENEGRO = 38;
	public static int AREA_CORSE = 39;
	public static int AREA_CRETE_WITH_KARPATHOS_KASOS_AND_GAVDHOS = 40;
	public static int AREA_CZECH_REPUBLIC = 41;
	public static int AREA_CROATIA = 42;
	public static int AREA_CYPRUS = 43;
	public static int AREA_FORMER_CZECHOSLOVAKIA = 44;
	public static int AREA_DENMARK_WITH_BORNHOLM = 45;
	public static int AREA_ESTONIA = 46;
	public static int AREA_FAROE_ISLANDS = 47;
	public static int AREA_FINLAND_WITH_AHVENANMAA = 48;
	public static int AREA_FRANCE = 49;
	public static int AREA_CHANNEL_ISLANDS = 50;
	public static int AREA_FRENCH_MAINLAND = 51;
	public static int AREA_MONACO = 52;
	public static int AREA_GERMANY = 53;
	public static int AREA_GREECE_WITH_CYCLADES_AND_MORE_ISLANDS = 54;
	public static int AREA_IRELAND = 55;
	public static int AREA_REPUBLIC_OF_IRELAND = 56;
	public static int AREA_NORTHERN_IRELAND = 57;
	public static int AREA_SWITZERLAND = 58;
	public static int AREA_NETHERLANDS = 59;
	public static int AREA_SPAIN = 60;
	public static int AREA_ANDORRA = 61;
	public static int AREA_GIBRALTAR = 62;
	public static int AREA_KINGDOM_OF_SPAIN = 63;
	public static int AREA_HUNGARY = 64;
	public static int AREA_ICELAND = 65;
	public static int AREA_ITALY = 66;
	public static int AREA_ITALIAN_MAINLAND = 67;
	public static int AREA_SAN_MARINO = 68;
	public static int AREA_FORMER_JUGOSLAVIA = 69;
	public static int AREA_LATVIA = 70;
	public static int AREA_LITHUANIA = 71;
	public static int AREA_PORTUGUESE_MAINLAND = 72;
	public static int AREA_MADEIRA_ARCHIPELAGO = 73;
	public static int AREA_DESERTAS = 74;
	public static int AREA_MADEIRA = 75;
	public static int AREA_PORTO_SANTO = 76;
	public static int AREA_THE_FORMER_JUGOSLAV_REPUBLIC_OF_MAKEDONIJA = 77;
	public static int AREA_MOLDOVA = 78;
	public static int AREA_NORWEGIAN_MAINLAND = 79;
	public static int AREA_POLAND = 80;
	public static int AREA_THE_RUSSIAN_FEDERATION = 81;
	public static int AREA_NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND = 82;
	public static int AREA_CENTRAL_EUROPEAN_RUSSIA = 83;
	public static int AREA_EASTERN_EUROPEAN_RUSSIA = 84;
	public static int AREA_KALININGRAD = 85;
	public static int AREA_NORTHERN_EUROPEAN_RUSSIA = 86;
	public static int AREA_NORTHWEST_EUROPEAN_RUSSIA = 87;
	public static int AREA_SOUTH_EUROPEAN_RUSSIA = 88;
	public static int AREA_ROMANIA = 89;
	public static int AREA_FORMER_USSR = 90;
	public static int AREA_RUSSIA_BALTIC = 91;
	public static int AREA_RUSSIA_CENTRAL = 92;
	public static int AREA_RUSSIA_SOUTHEAST = 93;
	public static int AREA_RUSSIA_NORTHERN = 94;
	public static int AREA_RUSSIA_SOUTHWEST = 95;
	public static int AREA_SARDEGNA = 96;
	public static int AREA_SVALBARD_WITH_BJORNOYA_AND_JAN_MAYEN = 97;
	public static int AREA_SELVAGENS_ISLANDS = 98;
	public static int AREA_SICILY_WITH_MALTA = 99;
	public static int AREA_MALTA = 100;
	public static int AREA_SICILY = 101;
	public static int AREA_SLOVAKIA = 102;
	public static int AREA_SLOVENIA = 103;
	public static int AREA_SERBIA_WITH_MONTENEGRO = 104;
	public static int AREA_SERBIA_INCLUDING_VOJVODINA_AND_WITH_KOSOVO = 105;
	public static int AREA_SWEDEN = 106;
	public static int AREA_EUROPEAN_TURKEY = 107;
	public static int AREA_UKRAINE_INCLUDING_CRIMEA = 108;
	public static int AREA_CRIMEA = 109;
	public static int AREA_UKRAINE = 110;
	public static int AREA_GREEK_MAINLAND = 111;
	public static int AREA_CRETE = 112;
	public static int AREA_DODECANESE_ISLANDS = 113;
	public static int AREA_CYCLADES_ISLANDS = 114;
	public static int AREA_NORTH_AEGEAN_ISLANDS = 115;
	public static int AREA_VATICAN_CITY = 116;
	public static int AREA_FRANZ_JOSEF_LAND = 117;
	public static int AREA_NOVAYA_ZEMLYA = 118;
	public static int AREA_AZERBAIJAN_INCLUDING_NAKHICHEVAN = 119;
	public static int AREA_AZERBAIJAN = 120;
	public static int AREA_NAKHICHEVAN = 121;
	public static int AREA_ALGERIA = 122;
	public static int AREA_ARMENIA = 123;
	public static int AREA_CAUCASUS_REGION = 124;
	public static int AREA_EGYPT = 125;
	public static int AREA_GEORGIA = 126;
	public static int AREA_ISRAEL_JORDAN = 127;
	public static int AREA_ISRAEL = 128;
	public static int AREA_JORDAN = 129;
	public static int AREA_LEBANON = 130;
	public static int AREA_LIBYA = 131;
	public static int AREA_LEBANON_SYRIA = 132;
	public static int AREA_MOROCCO = 133;
	public static int AREA_NORTH_CAUCASUS = 134;
	public static int AREA_SINAI = 135;
	public static int AREA_SYRIA = 136;
	public static int AREA_TUNISIA = 137;
	public static int AREA_ASIATIC_TURKEY = 138;
	public static int AREA_TURKEY = 139;
	public static int AREA_NORTHERN_AFRICA = 140;
	public static int AREA_AFRO_TROPICAL_REGION = 141;
	public static int AREA_AUSTRALIAN_REGION = 142;
	public static int AREA_EAST_PALAEARCTIC = 143;
	public static int AREA_NEARCTIC_REGION = 144;
	public static int AREA_NEOTROPICAL_REGION = 145;
	public static int AREA_NEAR_EAST = 146;
	public static int AREA_ORIENTAL_REGION = 147;
	public static int AREA_EUROPEAN_MARINE_WATERS = 148;
	public static int AREA_MEDITERRANEAN_SEA = 149;
	public static int AREA_WHITE_SEA = 150;
	public static int AREA_NORTH_SEA = 151;
	public static int AREA_BALTIC_SEA = 152;
	public static int AREA_BLACK_SEA = 153;
	public static int AREA_BARENTS_SEA = 154;
	public static int AREA_CASPIAN_SEA = 155;
	public static int AREA_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE = 156;
	public static int AREA_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE = 157;
	public static int AREA_FRENCH_EXCLUSIVE_ECONOMIC_ZONE = 158;
	public static int AREA_ENGLISH_CHANNEL = 159;
	public static int AREA_ADRIATIC_SEA = 160;
	public static int AREA_BISCAY_BAY = 161;
	public static int AREA_DUTCH_EXCLUSIVE_ECONOMIC_ZONE = 162;
	public static int AREA_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE = 163;
	public static int AREA_SPANISH_EXCLUSIVE_ECONOMIC_ZONE = 164;
	public static int AREA_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE = 165;
	public static int AREA_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE = 166;
	public static int AREA_TIRRENO_SEA = 167;
	public static int AREA_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE = 168;
	public static int AREA_IRISH_EXCLUSIVE_ECONOMIC_ZONE = 169;
	public static int AREA_IRISH_SEA = 170;
	public static int AREA_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE = 171;
	public static int AREA_NORWEGIAN_SEA = 172;
	public static int AREA_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE = 173;
	public static int AREA_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE = 174;
	public static int AREA_SKAGERRAK = 175;
	public static int AREA_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE = 176;
	public static int AREA_WADDEN_SEA = 177;
	public static int AREA_BELT_SEA = 178;
	public static int AREA_MARMARA_SEA = 179;
	public static int AREA_SEA_OF_AZOV = 180;
	public static int AREA_AEGEAN_SEA = 181;
	public static int AREA_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE = 182;
	public static int AREA_SOUTH_BALTIC_PROPER = 183;
	public static int AREA_BALTIC_PROPER = 184;
	public static int AREA_NORTH_BALTIC_PROPER = 185;
	public static int AREA_ARCHIPELAGO_SEA = 186;
	public static int AREA_BOTHNIAN_SEA = 187;
	public static int AREA_GERMAN_EXCLUSIVE_ECONOMIC_ZONE = 188;
	public static int AREA_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE = 189;
	public static int AREA_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE = 190;
	public static int AREA_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE = 191;
	public static int AREA_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE = 192;
	public static int AREA_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART = 193;
	public static int AREA_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE = 194;
	public static int AREA_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE = 195;
	public static int AREA_BALEAR_SEA = 196;
	public static int AREA_TURKISH_EXCLUSIVE_ECONOMIC_ZONE = 197;
	public static int AREA_DANISH_EXCLUSIVE_ECONOMIC_ZONE = 198;
	public static int AREA_TRANSCAUCASUS = 199;
	
	public static int AREA_UNITED_KINGDOM = 203;
	public static int AREA_DENMARK_COUNTRY = 204;
	public static int AREA_TURKEY_COUNTRY = 205;
	public static int AREA_SPAIN_COUNTRY = 206;
	public static int AREA_GREECE_COUNTRY = 207;
	public static int AREA_PORTUGAL_COUNTRY = 208;

	// OccurrenceStatus
	public static int STATUS_PRESENT = 1;
	public static int STATUS_ABSENT = 2;
	public static int STATUS_NATIVE = 3;
	public static int STATUS_INTRODUCED = 4;
	public static int STATUS_NATURALISED = 5;
	public static int STATUS_INVASIVE = 6;
	public static int STATUS_MANAGED = 7;
	public static int STATUS_DOUBTFUL = 8;

	private Map<String, Integer> tdwgKeyMap = new HashMap<String, Integer>();
	private Map<Integer, String> areaCacheMap = new HashMap<Integer, String>();
	private Map<Integer, String> languageCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, String> featureCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, String> nameStatusCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, String> qualityStatusCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, String> taxonStatusCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, String> taxRelQualifierCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, String> taxRelZooQualifierCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, String> sourceUseCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, String> fossilStatusCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, String> typeDesigStatusCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, String> sourceCategoryCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, String> occurrenceStatusCacheMap  = new HashMap<Integer, String>();
	private Map<Integer, Map<Integer, String>> rankCacheMap  = new  HashMap<Integer, Map<Integer, String>>();
	private Map<Integer, Map<Integer, String>> rankAbbrevCacheMap  = new  HashMap<Integer, Map<Integer, String>>();
	
	
	private Source destination;
	
	public PesiTransformer(Source destination) {
		super();
		this.destination = destination;
		fillMaps();
	}

	private void fillMaps() {
		//TDWG
		try {
			String sql = " SELECT AreaId, AreaName, AreaTdwgCode, AreaEmCode, AreaFaEuCode FROM Area";
			ResultSet rs = destination.getResultSet(sql);
			while (rs.next()){
				String tdwg = rs.getString("AreaTdwgCode");
				Integer id = rs.getInt("AreaId");
				String label = rs.getString("AreaName");
				
				if (StringUtils.isNotBlank(tdwg)){
					this.tdwgKeyMap.put(tdwg, id);
				}
				this.areaCacheMap.put(id, label);
			}
			
			//rankCache
			sql = " SELECT KingdomId, RankId, Rank, RankAbbrev  FROM Rank";
			rs = destination.getResultSet(sql);
			while (rs.next()){
				String rank = rs.getString("Rank");
				String abbrev = rs.getString("RankAbbrev");
				Integer rankId = rs.getInt("RankId");
				Integer kingdomId = rs.getInt("KingdomId");
				
				//rank str
				Map<Integer, String> kingdomMap = rankCacheMap.get(kingdomId);
				if (kingdomMap == null){
					kingdomMap = new HashMap<Integer, String>();
					rankCacheMap.put(kingdomId, kingdomMap);
				}
				kingdomMap.put(rankId, rank);
				
				//rank abbrev
				Map<Integer, String> kingdomAbbrevMap = rankAbbrevCacheMap.get(kingdomId);
				if (kingdomAbbrevMap == null){
					kingdomAbbrevMap = new HashMap<Integer, String>();
					rankAbbrevCacheMap.put(kingdomId, kingdomAbbrevMap);
				}
				if (StringUtils.isNotBlank(abbrev)){
					kingdomAbbrevMap.put(rankId, abbrev);
				}
				
			}
			
			//languageCache
			fillSingleMap(languageCacheMap,"Language");

			//feature / note category
			fillSingleMap(featureCacheMap,"NoteCategory");
			
			//nameStatusCache
			fillSingleMap(nameStatusCacheMap,"NameStatus", "NomStatus");

			//qualityStatusCache
			fillSingleMap(qualityStatusCacheMap,"QualityStatus");

			//taxonStatusCache
			fillSingleMap(taxonStatusCacheMap,"TaxonStatus", "Status");
			
			//sourceUse
			fillSingleMap(sourceUseCacheMap,"SourceUse");

			//fossil status
			fillSingleMap(fossilStatusCacheMap,"FossilStatus");
			
			//fossil status
			fillSingleMap(typeDesigStatusCacheMap,"FossilStatus");

			//fossil status
			fillSingleMap(occurrenceStatusCacheMap,"OccurrenceStatus");
			
			//source category
			fillSingleMap(sourceCategoryCacheMap,"SourceCategory", "Category", "SourceCategoryId");
			
			//RelTaxonQualifier
			sql = " SELECT QualifierId, Qualifier, ZoologQualifier FROM RelTaxonQualifier ";
			rs = destination.getResultSet(sql);
			while (rs.next()){
				Integer key = rs.getInt("QualifierId");
				String cache = rs.getString("Qualifier");
				if (StringUtils.isNotBlank(cache)){
					this.taxRelQualifierCacheMap.put(key, cache);
				}
				String zoologCache = rs.getString("ZoologQualifier");
				if (StringUtils.isNotBlank(zoologCache)){
					this.taxRelZooQualifierCacheMap.put(key, zoologCache);
				}
			}
					
		} catch (SQLException e) {
			logger.error("SQLException when trying to read area map", e);
			e.printStackTrace();
		}
		
	}

	private void fillSingleMap(Map<Integer, String> map, String tableName) throws SQLException {
		fillSingleMap(map, tableName, tableName,  tableName + "Id");
	}
	
	private void fillSingleMap(Map<Integer, String> map, String tableName, String attr) throws SQLException {
			fillSingleMap(map, tableName, attr,  attr + "Id");
	}
	
	private void fillSingleMap(Map<Integer, String> map, String tableName, String attr, String idAttr) throws SQLException {
		String sql;
		ResultSet rs;
		sql = " SELECT %s, %s FROM %s ";
		sql = String.format(sql, idAttr, attr, tableName);
		rs = destination.getResultSet(sql);
		while (rs.next()){
			Integer key = rs.getInt(idAttr);
			String cache = rs.getString(attr);
			if (StringUtils.isNotBlank(cache)){
				map.put(key, cache);
			} 
		}
	}

	/**
	 * Converts the databaseString to its abbreviation if its known.
	 * Otherwise the databaseString is returned.
	 * @param databaseString
	 * @return
	 */
	public static String databaseString2Abbreviation(String databaseString) {
		String result = databaseString;
		if (databaseString.equals("Fauna Europaea database")) {
			result = "FaEu";
		}
		return result;
	}

	
	/**
	 * Returns the OccurrenceStatusId for a given PresenceAbsenceTerm.
	 * @param term
	 * @return
	 * @throws UnknownCdmTypeException 
	 */
	public static Integer presenceAbsenceTerm2OccurrenceStatusId(PresenceAbsenceTermBase<?> term) {
		Integer result = null;
		if (term == null){
			return null;
		//present
		}else if (term.isInstanceOf(PresenceTerm.class)) {
			PresenceTerm presenceTerm = CdmBase.deproxy(term, PresenceTerm.class);
			if (presenceTerm.equals(PresenceTerm.PRESENT()) || 
					presenceTerm.equals(PresenceTerm.INTRODUCED_DOUBTFULLY_INTRODUCED()) || 
					presenceTerm.equals(PresenceTerm.NATIVE_DOUBTFULLY_NATIVE())) {
				result = STATUS_PRESENT;
			} else if (presenceTerm.equals(PresenceTerm.NATIVE())) {
				result = STATUS_NATIVE;
			} else if (presenceTerm.equals(PresenceTerm.INTRODUCED()) || 
					presenceTerm.equals(PresenceTerm.INTRODUCED_ADVENTITIOUS()) ||
					presenceTerm.equals(PresenceTerm.INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION())) {
				result = STATUS_INTRODUCED;
			} else if (presenceTerm.equals(PresenceTerm.NATURALISED()) 
					|| presenceTerm.equals(PresenceTerm.INTRODUCED_NATURALIZED())) {
				result = STATUS_NATURALISED;
			} else if (presenceTerm.equals(PresenceTerm.INVASIVE())) {
				result = STATUS_INVASIVE;
			} else if (presenceTerm.equals(PresenceTerm.CULTIVATED())) {
				result = STATUS_MANAGED;
			} else if (presenceTerm.equals(PresenceTerm.PRESENT_DOUBTFULLY())||
					presenceTerm.equals(PresenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE()) ||
					presenceTerm.equals(PresenceTerm.NATIVE_PRESENCE_QUESTIONABLE() )) {
				result = STATUS_DOUBTFUL;
			} else {
				logger.error("PresenceTerm could not be translated to datawarehouse occurrence status id: " + presenceTerm.getLabel());
			}
		//absent	
		} else if (term.isInstanceOf(AbsenceTerm.class)) {
			AbsenceTerm absenceTerm = CdmBase.deproxy(term, AbsenceTerm.class);
			if (absenceTerm.equals(AbsenceTerm.ABSENT()) || absenceTerm.equals(AbsenceTerm.NATIVE_FORMERLY_NATIVE()) ||
					absenceTerm.equals(AbsenceTerm.CULTIVATED_REPORTED_IN_ERROR()) || absenceTerm.equals(AbsenceTerm.INTRODUCED_REPORTED_IN_ERROR()) ||
					absenceTerm.equals(AbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED()) || absenceTerm.equals(AbsenceTerm.NATIVE_REPORTED_IN_ERROR() ) ) {
				result = STATUS_ABSENT;
			} else {
				logger.error("AbsenceTerm could not be translated to datawarehouse occurrence status id: " + absenceTerm.getLabel());
			}
		}
		return result;
	}
	

	@Override
	public String getCacheByPresenceAbsenceTerm(PresenceAbsenceTermBase status) throws UndefinedTransformerMethodException {
		if (status == null){
			return null;
		}else{
			return this.occurrenceStatusCacheMap.get(getKeyByPresenceAbsenceTerm(status)); 
		}
	}
	
	@Override
	public Object getKeyByPresenceAbsenceTerm(PresenceAbsenceTermBase status) throws UndefinedTransformerMethodException {
		return presenceAbsenceTerm2OccurrenceStatusId(status);
	}
	
	
	
	@Override
	public String getCacheByNamedArea(NamedArea namedArea) throws UndefinedTransformerMethodException {
		NamedArea area = CdmBase.deproxy(namedArea, NamedArea.class);
		if (area == null){
			return null;
		}else{
			return this.areaCacheMap.get(getKeyByNamedArea(area)); 
		}
	}
	
	
	@Override
	public Object getKeyByNamedArea(NamedArea area) throws UndefinedTransformerMethodException {
		NamedArea namedArea = CdmBase.deproxy(area, NamedArea.class);

		if (area == null) {
			return null;
		//TDWG areas
		} else if (area.isInstanceOf(TdwgArea.class)) {
			String abbrevLabel = namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel();
			Integer result = this.tdwgKeyMap.get(abbrevLabel);
			if (result == null){
				logger.warn("Unknown TDWGArea: " + area.getTitleCache());
			}
			return result;
		//countries & Waterbodies
		}else if (namedArea.isInstanceOf(WaterbodyOrCountry.class)){
			if (namedArea.equals(WaterbodyOrCountry.UKRAINE())) { return AREA_UKRAINE_INCLUDING_CRIMEA; }
			else if (namedArea.equals(WaterbodyOrCountry.AZERBAIJANREPUBLICOF())) { return AREA_AZERBAIJAN_INCLUDING_NAKHICHEVAN; }
			else if (namedArea.equals(WaterbodyOrCountry.GEORGIA())) { return AREA_GEORGIA; }
			else if (namedArea.equals(WaterbodyOrCountry.RUSSIANFEDERATION())) { return AREA_THE_RUSSIAN_FEDERATION; }
			else if (namedArea.equals(WaterbodyOrCountry.UNITEDKINGDOMOFGREATBRITAINANDNORTHERNIRELAND())) { return AREA_UNITED_KINGDOM; }
			else if (namedArea.equals(WaterbodyOrCountry.DENMARKKINGDOMOF())) { return AREA_DENMARK_COUNTRY; }
			else if (namedArea.equals(WaterbodyOrCountry.TURKEYREPUBLICOF())) { return AREA_TURKEY_COUNTRY; }
			else {
				logger.warn("Unknown Waterbody/Country: " + area.getTitleCache());
			}
		}else{  //Non TDWG, non country
			//EM
			if ( namedArea.getUuid().equals(BerlinModelTransformer.uuidMadeira)){ return AREA_MADEIRA; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidDesertas)) { return AREA_DESERTAS; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidPortoSanto)) { return AREA_PORTO_SANTO; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidFlores)) { return AREA_FLORES; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidCorvo)) { return AREA_CORVO; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidFaial)) { return AREA_FAIAL; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidGraciosa)) { return AREA_GRACIOSA; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidSaoJorge)) { return AREA_SAO_JORGE; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidSaoMiguel)) { return AREA_SAO_MIGUEL; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidPico)) { return AREA_PICO; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidSantaMaria)) { return AREA_SANTA_MARIA; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidTerceira)) { return AREA_TERCEIRA; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidGranCanaria)) { return AREA_GRAN_CANARIA; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidFuerteventura)) { return AREA_FUERTEVENTURA_WITH_LOBOS; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidGomera)) { return AREA_GOMERA; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidHierro)) { return AREA_HIERRO; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidLanzaroteWithGraciosa)) { return AREA_LANZAROTE_WITH_GRACIOSA; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidLaPalma)) { return AREA_LA_PALMA; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidTenerife)) { return AREA_TENERIFE; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidIbizaWithFormentera)) { return AREA_IBIZA_WITH_FORMENTERA; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidMallorca)) { return AREA_MALLORCA; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidMenorca)) { return AREA_MENORCA; }
			
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidUssr)) { return AREA_FORMER_USSR; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidRussiaBaltic)) { return AREA_RUSSIA_BALTIC; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidRussiaSouthEast)) { return AREA_RUSSIA_SOUTHEAST; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidRussiaSouthWest)) { return AREA_RUSSIA_SOUTHWEST; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidRussiaCentral)) { return AREA_RUSSIA_CENTRAL; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidRussiaNorthern)) { return AREA_RUSSIA_NORTHERN; }
			
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidTurkey)) { return AREA_TURKEY; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidEastAegeanIslands)) { return AREA_EAST_AEGEAN_ISLANDS; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidTurkishEastAegeanIslands)) { return AREA_TURKISH_EAST_AEGEAN_ISLANDS; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidSerbiaMontenegro)) { return AREA_SERBIA_WITH_MONTENEGRO; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidSerbia)) { return AREA_SERBIA_INCLUDING_VOJVODINA_AND_WITH_KOSOVO; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidCaucasia)) { return AREA_CAUCASUS_REGION; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidUkraineAndCrimea)) { return AREA_UKRAINE_INCLUDING_CRIMEA; }
			else if (namedArea.getUuid().equals(BerlinModelTransformer.uuidAzerbaijanNakhichevan)) { return AREA_AZERBAIJAN_INCLUDING_NAKHICHEVAN; }
			
			//FE
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaAFR)) { return AREA_AFRO_TROPICAL_REGION; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaAUS)) { return AREA_AUSTRALIAN_REGION; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaEPA)) { return AREA_EAST_PALAEARCTIC; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaGR_AEG)) { return AREA_NORTH_AEGEAN_ISLANDS; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaGR_CYC)) { return AREA_CYCLADES_ISLANDS; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaGR_DOD)) { return AREA_DODECANESE_ISLANDS; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaGR_CR)) { return AREA_CRETE; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaGR_GRC)) { return AREA_GREEK_MAINLAND; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaNAF)) { return AREA_NORTHERN_AFRICA; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaNEA)) { return AREA_NEARCTIC_REGION; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaNEO)) { return AREA_NEOTROPICAL_REGION; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaNRE)) { return AREA_NEAR_EAST; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaORR)) { return AREA_ORIENTAL_REGION; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaUA)) { return AREA_UKRAINE_INCLUDING_CRIMEA; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaRU_FJL)) { return AREA_FRANZ_JOSEF_LAND; }
			else if (namedArea.getUuid().equals(FaunaEuropaeaTransformer.uuidAreaRU_NOZ)) { return AREA_NOVAYA_ZEMLYA; }
			

			//ERMS
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidEuropeanMarineWaters)) { return AREA_EUROPEAN_MARINE_WATERS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("MES") ||
					(namedArea.getUuid().equals(ErmsTransformer.uuidMediterraneanSea))) { return AREA_MEDITERRANEAN_SEA; } // abbreviated label missing
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidWhiteSea)) { return AREA_WHITE_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidNorthSea)) { return AREA_NORTH_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBalticSea)) { return AREA_BALTIC_SEA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLS") ||
					(namedArea.getUuid().equals(ErmsTransformer.uuidBlackSea))) { return AREA_BLACK_SEA; } // abbreviated label missing
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBarentsSea)) { return AREA_BARENTS_SEA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CAS") ||
					(namedArea.getUuid().equals(ErmsTransformer.uuidCaspianSea))) { return AREA_CASPIAN_SEA; } // abbreviated label missingelse if (namedArea.getUuid().equals(ErmsTransformer.uuidPortugueseExclusiveEconomicZone)) { return AREA_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBelgianExclusiveEconomicZone)) { return AREA_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidFrenchExclusiveEconomicZone)) { return AREA_FRENCH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidEnglishChannel)) { return AREA_ENGLISH_CHANNEL; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidAdriaticSea)) { return AREA_ADRIATIC_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBiscayBay)) { return AREA_BISCAY_BAY; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidDutchExclusiveEconomicZone)) { return AREA_DUTCH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidUnitedKingdomExclusiveEconomicZone)) { return AREA_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSpanishExclusiveEconomicZone)) { return AREA_SPANISH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidEgyptianExclusiveEconomicZone)) { return AREA_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidGrecianExclusiveEconomicZone)) { return AREA_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidTirrenoSea)) { return AREA_TIRRENO_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidIcelandicExclusiveEconomicZone)) { return AREA_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidIrishExclusiveeconomicZone)) { return AREA_IRISH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidIrishSea)) { return AREA_IRISH_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidItalianExclusiveEconomicZone)) { return AREA_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidNorwegianSea)) { return AREA_NORWEGIAN_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidMoroccanExclusiveEconomicZone)) { return AREA_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidNorwegianExclusiveEconomicZone)) { return AREA_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSkagerrak)) { return AREA_SKAGERRAK; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidTunisianExclusiveEconomicZone)) { return AREA_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidWaddenSea)) { return AREA_WADDEN_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBeltSea)) { return AREA_BELT_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidMarmaraSea)) { return AREA_MARMARA_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSeaofAzov)) { return AREA_SEA_OF_AZOV; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidAegeanSea)) { return AREA_AEGEAN_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBulgarianExclusiveEconomicZone)) { return AREA_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSouthBalticproper)) { return AREA_SOUTH_BALTIC_PROPER; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBalticProper)) { return AREA_BALTIC_PROPER; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidNorthBalticproper)) { return AREA_NORTH_BALTIC_PROPER; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidArchipelagoSea)) { return AREA_ARCHIPELAGO_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBothnianSea)) { return AREA_BOTHNIAN_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidGermanExclusiveEconomicZone)) { return AREA_GERMAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSwedishExclusiveEconomicZone)) { return AREA_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidUkrainianExclusiveEconomicZone)) { return AREA_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidMadeiranExclusiveEconomicZone)) { return AREA_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidLebaneseExclusiveEconomicZone)) { return AREA_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSpanishExclusiveEconomicZoneMediterraneanpart)) { return AREA_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidEstonianExclusiveEconomicZone)) { return AREA_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidCroatianExclusiveEconomicZone)) { return AREA_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBalearSea)) { return AREA_BALEAR_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidTurkishExclusiveEconomicZone)) { return AREA_TURKISH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidDanishExclusiveEconomicZone)) { return AREA_DANISH_EXCLUSIVE_ECONOMIC_ZONE; }

			
			else {
				logger.warn("Unknown NamedArea Area: " + area.getTitleCache());
			}

		}
		return null;
	}
	

	/**
	 * Returns the PESI SourceUseId for a given CDM sourceUseId.
	 * @param sourceUseId
	 * @return
	 */
	public static Integer sourceUseIdSourceUseId(Integer sourceUseId) {
		// TODO: CDM sourceUseId and PESI sourceUseId are equal for now.
		Integer result = null;
		switch (sourceUseId) {
			case 3: return ADDITIONAL_SOURCE;
			case 4: return SOURCE_OF_SYNONYMY;
			case 8: return NOMENCLATURAL_REFERENCE;
		}
		return result;
	}

	
	/**
	 * Returns the FossilStatusCache to a given Fossil.
	 * @param fossil
	 * @return
	 */
	public static String fossil2FossilStatusCache(Fossil fossil) {
		String result = null;
		return result;
	}

	/**
	 * Returns the FossilStatusId to a given Fossil.
	 * @param fossil
	 * @return
	 */
	public static Integer fossil2FossilStatusId(Fossil fossil) {
		Integer result = null;
		return result;
	}
	
	
	
	@Override
	public Object getKeyByLanguage(Language language) throws UndefinedTransformerMethodException {
		return language2LanguageId(language);
	}

	@Override
	public String getCacheByLanguage(Language language) throws UndefinedTransformerMethodException {
		if (language == null){
			return null;
		}else{
			return this.languageCacheMap.get(getKeyByLanguage(language)); 
		}
	}

	
	/**
	 * Returns the identifier of the given Language.
	 * @param language
	 * @return
	 */
	public static Integer language2LanguageId(Language language) {
		if (language == null ) {
			return null;
		}
		if (language.equals(Language.ALBANIAN())) {
			return Language_Albanian;
		} else if (language.equals(Language.ARABIC())) {
			return Language_Arabic;
		} else if (language.equals(Language.ARMENIAN())) {
			return Language_Armenian;
		} else if (language.equals(Language.AZERBAIJANI())) {
			return Language_Azerbaijan;
		} else if (language.equals(Language.BELORUSSIAN())) {
			return Language_Belarusian;
		} else if (language.equals(Language.BULGARIAN())) {
			return Language_Bulgarian;
		} else if (language.equals(Language.CATALAN_VALENCIAN())) {
			return Language_Catalan;
		} else if (language.equals(Language.CROATIAN())) {
			return Language_Croat;
		} else if (language.equals(Language.CZECH())) {
			return Language_Czech;
		} else if (language.equals(Language.DANISH())) {
			return Language_Danish;
		} else if (language.equals(Language.DUTCH_MIDDLE())) {
			return Language_Dutch;
		} else if (language.equals(Language.ENGLISH())) {
			return Language_English;
//		} else if (language.equals(Language.)) {
//			return Language_Euskera;
		} else if (language.equals(Language.ESTONIAN())) {
			return Language_Estonian;
		} else if (language.equals(Language.FINNISH())) {
			return Language_Finnish;
		} else if (language.equals(Language.FRENCH())) {
			return Language_French;
		} else if (language.equals(Language.GEORGIAN())) {
			return Language_Georgian;
		} else if (language.equals(Language.GERMAN())) {
			return Language_German;
		} else if (language.equals(Language.GREEK_MODERN())) {
			return Language_Greek;
		} else if (language.equals(Language.HUNGARIAN())) {
			return Language_Hungarian;
		} else if (language.equals(Language.ICELANDIC())) {
			return Language_Icelandic;
		} else if (language.equals(Language.IRISH())) {
			return Language_Irish_Gaelic;
		} else if (language.equals(Language.HEBREW())) {
			return Language_Israel_Hebrew;
		} else if (language.equals(Language.ITALIAN())) {
			return Language_Italian;
		} else if (language.equals(Language.LATVIAN())) {
			return Language_Latvian;
		} else if (language.equals(Language.LITHUANIAN())) {
			return Language_Lithuanian;
		} else if (language.equals(Language.MACEDONIAN())) {
			return Language_Macedonian;
		} else if (language.equals(Language.MALTESE())) {
			return Language_Maltese;
		} else if (language.equals(Language.MOLDAVIAN())) {
			return Language_Moldovian;
		} else if (language.equals(Language.NORWEGIAN())) {
			return Language_Norwegian;
		} else if (language.equals(Language.POLISH())) {
			return Language_Polish;
		} else if (language.equals(Language.PORTUGUESE())) {
			return Language_Portuguese;
		} else if (language.equals(Language.ROMANIAN())) {
			return Language_Roumanian;
		} else if (language.equals(Language.RUSSIAN())) {
			return Language_Russian;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Caucasian;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Altaic_kalmyk_oirat;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Altaic_karachay_balkar;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Altaic_kumyk;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Altaic_nogai;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Altaic_north_azerbaijani;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Indo_european_russian;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Indo_european_kalmyk_oirat;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Indo_european_osetin;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_abaza;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_adyghe;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_chechen;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_kabardian;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_lak;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_avar;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_in;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Uralic_chuvash;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Uralic_udmurt;
		} else if (language.equals(Language.SERBIAN())) {
			return Language_Serbian;
		} else if (language.equals(Language.SLOVAK())) {
			return Language_Slovak;
		} else if (language.equals(Language.SLOVENIAN())) {
			return Language_Slovene;
		} else if (language.equals(Language.SPANISH_CASTILIAN())) {
			return Language_Spanish_Castillian;
		} else if (language.equals(Language.SWEDISH())) {
			return Language_Swedish;
		} else if (language.equals(Language.TURKISH())) {
			return Language_Turkish;
		} else if (language.equals(Language.UKRAINIAN())) {
			return Language_Ukraine;
		} else if (language.equals(Language.WELSH())) {
			return Language_Welsh;
		} else if (language.equals(Language.CORSICAN())) {
			return Language_Corsican;
		} else {
			logger.debug("Unknown Language: " + language.getTitleCache());
			return null;
		}
	}
	
	/**
	 * Returns the NodeCategoryCache for a given TextData.
	 * @param feature
	 * @return
	 */
	public String getCacheByFeature(Feature feature) {
		if (feature == null){
			return null;
		}else{
			return this.featureCacheMap.get(feature2NoteCategoryFk(feature)); 
		}
	}

	/**
	 * Returns the NodeCategoryFk for a given TextData.
	 * @param feature
	 * @return
	 */
	public static Integer feature2NoteCategoryFk(Feature feature) {
		if (feature == null) {
			return null;
		}

		if (feature.equals(Feature.DESCRIPTION())) {
			return NoteCategory_description;
		} else if (feature.equals(Feature.ECOLOGY())) {
			return NoteCategory_ecology;
		} else if (feature.equals(Feature.PHENOLOGY())) {
			return NoteCategory_phenology;
		} else if (feature.equals(Feature.COMMON_NAME())) {
			return NoteCategory_Common_names;
		} else if (feature.equals(Feature.OCCURRENCE())) {
			return NoteCategory_Occurrence;
		} else if (feature.equals(Feature.DISTRIBUTION())) {
			return NoteCategory_Distribution;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidRemark)) {
			return NoteCategory_Remark;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidAdditionalinformation)) {
			return NoteCategory_Additional_information;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidSpelling)) {
			return NoteCategory_Spelling;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidDateofPublication)) {
			return NoteCategory_Date_of_publication;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidSystematics)) {
			return NoteCategory_Systematics;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidClassification)) {
			return NoteCategory_Classification;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidEnvironment)) {
			return NoteCategory_Environment;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidHabitat)) {
			return NoteCategory_Habitat;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidAuthority)) {
			return NoteCategory_Authority;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidMorphology)) {
			return NoteCategory_Morphology;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidTaxonomicRemarks)) {
			return NoteCategory_Taxonomic_Remarks;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidNote)){
			return NoteCategory_Note;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidTaxonomy)) {
			return NoteCategory_Taxonomy;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidTaxonomicstatus)) {
			return NoteCategory_Taxonomic_status;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidStatus)){
			return NoteCategory_Status;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidRank)) {
			return NoteCategory_Rank;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidHomonymy)) {
			return NoteCategory_Homonymy;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidNomenclature)) {
			return NoteCategory_Nomenclature;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidTaxonomicRemark)) {
			return NoteCategory_Taxonomic_Remark;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidAcknowledgments)){
			return NoteCategory_Acknowledgments;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidOriginalpublication)) {
			return NoteCategory_Original_publication;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidTypelocality)) {
			return NoteCategory_Type_locality;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidValidity)) {
			return NoteCategory_Validity;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidIdentification)) {
			return NoteCategory_Identification;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidSynonymy)) {
			return NoteCategory_Synonymy;
		} else if (feature.equals(Feature.CITATION())) {
			return null;  //citations are handled differently
		} else if (feature.getUuid().equals(BerlinModelTransformer.uuidFeatureMaps)){
			return NoteCategory_Link_to_maps;
		} else if (feature.getUuid().equals(BerlinModelTransformer.uuidFeatureUse)){
			return NoteCategory_Use;
		} else if (feature.getUuid().equals(BerlinModelTransformer.uuidFeatureComments)){
			return NoteCategory_Comments;
		} else if (feature.getUuid().equals(BerlinModelTransformer.uuidFeatureConservationStatus)){
			return NoteCategory_Conservation_Status;
		
		//E+M
		} else if (feature.getUuid().equals(BerlinModelTransformer.uuidFeatureDistrEM)){
			return NoteCategory_general_distribution_euromed;
		} else if (feature.getUuid().equals(BerlinModelTransformer.uuidFeatureDistrWorld)){
			return NoteCategory_general_distribution_world;
		} else if (feature.getUuid().equals(BerlinModelTransformer.uuidFeatureEditorBrackets)){
			return NoteCategory_Editor_Brackets;
		} else if (feature.getUuid().equals(BerlinModelTransformer.uuidFeatureEditorParenthesis)){
			return NoteCategory_Editor_Parenthesis;
		} else if (feature.getUuid().equals(BerlinModelTransformer.uuidFeatureInedited)){
			return NoteCategory_Inedited;
		} else if (feature.getUuid().equals(BerlinModelTransformer.uuidFeatureCommentsEditing)){
			return NoteCategory_Comments_on_editing_process;
			
			
			// TODO: Unknown NoteCategories
//			NoteCategory_Common_names = 12;
//			NoteCategory_Maps =14;

//			NoteCategory_Link_to_images = 21;
//			NoteCategory_Link_to_taxonomy = 22;
//			NoteCategory_Link_to_general_information = 23;
//			NoteCategory_undefined_link = 24;
//			NoteCategory_Editor_Braces = 249;

//			NoteCategory_Publication_date = 254;
//			NoteCategory_Distribution = 278;
//			NoteCategory_Biology = 281;
//			NoteCategory_Diagnosis	= 282;
//			NoteCategory_Host = 283;

		}else{
			logger.warn("Unhandled Feature: " + feature.getTitleCache());
			return null;
		}
	}

	/**
	 * Returns the string representation for a given rank.
	 * @param rank
	 * @param pesiKingdomId
	 * @return
	 */
	public String getCacheByRankAndKingdom(Rank rank, Integer pesiKingdomId) {
		if (rank == null){
			return null;
		}else{
			return this.rankCacheMap.get(pesiKingdomId).get(rank2RankId(rank, pesiKingdomId)); 
		}
	}
	
	/**
	 * Returns the abbreviation for a given rank.
	 * Currently unused.
	 * @param rank
	 * @param pesiKingdomId
	 * @return
	 */
	public String getCacheAbbrevByRankAndKingdom(Rank rank, Integer pesiKingdomId) {
		if (rank == null){
			return null;
		}else{
			return this.rankAbbrevCacheMap.get(pesiKingdomId).get(rank2RankId(rank, pesiKingdomId)); 
		}
	}

	/**
	 * Returns the identifier of a PESI specific kingdom for a given CDM nomenclatural code.
	 * @param nomenclaturalCode
	 * @return KINGDOM_ANIMALIA for NomenclaturalCode.ICZN, KINGDOM_PLANTAE for NomenclaturalCode.ICBN
	 */
	public static Integer nomenClaturalCode2Kingdom(NomenclaturalCode nomenclaturalCode) {
		Integer result = null;
		// TODO: This needs to be refined. For now we differentiate between Animalia and Plantae only.
		if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)) {
			result = KINGDOM_ANIMALIA;
		} else if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)) {
			result = KINGDOM_PLANTAE;
		} else if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)) {
			result = KINGDOM_BACTERIA;
//		} else if (nomenclaturalCode.equals(NomenclaturalCode.)) { // Biota
//			result = 
		} else {
			logger.error("NomenclaturalCode not yet considered: " + nomenclaturalCode.getUuid() + " (" +  nomenclaturalCode.getTitleCache() + ")");
		}
		return result;
	}
	
	/**
	 * Returns the RankId for a Rank.
	 * @param rank
	 * @return
	 */
	public static Integer rank2RankId (Rank rank, Integer pesiKingdomId) {
		Integer result = null;
		if (rank == null) {
			return null;
		}
		
		// We differentiate between Animalia and Plantae only for now.
		if (pesiKingdomId != null && pesiKingdomId.intValue() == KINGDOM_ANIMALIA) {
			if (rank.equals(Rank.KINGDOM())) {
				result = Animalia_Kingdom;
			} else if (rank.equals(Rank.SUBKINGDOM())) {
				result = Animalia_Subkingdom;
			} else if (rank.equals(Rank.SUPERPHYLUM())) {
				result = Animalia_Superphylum;
			} else if (rank.equals(Rank.PHYLUM())) {
				result = Animalia_Phylum;
			} else if (rank.equals(Rank.SUBPHYLUM())) {
				result = Animalia_Subphylum;
			} else if (rank.equals(Rank.INFRAPHYLUM())) {
				result = Animalia_Infraphylum;
			} else if (rank.equals(Rank.SUPERCLASS())) {
				result = Animalia_Superclass;
			} else if (rank.equals(Rank.CLASS())) {
				result = Animalia_Class;
			} else if (rank.equals(Rank.SUBCLASS())) {
				result = Animalia_Subclass;
			} else if (rank.equals(Rank.INFRACLASS())) {
				result = Animalia_Infraclass;
			} else if (rank.equals(Rank.SUPERORDER())) {
				result = Animalia_Superorder;
			} else if (rank.equals(Rank.ORDER())) {
				result = Animalia_Order;
			} else if (rank.equals(Rank.SUBORDER())) {
				result = Animalia_Suborder;
			} else if (rank.equals(Rank.INFRAORDER())) {
				result = Animalia_Infraorder;
			} else if (rank.equals(Rank.SECTION_ZOOLOGY())) {
				result = Animalia_Section;
			} else if (rank.equals(Rank.SUBSECTION_ZOOLOGY())) {
				result = Animalia_Subsection;
			} else if (rank.equals(Rank.SUPERFAMILY())) {
				result = Animalia_Superfamily;
			} else if (rank.equals(Rank.FAMILY())) {
				result = Animalia_Family;
			} else if (rank.equals(Rank.SUBFAMILY())) {
				result = Animalia_Subfamily;
			} else if (rank.equals(Rank.TRIBE())) {
				result = Animalia_Tribe;
			} else if (rank.equals(Rank.SUBTRIBE())) {
				result = Animalia_Subtribe;
			} else if (rank.equals(Rank.GENUS())) {
				result = Animalia_Genus;
			} else if (rank.equals(Rank.SUBGENUS())) {
				result = Animalia_Subgenus;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Animalia_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Animalia_Subspecies;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Animalia_Natio;
			} else if (rank.equals(Rank.VARIETY())) {
				result = Animalia_Variety;
			} else if (rank.equals(Rank.SUBVARIETY())) {
				result = Animalia_Subvariety;
			} else if (rank.equals(Rank.FORM())) {
				result = Animalia_Forma;
			} else {
				//TODO Exception
				logger.warn("Rank for Kingdom Animalia not yet supported in CDM: "+ rank.getLabel());
				return null;
			}
		} else if (pesiKingdomId != null && pesiKingdomId.intValue() == KINGDOM_PLANTAE) {
			if (rank.equals(Rank.KINGDOM())) {
				result = Plantae_Kingdom;
			} else if (rank.equals(Rank.SUBKINGDOM())) {
				result = Plantae_Subkingdom;
			} else if (rank.equals(Rank.DIVISION())) {
				result = Plantae_Division;
			} else if (rank.equals(Rank.SUBDIVISION())) {
				result = Plantae_Subdivision;
			} else if (rank.equals(Rank.CLASS())) {
				result = Plantae_Class;
			} else if (rank.equals(Rank.SUBCLASS())) {
				result = Plantae_Subclass;
			} else if (rank.equals(Rank.ORDER())) {
				result = Plantae_Order;
			} else if (rank.equals(Rank.SUBORDER())) {
				result = Plantae_Suborder;
			} else if (rank.equals(Rank.FAMILY())) {
				result = Plantae_Family;
			} else if (rank.equals(Rank.SUBFAMILY())) {
				result = Plantae_Subfamily;
			} else if (rank.equals(Rank.TRIBE())) {
				result = Plantae_Tribe;
			} else if (rank.equals(Rank.SUBTRIBE())) {
				result = Plantae_Subtribe;
			} else if (rank.equals(Rank.GENUS())) {
				result = Plantae_Genus;
			} else if (rank.equals(Rank.SUBGENUS())) {
				result = Plantae_Subgenus;
			} else if (rank.equals(Rank.SECTION_BOTANY())) {
				result = Plantae_Section;
			} else if (rank.equals(Rank.SUBSECTION_BOTANY())) {
				result = Plantae_Subsection;
			} else if (rank.equals(Rank.SERIES())) {
				result = Plantae_Series;
			} else if (rank.equals(Rank.SUBSERIES())) {
				result = Plantae_Subseries;
			} else if (rank.equals(Rank.SPECIESAGGREGATE() )) {
				result = Plantae_Aggregate;
			} else if (rank.equals(Rank.SPECIESGROUP())) {
				logger.warn("Rank Species Group not yet implemented");
				result = null;
			} else if (rank.getUuid().equals(BerlinModelTransformer.uuidRankCollSpecies)) { 
				result = Plantae_Coll_Species;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Plantae_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Plantae_Subspecies;
			} else if (rank.equals(Rank.GREX())) {
				result = Plantae_Grex;
			} else if (rank.getUuid().equals(BerlinModelTransformer.uuidRankProles) ) {
				result = Plantae_Proles;
			} else if (rank.getUuid().equals(BerlinModelTransformer.uuidRankRace)) {
				result = Plantae_Race;
			} else if (rank.equals(Rank.CONVAR())) {
				result = Plantae_Convarietas;
			} else if (rank.equals(Rank.VARIETY())) {
				result = Plantae_Variety;
			} else if (rank.equals(Rank.SUBVARIETY())) {
				result = Plantae_Subvariety;
			} else if (rank.equals(Rank.FORM())) {
				result = Plantae_Forma;
			} else if (rank.equals(Rank.SUBFORM())) {
				result = Plantae_Subforma;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Forma_spec;
			} else if (rank.equals(Rank.INFRAGENERICTAXON())) { 
				result = Plantae_Taxa_infragen;
			} else if (rank.equals(Rank.INFRASPECIFICTAXON())) { 
				result = Plantae_Taxa_infraspec;
			} else {
				//TODO Exception
				logger.warn("Rank for Kingdom Plantae not yet supported in CDM: "+ rank.getLabel());
				return null;
			}
		} else if (pesiKingdomId != null && pesiKingdomId.intValue() == KINGDOM_BACTERIA) {
			if (rank.equals(Rank.KINGDOM())) { result = Bacteria_Kingdom; }
			else if (rank.equals(Rank.SUBKINGDOM())) { result = Bacteria_Subkingdom; }
			else if (rank.equals(Rank.PHYLUM())) { result = Bacteria_Phylum; }
			else if (rank.equals(Rank.SUBPHYLUM())) { result = Bacteria_Subphylum; }
			else if (rank.equals(Rank.SUPERCLASS())) { result = Bacteria_Superclass; }
			else if (rank.equals(Rank.CLASS())) { result = Bacteria_Class; }
			else if (rank.equals(Rank.SUBCLASS())) { result = Bacteria_Subclass; }
			else if (rank.equals(Rank.INFRACLASS())) { result = Bacteria_Infraclass; }
			else if (rank.equals(Rank.SUPERORDER())) { result = Bacteria_Superorder; }
			else if (rank.equals(Rank.ORDER())) { result = Bacteria_Order; }
			else if (rank.equals(Rank.SUBORDER())) { result = Bacteria_Suborder; }
			else if (rank.equals(Rank.INFRAORDER())) { result = Bacteria_Infraorder; }
			else if (rank.equals(Rank.SUPERFAMILY())) { result = Bacteria_Superfamily; }
			else if (rank.equals(Rank.FAMILY())) { result = Bacteria_Family; }
			else if (rank.equals(Rank.SUBFAMILY())) { result = Bacteria_Subfamily; }
			else if (rank.equals(Rank.TRIBE())) { result = Bacteria_Tribe; }
			else if (rank.equals(Rank.SUBTRIBE())) { result = Bacteria_Subtribe; }
			else if (rank.equals(Rank.GENUS())) { result = Bacteria_Genus; }
			else if (rank.equals(Rank.SUBGENUS())) { result = Bacteria_Subgenus; }
			else if (rank.equals(Rank.SPECIES())) { result = Bacteria_Species; }
			else if (rank.equals(Rank.SUBSPECIES())) { result = Bacteria_Subspecies; }
			else if (rank.equals(Rank.VARIETY())) { result = Bacteria_Variety; }
			else if (rank.equals(Rank.FORM())) { result = Bacteria_Forma; }
		} else {
			//TODO Exception
			logger.warn("Kingdom not yet supported in CDM: "+ pesiKingdomId);
			return null;
		}
		return result;
	}

	/**
	 * 
	 * @param nameTypeDesignationStatus
	 * @return
	 */
	public static Integer nameTypeDesignationStatus2TypeDesignationStatusId(NameTypeDesignationStatus nameTypeDesignationStatus) {
		if (nameTypeDesignationStatus == null) {
			return null;
		}
		if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.ORIGINAL_DESIGNATION())) {
			return TYPE_BY_ORIGINAL_DESIGNATION;
		} else if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION())) {
			return TYPE_BY_SUBSEQUENT_DESIGNATION;
		} else if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.MONOTYPY())) {
			return TYPE_BY_MONOTYPY;
		} else {
			//TODO Figure out a way to handle this gracefully.
			logger.warn("Name Type Designation Status not yet supported in PESI: "+ nameTypeDesignationStatus.getLabel());
			return null;
		}

	}

	/**
	 * 
	 * @param nameTypeDesignationStatus
	 * @return
	 */
	public static String nameTypeDesignationStatus2TypeDesignationStatusCache(NameTypeDesignationStatus nameTypeDesignationStatus) {
		if (nameTypeDesignationStatus == null) {
			return null;
		}
		if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.ORIGINAL_DESIGNATION())) {
			return TYPE_STR_BY_ORIGINAL_DESIGNATION;
		} else if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION())) {
			return TYPE_STR_BY_SUBSEQUENT_DESIGNATION;
		} else if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.MONOTYPY())) {
			return TYPE_STR_BY_MONOTYPY;
		} else {
			//TODO Figure out a way to handle this gracefully.
			logger.warn("Name Type Designation Status not yet supported in PESI: "+ nameTypeDesignationStatus.getLabel());
			return null;
		}

	}

	/**
	 * @see PesiTaxonExport#doPhaseUpdates(PesiExportState) for further transformation
	 * @param taxonBase
	 * @return
	 */
	public static Integer taxonBase2statusFk (TaxonBase<?> taxonBase){
		if (taxonBase == null){
			return null;
		}		
		if (taxonBase.isInstanceOf(Taxon.class)){
			Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
			if (taxon.getTaxonNodes().size() == 0){
				return T_STATUS_NOT_ACCEPTED;
			}else{
				return T_STATUS_ACCEPTED;
			}
		}else if (taxonBase.isInstanceOf(Synonym.class)){
			return T_STATUS_SYNONYM;
		}else{
			logger.warn("Unknown ");
			return T_STATUS_UNRESOLVED;
		}
		//TODO 
//		public static int T_STATUS_PARTIAL_SYN = 3;
//		public static int T_STATUS_PRO_PARTE_SYN = 4;
//		public static int T_STATUS_UNRESOLVED = 5;
//		public static int T_STATUS_ORPHANED = 6;
	}

//	/**
//	 * 
//	 * @param taxonBase
//	 * @return
//	 */
//	public static String taxonBase2statusCache (TaxonBase<?> taxonBase){
//		if (taxonBase == null){return null;}
//		if (taxonBase.isInstanceOf(Taxon.class)){
//			Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
//			if (taxon.getTaxonNodes().size() == 0){
//				return T_STATUS_STR_NOT_ACCEPTED;
//			}else{
//				return T_STATUS_STR_ACCEPTED;
//			}
//		}else if (taxonBase.isInstanceOf(Synonym.class)){
//			return T_STATUS_STR_SYNONYM;
//		}else{
//			logger.warn("Unknown ");
//			return T_STATUS_STR_UNRESOLVED;
//		}
//		//TODO 
//		public static int T_STATUS_STR_PARTIAL_SYN = 3;
//		public static int T_STATUS_STR_PRO_PARTE_SYN = 4;
//		public static int T_STATUS_STR_UNRESOLVED = 5;
//		public static int T_STATUS_STR_ORPHANED = 6;
//	}
		
	/**
	 * Returns the {@link SourceCategory SourceCategory} representation of the given {@link ReferenceType ReferenceType} in PESI.
	 * @param reference The {@link Reference Reference}.
	 * @return The {@link SourceCategory SourceCategory} representation in PESI.
	 */
	public static Integer reference2SourceCategoryFK(Reference<?> reference) {
		if (reference == null){
			return null;
		} else if (reference.getType().equals(ReferenceType.Article)) {
			return REF_ARTICLE_IN_PERIODICAL;
		} else if (reference.getType().equals(ReferenceType.Book)) {
			return REF_BOOK;
		} else if (reference.getType().equals(ReferenceType.BookSection)) {
			return REF_PART_OF_OTHER;
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
			logger.warn("Reference type not yet supported in PESI: "+ reference.getType());
			return null;
		}
	}
	
	/**
	 * Returns the {@link SourceCategoryCache SourceCategoryCache}.
	 * @param reference The {@link Reference Reference}.
	 * @return The {@link SourceCategoryCache SourceCategoryCache}.
	 */
	public String getCacheByReference(Reference<?> reference) {
		if (reference == null){
			return null;
		}else{
			return this.sourceCategoryCacheMap.get(reference2SourceCategoryFK(reference)); 
		}
	}

	/**
	 * 
	 * @param status
	 * @return
	 */
	public String getCacheByNomStatus(NomenclaturalStatusType status) {
		if (status == null){
			return null;
		}else{
			return this.nameStatusCacheMap.get(nomStatus2nomStatusFk(status)); 
		}
	}
	
	/**
	 * 
	 * @param status
	 * @return
	 */
	public static Integer nomStatus2nomStatusFk (NomenclaturalStatusType status){
		if (status == null){
			return null;
		}
		if (status.equals(NomenclaturalStatusType.INVALID())) {return NAME_ST_NOM_INVAL;
		}else if (status.equals(NomenclaturalStatusType.ILLEGITIMATE())) {return NAME_ST_NOM_ILLEG;
		}else if (status.equals(NomenclaturalStatusType.NUDUM())) {return NAME_ST_NOM_NUD;
		}else if (status.equals(NomenclaturalStatusType.REJECTED())) {return NAME_ST_NOM_REJ;
		}else if (status.equals(NomenclaturalStatusType.REJECTED_PROP())) {return NAME_ST_NOM_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED())) {return NAME_ST_NOM_UTIQUE_REJ;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED_PROP())) {return NAME_ST_NOM_UTIQUE_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.CONSERVED())) {return NAME_ST_NOM_CONS;
		
		}else if (status.equals(NomenclaturalStatusType.CONSERVED_PROP())) {return NAME_ST_NOM_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED())) {return NAME_ST_ORTH_CONS;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP())) {return NAME_ST_ORTH_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.SUPERFLUOUS())) {return NAME_ST_NOM_SUPERFL;
		}else if (status.equals(NomenclaturalStatusType.AMBIGUOUS())) {return NAME_ST_NOM_AMBIG;
		}else if (status.equals(NomenclaturalStatusType.PROVISIONAL())) {return NAME_ST_NOM_PROVIS;
		}else if (status.equals(NomenclaturalStatusType.DOUBTFUL())) {return NAME_ST_NOM_DUB;
		}else if (status.equals(NomenclaturalStatusType.NOVUM())) {return NAME_ST_NOM_NOV;
		
		}else if (status.equals(NomenclaturalStatusType.CONFUSUM())) {return NAME_ST_NOM_CONFUS;
		}else if (status.equals(NomenclaturalStatusType.ALTERNATIVE())) {return NAME_ST_NOM_ALTERN;
		}else if (status.equals(NomenclaturalStatusType.COMBINATION_INVALID())) {return NAME_ST_COMB_INVAL;
		}else if (status.equals(NomenclaturalStatusType.LEGITIMATE())) {return NAME_ST_LEGITIMATE;
		
		}else if (status.getUuid().equals(BerlinModelTransformer.uuidNomStatusCombIned)) {return NAME_ST_COMB_INED;
		}else if (status.getUuid().equals(BerlinModelTransformer.uuidNomStatusNomOrthCons)) {return NAME_ST_NOM_AND_ORTH_CONS;
		}else if (status.getUuid().equals(BerlinModelTransformer.uuidNomStatusSpNovIned)) {return NAME_ST_SP_NOV_INED;
		
		
		// The following are non-existent in CDM
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_COMB_AND_STAT_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_NOM_NOV_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_ALTERNATE_REPRESENTATION;
		}else if (status.getUuid().equals(FaunaEuropaeaTransformer.uuidNomStatusTempNamed)) {return NAME_ST_TEMPORARY_NAME;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_SPECIES_INQUIRENDA;

		//TODO
		}else {
			//TODO Exception
			logger.warn("NomStatus type not yet supported by PESI export: "+ status);
			return null;
		}
	}
	
	/**
	 * Returns the RelTaxonQualifierCache for a given taxonRelation.
	 * @param relation
	 * @return
	 */
	public String getCacheByRelationshipType(RelationshipBase relation, NomenclaturalCode code){
		if (relation == null){
			return null;
		}else{
			String result; 
			Integer key = taxonRelation2RelTaxonQualifierFk(relation);
			if (code.equals(NomenclaturalCode.ICZN)){
				result = this.taxRelZooQualifierCacheMap.get(key);
				if (result == null){
					this.taxRelQualifierCacheMap.get(key); 
				}
			}else{
				result = this.taxRelQualifierCacheMap.get(key); 
			}
			return result;
		}
	}
	
	/**
	 * Returns the RelTaxonQualifierFk for a TaxonRelation.
	 * @param relation
	 * @return
	 */
	public static Integer taxonRelation2RelTaxonQualifierFk(RelationshipBase<?,?,?> relation) {
		if (relation == null) {
			return null;
		}
		RelationshipTermBase<?> type = relation.getType();
		if (type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())) {
			return IS_MISAPPLIED_NAME_FOR;
		} else if (type.equals(SynonymRelationshipType.SYNONYM_OF())) {
			SynonymRelationship synRel = CdmBase.deproxy(relation, SynonymRelationship.class);
			if (synRel.isProParte()){
				return IS_PRO_PARTE_SYNONYM_OF;
			}else if (synRel.isPartial()){
				return IS_PARTIAL_SYNONYM_OF;
			}else{
				return IS_SYNONYM_OF;
			}
		} else if (type.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
			SynonymRelationship synRel = CdmBase.deproxy(relation, SynonymRelationship.class);
			if (synRel.isProParte()){
				return IS_PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF;
			}else if (synRel.isPartial()){
				return IS_PARTIAL_AND_HOMOTYPIC_SYNONYM_OF;
			}else{
				return IS_HOMOTYPIC_SYNONYM_OF;
			}
		} else if (type.equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())) {
			SynonymRelationship synRel = CdmBase.deproxy(relation, SynonymRelationship.class);
			if (synRel.isProParte()){
				return IS_PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF;
			}else if (synRel.isPartial()){
				return IS_PARTIAL_AND_HETEROTYPIC_SYNONYM_OF;
			}else{
				return IS_HETEROTYPIC_SYNONYM_OF;
			}
		} else if (type.equals(SynonymRelationshipType.INFERRED_EPITHET_OF())) {
			return IS_INFERRED_EPITHET_FOR;
		} else if (type.equals(SynonymRelationshipType.INFERRED_GENUS_OF())) {
			return IS_INFERRED_GENUS_FOR;
		} else if (type.equals(SynonymRelationshipType.POTENTIAL_COMBINATION_OF())) {
			return IS_POTENTIAL_COMBINATION_FOR;
		} else if (type.equals(NameRelationshipType.BASIONYM())) {
			return IS_BASIONYM_FOR;
		} else if (type.equals(NameRelationshipType.LATER_HOMONYM())) {
			return IS_LATER_HOMONYM_OF;
		} else if (type.equals(NameRelationshipType.REPLACED_SYNONYM())) {
			return IS_REPLACED_SYNONYM_FOR;
		} else if (type.equals(NameRelationshipType.VALIDATED_BY_NAME())) {
			return IS_VALIDATION_OF;
		} else if (type.equals(NameRelationshipType.LATER_VALIDATED_BY_NAME())) {
			return IS_LATER_VALIDATION_OF;
		} else if (type.equals(NameRelationshipType.CONSERVED_AGAINST())) {
			return IS_CONSERVED_AGAINST;
		} else if (type.equals(NameRelationshipType.TREATED_AS_LATER_HOMONYM())) {
			return IS_TREATED_AS_LATER_HOMONYM_OF;
		} else if (type.equals(NameRelationshipType.ORTHOGRAPHIC_VARIANT())) {
			return IS_ORTHOGRAPHIC_VARIANT_OF;
		} else if (type.equals(NameRelationshipType.ALTERNATIVE_NAME())) {
			return IS_ALTERNATIVE_NAME_FOR;
		} else if (type.equals(HybridRelationshipType.FEMALE_PARENT())) {
			return IS_FEMALE_PARENT_OF;
		} else if (type.equals(HybridRelationshipType.MALE_PARENT())) {
			return IS_MALE_PARENT_OF;
		} else if (type.equals(HybridRelationshipType.FIRST_PARENT())) {
			return IS_FIRST_PARENT_OF;
		} else if (type.equals(HybridRelationshipType.SECOND_PARENT())) {
			return IS_SECOND_PARENT_OF;
		} else if (type.getUuid().equals(ErmsTransformer.uuidTaxRelTypeIsTaxonSynonymOf)) {
			return IS_SYNONYM_OF;
		} else {
			logger.warn("No equivalent RelationshipType found in datawarehouse for: " + type.getTitleCache());
		}

		// The following have no equivalent attribute in CDM
//		IS_TYPE_OF
//		IS_CONSERVED_TYPE_OF
//		IS_REJECTED_TYPE_OF
//		IS_REJECTED_IN_FAVOUR_OF
//		HAS_SAME_TYPE_AS
//		IS_LECTOTYPE_OF
//		TYPE_NOT_DESIGNATED


		return null;
	}
	
	/**
	 * Returns the StatusFk for a given StatusCache.
	 * @param StatusCache
	 * @return
	 */
	public Integer StatusCache2StatusFk(String StatusCache) {
		Integer result = null;
		if (StatusCache.equalsIgnoreCase("Checked by Taxonomic Editor: included in ERMS 1.1")) {
			return 0;
		} else if (StatusCache.equalsIgnoreCase("Added by Database Management Team")) {
			return 2;
		} else if (StatusCache.equalsIgnoreCase("Checked by Taxonomic Editor")) {
			return 3;
		} else if (StatusCache.equalsIgnoreCase("Edited by Database Management Team")) {
			return 4;
		} else {
			logger.error("StatusFk could not be determined. StatusCache unknown: " + StatusCache);
		}
		
		return result;
	}
	
	/**
	 * Returns the FossilStatusFk for a given FossilStatusCache.
	 * @param fossilStatusCache
	 * @return
	 */
	public Integer FossilStatusCache2FossilStatusFk(String fossilStatusCache) {
		Integer result = null;
		if (fossilStatusCache.equalsIgnoreCase("recent only")) {
			return 1;
		} else if (fossilStatusCache.equalsIgnoreCase("fossil only")) {
			return 2;
		} else if (fossilStatusCache.equalsIgnoreCase("recent + fossil")) {
			return 3;
		} else {
			logger.error("FossilStatusFk could not be determined. FossilStatusCache unknown: " + fossilStatusCache);
		}
		
		return result;
	}

	/**
	 * Returns the NoteCategoryFk for a given UUID representing an ExtensionType.
	 * @param uuid
	 * @return
	 */
	public static Integer getNoteCategoryFk(UUID uuid) {
		Integer result = null;
		if (uuid.equals(taxCommentUuid)) {
			result = 270;
		} else if (uuid.equals(fauCommentUuid)) {
			result = 281;
		} else if (uuid.equals(fauExtraCodesUuid)) {
			result = 278;
		}
		return result;
	}
	
	/**
	 * Returns the NoteCategoryCache for a given UUID representing an ExtensionType.
	 * @param uuid
	 * @return
	 */
	public static String getNoteCategoryCache(UUID uuid) {
		String result = null;
		if (uuid.equals(taxCommentUuid)) {
			result = "Taxonomy";
		} else if (uuid.equals(fauCommentUuid)) {
			result = "Biology";
		} else if (uuid.equals(fauExtraCodesUuid)) {
			result = "Distribution";
		}
		return result;
	}

	public static Integer getQualityStatusKeyBySource(BitSet sources, TaxonNameBase<?,?> taxonName) {
		if (sources.get(SOURCE_EM)){
			return QUALITY_STATUS_ADD_BY_DBMT;
		}else if (sources.get(SOURCE_ERMS)){
			Set<String> statusSet = getAllQualityStatus(taxonName);
			if (statusSet.size() > 1){
				logger.warn("ERMS TaxonName has more than 1 quality status: " + taxonName.getTitleCache() + "; lisd=" + taxonName.getLsid());
			}
			if (statusSet.contains("Checked by Taxonomic Editor: included in ERMS 1.1")){
				return QUALITY_STATUS_CHECKED_EDITOR_ERMS_1_1;
			}else if (statusSet.contains("Added by Database Management Team")){
				return QUALITY_STATUS_ADD_BY_DBMT;
			}else if (statusSet.contains("Checked by Taxonomic Editor")){
				return QUALITY_STATUS_CHECKED_EDITOR;
			}else if (statusSet.contains("Edited by Database Management Team")){
				return QUALITY_STATUS_EDITED_BY_DBMT;
			}else{
				logger.warn("Unknown ERMS quality status: " + statusSet.iterator().next() + " for taxon name " + taxonName.getTitleCache());
				return null;
			}
		}else{
			return null;   // TODO needs to be implemented for others 
		}
	}

	
	private static Set<String> getAllQualityStatus(TaxonNameBase<?, ?> taxonName) {
		Set<String> result = new HashSet<String>();
		for (TaxonBase<?> taxonBase : taxonName.getTaxonBases()){
			result.addAll(taxonBase.getExtensions(ErmsTransformer.uuidQualityStatus));
		}
		return result;
	}

	@Override
	public String getQualityStatusCacheByKey(Integer qualityStatusId) throws UndefinedTransformerMethodException {
		if (qualityStatusId == null){
			return null;
		}else{
			return this.qualityStatusCacheMap.get(qualityStatusId); 
		}
	}
	

	public Object getSourceUseCacheByKey(Integer sourceUseFk) {
		if (sourceUseFk == null){
			return null;
		}else{
			return this.sourceUseCacheMap.get(sourceUseFk); 
		}
	}
	
	@Override
	public String getTaxonStatusCacheByKey(Integer taxonStatusId) throws UndefinedTransformerMethodException {
		if (taxonStatusId == null){
			return null;
		}else{
			return this.taxonStatusCacheMap.get(taxonStatusId); 
		}
	}

	public static String getOriginalDbBySources(BitSet sources) {
		String result = "";
		if (sources.get(SOURCE_EM)){
			result = CdmUtils.concat(",", result,  SOURCE_STR_EM);
		}
		if (sources.get(SOURCE_FE)){
			result = CdmUtils.concat(",", result,  SOURCE_STR_FE);
		}
		if (sources.get(SOURCE_IF)){
			result = CdmUtils.concat(",", result,  SOURCE_STR_IF);
		}
		if (sources.get(SOURCE_ERMS)){
			result = CdmUtils.concat(",", result,  SOURCE_STR_ERMS);
		}
		
		return result;
	}


	
}
