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
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
import eu.etaxonomy.cdm.model.reference.ISectionBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

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

	public static String NAME_ST_STR_NOM_INVAL = "Nom. Inval.";
	public static String NAME_ST_STR_NOM_ILLEG = "Nom. Illeg.";
	public static String NAME_ST_STR_NOM_NUD = "Nom. Nud.";
	public static String NAME_ST_STR_NOM_REJ = "Nom. Rej.";
	public static String NAME_ST_STR_NOM_REJ_PROP = "Nom. Rej. Prop.";
	public static String NAME_ST_STR_NOM_UTIQUE_REJ = "Nom. Utique Rej.";
	public static String NAME_ST_STR_NOM_UTIQUE_REJ_PROP = "Nom. Utique Rej. Prop.";
	public static String NAME_ST_STR_NOM_CONS = "Nom. Cons.";
	public static String NAME_ST_STR_NOM_CONS_PROP = "Nom. Cons. Prop.";
	public static String NAME_ST_STR_ORTH_CONS = "Orth. Cons.";
	public static String NAME_ST_STR_ORTH_CONS_PROP = "Orth. Cons. Prop.";
	public static String NAME_ST_STR_NOM_SUPERFL = "Nom. Superfl.";
	public static String NAME_ST_STR_NOM_AMBIG = "Nom. Ambig.";
	public static String NAME_ST_STR_NOM_PROVIS = "Nom. Provis.";
	public static String NAME_ST_STR_NOM_DUB = "Nom. Dub.";
	public static String NAME_ST_STR_NOM_NOV = "Nom. Nov.";
	public static String NAME_ST_STR_NOM_CONFUS = "Nom. Confus.";
	public static String NAME_ST_STR_NOM_ALTERN = "Nom. Altern.";
	public static String NAME_ST_STR_COMB_INVAL = "Comb. Inval.";
	public static String NAME_ST_STR_LEGITIMATE = "Legitim"; 
	public static String NAME_ST_STR_COMB_INED = "Comb. Ined."; // PESI specific from here
	public static String NAME_ST_STR_COMB_AND_STAT_INED = "Comb. & Stat. Ined.";
	public static String NAME_ST_STR_NOM_AND_ORTH_CONS = "Nom. & Orth. Cons.";
	public static String NAME_ST_STR_NOM_NOV_INED = "Nom. Nov. Ined.";
	public static String NAME_ST_STR_SP_NOV_INED = "Sp. Nov. Ined.";
	public static String NAME_ST_STR_ALTERNATE_REPRESENTATION = "Alternate Representation";
	public static String NAME_ST_STR_TEMPORARY_NAME = "Temporary Name";
	public static String NAME_ST_STR_SPECIES_INQUIRENDA = "Species Inquirenda";

	// TaxonStatus
	public static int T_STATUS_ACCEPTED = 1;
	public static int T_STATUS_SYNONYM = 2;
	public static int T_STATUS_PARTIAL_SYN = 3;
	public static int T_STATUS_PRO_PARTE_SYN = 4;
	public static int T_STATUS_UNRESOLVED = 5;
	public static int T_STATUS_ORPHANED = 6;
	
	public static String T_STATUS_STR_ACCEPTED = "Accepted";
	public static String T_STATUS_STR_SYNONYM = "Synonym";
	public static String T_STATUS_STR_PARTIAL_SYN = "Partial Synonym";
	public static String T_STATUS_STR_PRO_PARTE_SYN = "Pro Parte Synonym";
	public static String T_STATUS_STR_UNRESOLVED = "Unresolved";
	public static String T_STATUS_STR_ORPHANED = "Orphaned";
	
	// TypeDesginationStatus
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

	public static String STR_IS_BASIONYM_FOR = "is basionym for";
	public static String STR_IS_LATER_HOMONYM_OF = "is later homonym of";
	public static String STR_IS_REPLACED_SYNONYM_FOR = "is replaced synonym for";
	public static String STR_IS_VALIDATION_OF = "is validation of";
	public static String STR_IS_LATER_VALIDATION_OF = "is later validation of";
	public static String STR_IS_TYPE_OF = "is type of";
	public static String STR_IS_CONSERVED_TYPE_OF = "is conserved type of";
	public static String STR_IS_REJECTED_TYPE_OF = "is rejected type of";
	public static String STR_IS_FIRST_PARENT_OF = "is first parent of";
	public static String STR_IS_SECOND_PARENT_OF = "is second parent of";
	public static String STR_IS_FEMALE_PARENT_OF = "is female parent of";
	public static String STR_IS_MALE_PARENT_OF = "is male parent of";
	public static String STR_IS_CONSERVED_AGAINST = "is conserved against";
	public static String STR_IS_REJECTED_IN_FAVOUR_OF = "is rejected in favour of";
	public static String STR_IS_TREATED_AS_LATER_HOMONYM_OF = "is treated as later homonym of";
	public static String STR_IS_ORTHOGRAPHIC_VARIANT_OF = "is orthographic variant of";
	public static String STR_IS_ALTERNATIVE_NAME_FOR = "is alternative name for";
	public static String STR_HAS_SAME_TYPE_AS = "has same type as";
	public static String STR_IS_LECTOTYPE_OF = "is lectotype of";
	public static String STR_TYPE_NOT_DESIGNATED = "type not designated";
	public static String STR_IS_TAXONOMICALLY_INCLUDED_IN  = "is taxonomically included in";
	public static String STR_IS_SYNONYM_OF = "is synonym of";
	public static String STR_IS_MISAPPLIED_NAME_FOR = "is misapplied name for";
	public static String STR_IS_PRO_PARTE_SYNONYM_OF = "is pro parte synonym of";
	public static String STR_IS_PARTIAL_SYNONYM_OF = "is partial synonym of";
	public static String STR_IS_HETEROTYPIC_SYNONYM_OF = "is heterotypic synonym of";
	public static String STR_IS_HOMOTYPIC_SYNONYM_OF = "is homotypic synonym of";
	public static String STR_IS_PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF = "is pro parte and homotypic synonym of";
	public static String STR_IS_PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF = "is pro parte and heterotypic synonym of";
	public static String STR_IS_PARTIAL_AND_HOMOTYPIC_SYNONYM_OF = "is partial and homotypic synonym of";
	public static String STR_IS_PARTIAL_AND_HETEROTYPIC_SYNONYM_OF = "is partial and heterotypic synonym of";
	public static String STR_IS_INFERRED_EPITHET_FOR = "is inferred epithet for";
	public static String STR_IS_INFERRED_GENUS_FOR = "is inferred genus for";
	public static String STR_IS_POTENTIAL_COMBINATION_FOR = "is potential combination for";

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

	public static String Animalia_STR_Kingdom = "Kingdom";
	public static String Animalia_STR_Subkingdom = "Subkingdom";
	public static String Animalia_STR_Superphylum = "Superphylum";
	public static String Animalia_STR_Phylum = "Phylum";
	public static String Animalia_STR_Subphylum = "Subphylum";
	public static String Animalia_STR_Infraphylum = "Infraphylum";
	public static String Animalia_STR_Superclass = "Superclass";
	public static String Animalia_STR_Class = "Class";
	public static String Animalia_STR_Subclass = "Subclass";
	public static String Animalia_STR_Infraclass = "Infraclass";
	public static String Animalia_STR_Superorder = "Superorder";
	public static String Animalia_STR_Order = "Order";
	public static String Animalia_STR_Suborder = "Suborder";
	public static String Animalia_STR_Infraorder = "Infraorder";
	public static String Animalia_STR_Section = "Section";
	public static String Animalia_STR_Subsection = "Subsection";
	public static String Animalia_STR_Superfamily = "Superfamily";
	public static String Animalia_STR_Family = "Family";
	public static String Animalia_STR_Subfamily = "Subfamily";
	public static String Animalia_STR_Tribe = "Tribe";
	public static String Animalia_STR_Subtribe = "Subtribe";
	public static String Animalia_STR_Genus = "Genus";
	public static String Animalia_STR_Subgenus = "Subgenus";
	public static String Animalia_STR_Species = "Species";
	public static String Animalia_STR_Subspecies = "Subspecies";
	public static String Animalia_STR_Natio = "Natio";
	public static String Animalia_STR_Variety = "Variety";
	public static String Animalia_STR_Subvariety = "Subvariety";
	public static String Animalia_STR_Forma = "Forma";

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
	
	public static String Plantae_STR_Kingdom = "Kingdom";
	public static String Plantae_STR_Subkingdom = "Subkingdom";
	public static String Plantae_STR_Division = "Division";
	public static String Plantae_STR_Subdivision = "Subdivision";
	public static String Plantae_STR_Class = "Class";
	public static String Plantae_STR_Subclass = "Subclass";
	public static String Plantae_STR_Order = "Order";
	public static String Plantae_STR_Suborder = "Suborder";
	public static String Plantae_STR_Family = "Family";
	public static String Plantae_STR_Subfamily = "Subfamily";
	public static String Plantae_STR_Tribe	= "Tribe";
	public static String Plantae_STR_Subtribe = "Subtribe";
	public static String Plantae_STR_Genus = "Genus";
	public static String Plantae_STR_Subgenus = "Subgenus";
	public static String Plantae_STR_Section = "Section";
	public static String Plantae_STR_Subsection = "Subsection";
	public static String Plantae_STR_Series = "Series";
	public static String Plantae_STR_Subseries	= "Subseries";
	public static String Plantae_STR_Aggregate	= "Aggregate";
	public static String Plantae_STR_Coll_Species = "Coll. Species";
	public static String Plantae_STR_Species = "Species";
	public static String Plantae_STR_Subspecies = "Subspecies";
	public static String Plantae_STR_Proles = "Proles";
	public static String Plantae_STR_Race = "Race";
	public static String Plantae_STR_Convarietas = "Convarietas";
	public static String Plantae_STR_Variety = "Variety";
	public static String Plantae_STR_Subvariety = "Subvariety";
	public static String Plantae_STR_Forma	= "Forma";
	public static String Plantae_STR_Subforma = "Subforma";
	public static String Plantae_STR_Forma_spec = "Forma spec.";
	public static String Plantae_STR_Taxa_infragen = "Taxa infragen.";
	public static String Plantae_STR_Taxa_infraspec = "Taxa infraspec.";
	
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

	
	public static String NoteCategory_STR_description = "description";
	public static String NoteCategory_STR_ecology = "ecology";
	public static String NoteCategory_STR_phenology	= "phenology";
	public static String NoteCategory_STR_general_distribution_euromed = "general distribution (Euro+Med)";
	public static String NoteCategory_STR_general_distribution_world = "general distribution (world)";
	public static String NoteCategory_STR_Common_names = "Common names";
	public static String NoteCategory_STR_Occurrence = "Occurrence";
	public static String NoteCategory_STR_Maps = "Maps";
	public static String NoteCategory_STR_Link_to_maps = "Link to maps";
	public static String NoteCategory_STR_Link_to_images = "Link to images";
	public static String NoteCategory_STR_Link_to_taxonomy = "Link to taxonomy";
	public static String NoteCategory_STR_Link_to_general_information = "Link to general information";
	public static String NoteCategory_STR_undefined_link = "undefined link";
	public static String NoteCategory_STR_Editor_Braces = "Editor_Braces";
	public static String NoteCategory_STR_Editor_Brackets = "Editor_Brackets";
	public static String NoteCategory_STR_Editor_Parenthesis = "Editor_Parenthesis";
	public static String NoteCategory_STR_Inedited = "Inedited";
	public static String NoteCategory_STR_Comments_on_editing_process = "Comments on editing process";
	public static String NoteCategory_STR_Publication_date = "Publication date";
	public static String NoteCategory_STR_Morphology = "Morphology";
	public static String NoteCategory_STR_Acknowledgments = "Acknowledgments";
	public static String NoteCategory_STR_Original_publication = "Original publication";
	public static String NoteCategory_STR_Type_locality	= "Type locality";
	public static String NoteCategory_STR_Environment = "Environment";
	public static String NoteCategory_STR_Spelling = "Spelling";
	public static String NoteCategory_STR_Systematics = "Systematics";
	public static String NoteCategory_STR_Remark = "Remark";
	public static String NoteCategory_STR_Date_of_publication = "Date of publication";
	public static String NoteCategory_STR_Additional_information = "Additional information";
	public static String NoteCategory_STR_Status = "Status";
	public static String NoteCategory_STR_Nomenclature = "Nomenclature";
	public static String NoteCategory_STR_Homonymy = "Homonymy";
	public static String NoteCategory_STR_Taxonomy = "Taxonomy";
	public static String NoteCategory_STR_Taxonomic_status = "Taxonomic status";
	public static String NoteCategory_STR_Authority	= "Authority";
	public static String NoteCategory_STR_Identification = "Identification";
	public static String NoteCategory_STR_Validity = "Validity";
	public static String NoteCategory_STR_Classification = "Classification";
	public static String NoteCategory_STR_Distribution = "Distribution";
	public static String NoteCategory_STR_Synonymy = "Synonymy";
	public static String NoteCategory_STR_Habitat = "Habitat";
	public static String NoteCategory_STR_Biology = "Biology";
	public static String NoteCategory_STR_Diagnosis	= "Diagnosis";
	public static String NoteCategory_STR_Host = "Host";
	public static String NoteCategory_STR_Note = "Note";
	public static String NoteCategory_STR_Rank = "Rank";
	public static String NoteCategory_STR_Taxonomic_Remark = "Taxonomic Remark";
	public static String NoteCategory_STR_Taxonomic_Remarks = "Taxonomic Remarks";
	
	
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

	public static String STR_LANGUAGE_ALBANIAN = "Albanian";
	public static String STR_LANGUAGE_ARABIC = "Arabic";
	public static String STR_LANGUAGE_ARMENIAN = "Armenian";
	public static String STR_LANGUAGE_AZERBAIJAN = "Azerbaijan";
	public static String STR_LANGUAGE_BELARUSIAN = "Belarusian";
	public static String STR_LANGUAGE_BULGARIAN = "Bulgarian";
	public static String STR_LANGUAGE_CATALAN = "Catalan";
	public static String STR_LANGUAGE_CROAT = "Croat";
	public static String STR_LANGUAGE_CZECH = "Czech";
	public static String STR_LANGUAGE_DANISH = "Danish";
	public static String STR_LANGUAGE_DUTCH = "Dutch";
	public static String STR_LANGUAGE_ENGLISH = "English";
	public static String STR_LANGUAGE_EUSKERA = "Euskera";
	public static String STR_LANGUAGE_ESTONIAN = "Estonian";
	public static String STR_LANGUAGE_FINNISH = "Finnish";
	public static String STR_LANGUAGE_FRENCH = "French";
	public static String STR_LANGUAGE_GEORGIAN = "Georgian";
	public static String STR_LANGUAGE_GERMAN = "German";
	public static String STR_LANGUAGE_GREEK = "Greek";
	public static String STR_LANGUAGE_HUNGARIAN = "Hungarian";
	public static String STR_LANGUAGE_ICELANDIC = "Icelandic";
	public static String STR_LANGUAGE_IRISH_GAELIC = "Irish Gaelic";
	public static String STR_LANGUAGE_ISRAEL_HEBREW = "Israel (Hebrew)";
	public static String STR_LANGUAGE_ITALIAN = "Italian";
	public static String STR_LANGUAGE_LATVIAN = "Latvian";
	public static String STR_LANGUAGE_LITHUANIAN = "Lithuanian";
	public static String STR_LANGUAGE_MACEDONIAN = "Macedonian";
	public static String STR_LANGUAGE_MALTESE = "Maltese";
	public static String STR_LANGUAGE_MOLDOVIAN = "Moldovian";
	public static String STR_LANGUAGE_NORWEGIAN = "Norwegian";
	public static String STR_LANGUAGE_POLISH = "Polish";
	public static String STR_LANGUAGE_PORTUGUESE = "Portuguese";
	public static String STR_LANGUAGE_ROUMANIAN = "Roumanian";
	public static String STR_LANGUAGE_RUSSIAN = "Russian";
	public static String STR_LANGUAGE_RUSSIAN_CAUCASIAN = "Russian Caucasian";
	public static String STR_LANGUAGE_RUSSIAN_ALTAIC_KALMYK_OIRAT = "Russian (Altaic, kalmyk-oirat)";
	public static String STR_LANGUAGE_RUSSIAN_ALTAIC_KARACHAY_BALKAR = "Russian (Altaic, karachay-balkar)";
	public static String STR_LANGUAGE_RUSSIAN_ALTAIC_KUMYK = "Russian (Altaic, kumyk)";
	public static String STR_LANGUAGE_RUSSIAN_ALTAIC_NOGAI = "Russian (Altaic, nogai)";
	public static String STR_LANGUAGE_RUSSIAN_ALTAIC_NORTH_AZERBAIJANI = "Russian (Altaic, north azerbaijani)";
	public static String STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_RUSSIAN = "Russian (Indo-european, russian)";
	public static String STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_KALMYK_OIRAT = "Russian (Indo-european, kalmyk-oirat)";
	public static String STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_OSETIN = "Russian (Indo-european, osetin)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_ABAZA = "Russian (North caucasian, abaza)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_ADYGHE = "Russian (North caucasian, adyghe)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_CHECHEN = "Russian (North caucasian, chechen)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_KABARDIAN = "Russian (North caucasian, kabardian)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_LAK = "Russian (North caucasian, lak)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_AVAR = "Russian (North caucasian, avar)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_IN = "Russian (North caucasian, in)";
	public static String STR_LANGUAGE_RUSSIAN_URALIC_CHUVASH = "Russian (Uralic, chuvash)";
	public static String STR_LANGUAGE_RUSSIAN_URALIC_UDMURT = "Russian (Uralic, udmurt)";
	public static String STR_LANGUAGE_SERBIAN = "Serbian";
	public static String STR_LANGUAGE_SLOVAK = "Slovak";
	public static String STR_LANGUAGE_SLOVENE = "Slovene";
	public static String STR_LANGUAGE_SPANISH_CASTILLIAN = "Spanish, Castillian";
	public static String STR_LANGUAGE_SWEDISH = "Swedish";
	public static String STR_LANGUAGE_TURKISH = "Turkish";
	public static String STR_LANGUAGE_UKRAINE = "Ukraine";
	public static String STR_LANGUAGE_WELSH = "Welsh";
	public static String STR_LANGUAGE_CORSICAN = "Corsican";

	
	// FossilStatus
	public static int FOSSILSTATUS_RECENT_ONLY = 1;
	public static int FOSSILSTATUS_FOSSIL_ONLY = 2;
	public static int FOSSILSTATUS_RECENT_FOSSIL = 3;
	
	public static String STR_RECENT_ONLY = "recent only";
	public static String STR_FOSSIL_ONLY = "fossil only";
	public static String STR_RECENT_FOSSIL = "recent + fossil";

	// SourceUse
	public static int ORIGINAL_DESCRIPTION = 1;
	public static int BASIS_OF_RECORD = 2;
	public static int ADDITIONAL_SOURCE = 3;
	public static int SOURCE_OF_SYNONYMY = 4;
	public static int REDESCRIPTION = 5;
	public static int NEW_COMBINATION_REFERENCE = 6;
	public static int STATUS_SOURCE = 7;
	public static int NOMENCLATURAL_REFERENCE = 8;
	
	public static String STR_ORIGINAL_DESCRIPTION = "original description";
	public static String STR_BASIS_OF_RECORD = "basis of record";
	public static String STR_ADDITIONAL_SOURCE = "additional source";
	public static String STR_SOURCE_OF_SYNONYMY = "source of synonymy";
	public static String STR_REDESCRIPTION = "redescription";
	public static String STR_NEW_COMBINATION_REFERENCE = "new combination reference";
	public static String STR_STATUS_SOURCE = "status source";
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
	public static int AREA_BALTIC_STATES_AND_KALININGRAD_REGION = 27;
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
	public static int AREA_CRETE_WITH_KARPATHOS,_KASOS_AND_GAVDHOS = 40;
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
	public static int AREA_NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND = 82; // LOL!!
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

	public static String STR_AREA_AEGEAN_SEA = "Aegean_Sea";
	public static String STR_AREA_AFRO_TROPICAL_REGION = "Afro-tropical_region";
	public static String STR_AREA_ALBANIA = "Albania";
	public static String STR_AREA_ALGERIA = "Algeria";
	public static String STR_AREA_ANDORRA = "Andorra";
	public static String STR_AREA_ARCHIPELAGO_SEA = "Archipelago_Sea";
	public static String STR_AREA_AREANAME = "AreaName";
	public static String STR_AREA_ARMENIA = "Armenia";
	public static String STR_AREA_ASIATIC_TURKEY = "Asiatic_Turkey";
	public static String STR_AREA_AUSTRALIAN_REGION = "Australian_region";
	public static String STR_AREA_AUSTRIA = "Austria";
	public static String STR_AREA_AUSTRIA_WITH_LIECHTENSTEIN = "Austria_with_Liechtenstein";
	public static String STR_AREA_AZERBAIJAN = "Azerbaijan";
	public static String STR_AREA_AZERBAIJAN_INCLUDING_NAKHICHEVAN = "Azerbaijan_including_Nakhichevan";
	public static String STR_AREA_AZORES = "Azores";
	public static String STR_AREA_BALEAR_SEA = "Balear_Sea";
	public static String STR_AREA_BALEARES = "Baleares";
	public static String STR_AREA_BALTIC_PROPER = "Baltic_Proper";
	public static String STR_AREA_BALTIC_SEA = "Baltic_Sea";
	public static String STR_AREA_BALTIC_STATES_AND_KALININGRAD_REGION = "Baltic_states_(Estonia,_Latvia,_Lithuania)_and_Kaliningrad_region";
	public static String STR_AREA_BARENTS_SEA = "Barents_Sea";
	public static String STR_AREA_BELARUS = "Belarus";
	public static String STR_AREA_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE = "Belgian_Exclusive_Economic_Zone";
	public static String STR_AREA_BELGIUM = "Belgium";
	public static String STR_AREA_BELGIUM_WITH_LUXEMBOURG = "Belgium_with_Luxembourg";
	public static String STR_AREA_BELT_SEA = "Belt_Sea";
	public static String STR_AREA_BISCAY_BAY = "Biscay_Bay";
	public static String STR_AREA_BLACK_SEA = "Black_Sea";
	public static String STR_AREA_BOSNIA_HERZEGOVINA = "Bosnia-Herzegovina";
	public static String STR_AREA_BOTHNIAN_SEA = "Bothnian_Sea";
	public static String STR_AREA_BULGARIA = "Bulgaria";
	public static String STR_AREA_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE = "Bulgarian_Exclusive_Economic_Zone";
	public static String STR_AREA_CANARY_ISLANDS = "Canary_Islands";
	public static String STR_AREA_CASPIAN_SEA = "Caspian_Sea";
	public static String STR_AREA_CAUCASUS_REGION = "Caucasus_region";
	public static String STR_AREA_CENTRAL_EUROPEAN_RUSSIA = "Central_European_Russia";
	public static String STR_AREA_CHANNEL_ISLANDS = "Channel_Islands";
	public static String STR_AREA_CORSE = "Corse";
	public static String STR_AREA_CORVO = "Corvo";
	public static String STR_AREA_CRETE = "Crete";
	public static String STR_AREA_CRETE_WITH_KARPATHOS_KASOS_AND_GAVDHOS = "Crete_with_Karpathos,_Kasos_&_Gavdhos";
	public static String STR_AREA_CRIMEA = "Crimea";
	public static String STR_AREA_CROATIA = "Croatia";
	public static String STR_AREA_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE = "Croatian_Exclusive_Economic_Zone";
	public static String STR_AREA_CYCLADES_ISLANDS = "Cyclades_Islands";
	public static String STR_AREA_CYPRUS = "Cyprus";
	public static String STR_AREA_CZECH_REPUBLIC = "Czech_Republic";
	public static String STR_AREA_DANISH_EXCLUSIVE_ECONOMIC_ZONE = "Danish_Exclusive_Economic_Zone";
	public static String STR_AREA_DENMARK_WITH_BORNHOLM = "Denmark_with_Bornholm";
	public static String STR_AREA_DESERTAS = "Desertas";
	public static String STR_AREA_DODECANESE_ISLANDS = "Dodecanese_Islands";
	public static String STR_AREA_DUTCH_EXCLUSIVE_ECONOMIC_ZONE = "Dutch_Exclusive_Economic_Zone";
	public static String STR_AREA_EAST_AEGEAN_ISLANDS = "East_Aegean_Islands";
	public static String STR_AREA_EAST_PALAEARCTIC = "East_Palaearctic";
	public static String STR_AREA_EASTERN_EUROPEAN_RUSSIA = "Eastern_European_Russia";
	public static String STR_AREA_EGYPT = "Egypt";
	public static String STR_AREA_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE = "Egyptian_Exclusive_Economic_Zone";
	public static String STR_AREA_ENGLISH_CHANNEL = "English_Channel";
	public static String STR_AREA_ESTONIA = "Estonia";
	public static String STR_AREA_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE = "Estonian_Exclusive_Economic_Zone";
	public static String STR_AREA_EUROPEAN_MARINE_WATERS = "European_Marine_Waters";
	public static String STR_AREA_EUROPEAN_TURKEY = "European_Turkey";
	public static String STR_AREA_FAIAL = "Faial";
	public static String STR_AREA_FAROE_ISLANDS = "Faroe_Islands";
	public static String STR_AREA_FINLAND_WITH_AHVENANMAA = "Finland_with_Ahvenanmaa";
	public static String STR_AREA_FLORES = "Flores";
	public static String STR_AREA_FORMER_CZECHOSLOVAKIA = "Former_Czechoslovakia";
	public static String STR_AREA_FORMER_JUGOSLAVIA = "Former_Jugoslavia";
	public static String STR_AREA_FORMER_USSR = "Former_USSR";
	public static String STR_AREA_FRANCE = "France";
	public static String STR_AREA_FRANZ_JOSEF_LAND = "Franz_Josef_Land";
	public static String STR_AREA_FRENCH_EXCLUSIVE_ECONOMIC_ZONE = "French_Exclusive_Economic_Zone";
	public static String STR_AREA_FRENCH_MAINLAND = "French_mainland";
	public static String STR_AREA_FUERTEVENTURA_WITH_LOBOS = "Fuerteventura_with_Lobos";
	public static String STR_AREA_GEORGIA = "Georgia";
	public static String STR_AREA_GERMAN_EXCLUSIVE_ECONOMIC_ZONE = "German_Exclusive_Economic_Zone";
	public static String STR_AREA_GERMANY = "Germany";
	public static String STR_AREA_GIBRALTAR = "Gibraltar";
	public static String STR_AREA_GOMERA = "Gomera";
	public static String STR_AREA_GRACIOSA = "Graciosa";
	public static String STR_AREA_GRAN_CANARIA = "Gran_Canaria";
	public static String STR_AREA_GREAT_BRITAIN = "Great_Britain";
	public static String STR_AREA_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE = "Grecian_Exclusive_Economic_Zone";
	public static String STR_AREA_GREECE_WITH_CYCLADES_AND_MORE_ISLANDS = "Greece_with_Cyclades_and_more_islands";
	public static String STR_AREA_GREEK_EAST_AEGEAN_ISLANDS = "Greek_East_Aegean_Islands";
	public static String STR_AREA_GREEK_MAINLAND = "Greek_mainland";
	public static String STR_AREA_HIERRO = "Hierro";
	public static String STR_AREA_HUNGARY = "Hungary";
	public static String STR_AREA_IBIZA_WITH_FORMENTERA = "Ibiza_with_Formentera";
	public static String STR_AREA_ICELAND = "Iceland";
	public static String STR_AREA_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE = "Icelandic_Exclusive_Economic_Zone";
	public static String STR_AREA_IRELAND = "Ireland";
	public static String STR_AREA_IRISH_EXCLUSIVE_ECONOMIC_ZONE = "Irish_Exclusive_economic_Zone";
	public static String STR_AREA_IRISH_SEA = "Irish_Sea";
	public static String STR_AREA_ISRAEL = "Israel";
	public static String STR_AREA_ISRAEL_JORDAN = "Israel-Jordan";
	public static String STR_AREA_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE = "Italian_Exclusive_Economic_Zone";
	public static String STR_AREA_ITALIAN_MAINLAND = "Italian_mainland";
	public static String STR_AREA_ITALY = "Italy";
	public static String STR_AREA_JORDAN = "Jordan";
	public static String STR_AREA_KALININGRAD = "Kaliningrad";
	public static String STR_AREA_KINGDOM_OF_SPAIN = "Kingdom_of_Spain";
	public static String STR_AREA_LA_PALMA = "La_Palma";
	public static String STR_AREA_LANZAROTE_WITH_GRACIOSA = "Lanzarote_with_Graciosa";
	public static String STR_AREA_LATVIA = "Latvia";
	public static String STR_AREA_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE = "Lebanese_Exclusive_Economic_Zone";
	public static String STR_AREA_LEBANON = "Lebanon";
	public static String STR_AREA_LEBANON_SYRIA = "Lebanon-Syria";
	public static String STR_AREA_LIBYA = "Libya";
	public static String STR_AREA_LIECHTENSTEIN = "Liechtenstein";
	public static String STR_AREA_LITHUANIA = "Lithuania";
	public static String STR_AREA_LUXEMBOURG = "Luxembourg";
	public static String STR_AREA_MADEIRA_ARCHIPELAGO = "Madeira Archipelago";
	public static String STR_AREA_MADEIRA = "Madeira";
	public static String STR_AREA_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE = "Madeiran_Exclusive_Economic_Zone";
	public static String STR_AREA_MALLORCA = "Mallorca";
	public static String STR_AREA_MALTA = "Malta";
	public static String STR_AREA_MARMARA_SEA = "Marmara_Sea";
	public static String STR_AREA_MEDITERRANEAN_SEA = "Mediterranean_Sea";
	public static String STR_AREA_MENORCA = "Menorca";
	public static String STR_AREA_MOLDOVA = "Moldova";
	public static String STR_AREA_MONACO = "Monaco";
	public static String STR_AREA_MONTENEGRO = "Montenegro";
	public static String STR_AREA_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE = "Moroccan_Exclusive_Economic_Zone";
	public static String STR_AREA_MOROCCO = "Morocco";
	public static String STR_AREA_NAKHICHEVAN = "Nakhichevan";
	public static String STR_AREA_NEAR_EAST = "Near_East";
	public static String STR_AREA_NEARCTIC_REGION = "Nearctic_region";
	public static String STR_AREA_NEOTROPICAL_REGION = "Neotropical_region";
	public static String STR_AREA_NETHERLANDS = "Netherlands";
	public static String STR_AREA_NORTH_AEGEAN_ISLANDS = "North_Aegean_Islands";
	public static String STR_AREA_NORTH_BALTIC_PROPER = "North_Baltic_proper";
	public static String STR_AREA_NORTH_CAUCASUS = "North_Caucasus";
	public static String STR_AREA_NORTH_SEA = "North_Sea";
	public static String STR_AREA_NORTHERN_AFRICA = "Northern_Africa";
	public static String STR_AREA_NORTHERN_EUROPEAN_RUSSIA = "Northern_European_Russia";
	public static String STR_AREA_NORTHERN_IRELAND = "Northern_Ireland";
	public static String STR_AREA_NORTHWEST_EUROPEAN_RUSSIA = "Northwest_European_Russia";
	public static String STR_AREA_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE = "Norwegian_Exclusive_Economic_Zone";
	public static String STR_AREA_NORWEGIAN_MAINLAND = "Norwegian_mainland";
	public static String STR_AREA_NORWEGIAN_SEA = "Norwegian_Sea";
	public static String STR_AREA_NOVAYA_ZEMLYA = "Novaya_Zemlya";
	public static String STR_AREA_NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND = "Novaya_Zemlya_&_Franz-Joseph_Land";
	public static String STR_AREA_ORIENTAL_REGION = "Oriental_region";
	public static String STR_AREA_PICO = "Pico";
	public static String STR_AREA_POLAND = "Poland";
	public static String STR_AREA_PORTO_SANTO = "Porto_Santo";
	public static String STR_AREA_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE = "Portuguese_Exclusive_Economic_Zone";
	public static String STR_AREA_PORTUGUESE_MAINLAND = "Portuguese_mainland";
	public static String STR_AREA_REPUBLIC_OF_IRELAND = "Republic_of_Ireland";
	public static String STR_AREA_ROMANIA = "Romania";
	public static String STR_AREA_RUSSIA_BALTIC = "Russia_Baltic";
	public static String STR_AREA_RUSSIA_CENTRAL = "Russia_Central";
	public static String STR_AREA_RUSSIA_NORTHERN = "Russia_Northern";
	public static String STR_AREA_RUSSIA_SOUTHEAST = "Russia_Southeast";
	public static String STR_AREA_RUSSIA_SOUTHWEST = "Russia_Southwest";
	public static String STR_AREA_SAN_MARINO = "San_Marino";
	public static String STR_AREA_SANTA_MARIA = "Santa_Maria";
	public static String STR_AREA_SAO_JORGE = "São_Jorge";
	public static String STR_AREA_SAO_MIGUEL = "São_Miguel";
	public static String STR_AREA_SARDEGNA = "Sardegna";
	public static String STR_AREA_SEA_OF_AZOV = "Sea_of_Azov";
	public static String STR_AREA_SELVAGENS_ISLANDS = "Selvagens_Islands";
	public static String STR_AREA_SERBIA_INCLUDING_VOJVODINA_AND_WITH_KOSOVO = "Serbia_including_Vojvodina_and_with_Kosovo";
	public static String STR_AREA_SERBIA_WITH_MONTENEGRO = "Serbia_with_Montenegro";
	public static String STR_AREA_SICILY = "Sicily";
	public static String STR_AREA_SICILY_WITH_MALTA = "Sicily_with_Malta";
	public static String STR_AREA_SINAI = "Sinai";
	public static String STR_AREA_SKAGERRAK = "Skagerrak";
	public static String STR_AREA_SLOVAKIA = "Slovakia";
	public static String STR_AREA_SLOVENIA = "Slovenia";
	public static String STR_AREA_SOUTH_BALTIC_PROPER = "South_Baltic_proper";
	public static String STR_AREA_SOUTH_EUROPEAN_RUSSIA = "South_European_Russia";
	public static String STR_AREA_SPAIN = "Spain";
	public static String STR_AREA_SPANISH_EXCLUSIVE_ECONOMIC_ZONE = "Spanish_Exclusive_Economic_Zone";
	public static String STR_AREA_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART = "Spanish_Exclusive_Economic_Zone_[Mediterranean_part]";
	public static String STR_AREA_SVALBARD_WITH_BJORNOYA_AND_JAN_MAYEN = "Svalbard_with_Björnöya_and_Jan_Mayen";
	public static String STR_AREA_SWEDEN = "Sweden";
	public static String STR_AREA_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE = "Swedish_Exclusive_Economic_Zone";
	public static String STR_AREA_SWITZERLAND = "Switzerland";
	public static String STR_AREA_SYRIA = "Syria";
	public static String STR_AREA_TENERIFE = "Tenerife";
	public static String STR_AREA_TERCEIRA = "Terceira";
	public static String STR_AREA_THE_FORMER_JUGOSLAV_REPUBLIC_OF_MAKEDONIJA = "The_former_Jugoslav_Republic_of_Makedonija";
	public static String STR_AREA_THE_RUSSIAN_FEDERATION = "The_Russian_Federation";
	public static String STR_AREA_TIRRENO_SEA = "Tirreno_Sea";
	public static String STR_AREA_TUNISIA = "Tunisia";
	public static String STR_AREA_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE = "Tunisian_Exclusive_Economic_Zone";
	public static String STR_AREA_TURKEY = "Turkey";
	public static String STR_AREA_TURKISH_EAST_AEGEAN_ISLANDS = "Turkish_East_Aegean_Islands";
	public static String STR_AREA_TURKISH_EXCLUSIVE_ECONOMIC_ZONE = "Turkish_Exclusive_Economic_Zone";
	public static String STR_AREA_UKRAINE = "Ukraine";
	public static String STR_AREA_UKRAINE_INCLUDING_CRIMEA = "Ukraine_including_Crimea";
	public static String STR_AREA_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE = "Ukrainian_Exclusive_Economic_Zone";
	public static String STR_AREA_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE = "United_Kingdom_Exclusive_Economic_Zone";
	public static String STR_AREA_VATICAN_CITY = "Vatican_City";
	public static String STR_AREA_WADDEN_SEA = "Wadden_Sea";
	public static String STR_AREA_WHITE_SEA = "White_Sea";

	
	// OccurrenceStatus
	public static int STATUS_PRESENT = 1;
	public static int STATUS_ABSENT = 2;
	public static int STATUS_NATIVE = 3;
	public static int STATUS_INTRODUCED = 4;
	public static int STATUS_NATURALISED = 5;
	public static int STATUS_INVASIVE = 6;
	public static int STATUS_MANAGED = 7;
	public static int STATUS_DOUBTFUL = 8;

	public static String STR_STATUS_PRESENT = "Present";
	public static String STR_STATUS_ABSENT = "Absent";
	public static String STR_STATUS_NATIVE = "Native";
	public static String STR_STATUS_INTRODUCED = "Introduced";
	public static String STR_STATUS_NATURALISED = "Naturalised";
	public static String STR_STATUS_INVASIVE = "Invasive";
	public static String STR_STATUS_MANAGED = "Managed";
	public static String STR_STATUS_DOUBTFUL = "Doubtful";


	/**
	 * Returns the OccurrenceStatusCache for a given PresenceAbsenceTerm.
	 * @param term
	 * @return
	 * @throws UnknownCdmTypeException 
	 */
	public static String presenceAbsenceTerm2OccurrenceStatusCache(PresenceAbsenceTermBase<?> term) {
		String result = STR_STATUS_PRESENT; // TODO: What should be returned if a PresenceTerm/AbsenceTerm could not be translated to a datawarehouse occurrence status id?
		if (term.isInstanceOf(PresenceTerm.class)) {
			PresenceTerm presenceTerm = CdmBase.deproxy(term, PresenceTerm.class);
			if (presenceTerm.equals(PresenceTerm.PRESENT())) {
				result = STR_STATUS_PRESENT;
			} else if (presenceTerm.equals(PresenceTerm.NATIVE())) {
				result = STR_STATUS_NATIVE;
			} else if (presenceTerm.equals(PresenceTerm.INTRODUCED())) {
				result = STR_STATUS_INTRODUCED;
			} else if (presenceTerm.equals(PresenceTerm.NATURALISED())) {
				result = STR_STATUS_NATURALISED;
			} else if (presenceTerm.equals(PresenceTerm.INVASIVE())) {
				result = STR_STATUS_INVASIVE;
//			} else if (presenceTerm.equals(PresenceTerm.)) {
//				result = STR_STATUS_MANAGED;
//			} else if (presenceTerm.equals(PresenceTerm.)) {
//				result = STR_STATUS_DOUBTFUL;
			} else {
				logger.error("PresenceTerm could not be translated to datawarehouse occurrence status id: " + presenceTerm.getLabel());
			}
		} else if (term.isInstanceOf(AbsenceTerm.class)) {
			AbsenceTerm absenceTerm = CdmBase.deproxy(term, AbsenceTerm.class);
			if (absenceTerm.equals(AbsenceTerm.ABSENT())) {
				result = STR_STATUS_ABSENT;
			} else {
				logger.error("AbsenceTerm could not be translated to datawarehouse occurrence status id: " + absenceTerm.getLabel());
			}
//			result = STR_STATUS_ABSENT; // or just like this?
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
		Integer result = STATUS_PRESENT; // TODO: What should be returned if a PresenceTerm/AbsenceTerm could not be translated to a datawarehouse occurrence status id?
		if (term.isInstanceOf(PresenceTerm.class)) {
			PresenceTerm presenceTerm = CdmBase.deproxy(term, PresenceTerm.class);
			if (presenceTerm.equals(PresenceTerm.PRESENT())) {
				result = STATUS_PRESENT;
			} else if (presenceTerm.equals(PresenceTerm.NATIVE())) {
				result = STATUS_NATIVE;
			} else if (presenceTerm.equals(PresenceTerm.INTRODUCED())) {
				result = STATUS_INTRODUCED;
			} else if (presenceTerm.equals(PresenceTerm.NATURALISED())) {
				result = STATUS_NATURALISED;
			} else if (presenceTerm.equals(PresenceTerm.INVASIVE())) {
				result = STATUS_INVASIVE;
//			} else if (presenceTerm.equals(PresenceTerm.)) {
//				result = STATUS_MANAGED;
//			} else if (presenceTerm.equals(PresenceTerm.)) {
//				result = STATUS_DOUBTFUL;
			} else {
				logger.error("PresenceTerm could not be translated to datawarehouse occurrence status id: " + presenceTerm.getLabel());
			}
		} else if (term.isInstanceOf(AbsenceTerm.class)) {
			AbsenceTerm absenceTerm = CdmBase.deproxy(term, AbsenceTerm.class);
			if (absenceTerm.equals(AbsenceTerm.ABSENT())) {
				result = STATUS_ABSENT;
			} else {
				logger.error("AbsenceTerm could not be translated to datawarehouse occurrence status id: " + absenceTerm.getLabel());
			}
//			result = STATUS_ABSENT; // or just like this?
		}
		return result;
	}
	
	/**
	 * Returns the AreaCache for a given Area.
	 * @param area
	 * @return
	 */
	public static String area2AreaCache(NamedArea area) {
		if (area == null) {
			return null;
		} else if (area.isInstanceOf(TdwgArea.class)) {
			// TODO: This will take forever to implement the return of the appropriate datawarehouse area for any TDWG area. The return of STR_AREA_ALBANIA for any TDWG Area found is just a placeholder.
			TdwgArea tdwgArea = CdmBase.deproxy(area, TdwgArea.class);
			if (tdwgArea.isTdwgAreaAbbreviation("")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("1")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("2")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("3")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("4")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("5")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("6")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("7")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("8")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("9")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("10")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("11")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("12")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("13")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("14")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("20")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("21")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("22")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("23")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("24")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("25")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("26")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("27")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("28")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("29")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("30")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("31")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("32")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("33")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("34")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("35")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("36")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("37")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("38")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("40")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("41")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("42")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("43")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("50")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("51")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("60")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("61")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("62")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("63")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("70")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("71")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("72")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("73")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("74")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("75")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("76")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("77")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("78")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("79")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("80")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("81")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("82")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("83")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("84")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("85")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("90")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("91")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ABT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AFG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALD")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AMU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AND")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ANG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ANT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ATP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AUT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AZO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BEN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BER")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BGM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BIS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BKN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLZ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BRC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BRY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BUL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BUR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAF")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CBD")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CGS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHQ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHX")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CKI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CMN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CNT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CNY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CON")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPV")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CRL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CRZ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CTA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CTM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CUB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CVI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CYP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CZE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DEL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DEN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DJI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DOM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DSV")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EAI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EAS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ECU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EGY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EHM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ELS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EQG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ERI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ETH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FAL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FIJ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FIN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FLA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FOR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GEO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GER")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GGI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GHA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GIL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GNB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GNL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GRB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GRC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GST")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAW")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HBI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HMD")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HON")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HUN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ICE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IDA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ILL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("INI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IOW")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRQ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ITA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IVO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAW")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JNF")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAZ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KEG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KEN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KER")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KGZ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KHA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KOR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KTY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KUR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KUW")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KZN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LAB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LAO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LDV")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LES")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LIN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LOU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LSI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAQ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MCI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MCS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDV")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MIC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MIN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLW")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MNT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MON")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOZ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MPE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRQ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MSI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MSO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MTN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MYA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NAM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NAT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NBR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NDA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NET")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEV")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NGA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NGR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NIC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NLA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NNS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NOR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NRU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NSC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NSW")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NTA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NUE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NUN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWJ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NZN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NZS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OFS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OGA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OHI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OKL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OMA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ONT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ORE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PEI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PEN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PER")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PHI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PHX")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PIT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("POL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("POR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PRM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PUE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("QLD")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("QUE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("REU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RHO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ROD")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ROM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUW")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RWA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCZ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SDA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SGE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SPA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SRL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SSA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("STH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUD")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SVA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWZ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TDC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TEN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TEX")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("THA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TKM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TON")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TRT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUV")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TZK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UGA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UKR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("URU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UTA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UZB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VAN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VEN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VER")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VIC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VIE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VNA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VRG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WDC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WHM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WSA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WSB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WVA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WYO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("XMS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YAK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YEM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZAI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZAM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZIM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ABT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AFG-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-BA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-CH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-CN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-CO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-DF")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-ER")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-FO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-LP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-MI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-CB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-NE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-RN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-SC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-SF")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-TF")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-CA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-JU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-LR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-ME")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-SA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-SE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-SJ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-SL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-TU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALD-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALG-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALU-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AMU-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AND-AN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AND-CO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ANG-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ANT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARK-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARU-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASK-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASP-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-AS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-MA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-ME")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-MI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-NA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-TR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ATP-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AUT-AU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AUT-LI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AZO-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAH-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BEN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BER-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BGM-BE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BGM-LU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BIS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BKN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLT-ES")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLT-KA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLT-LA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLT-LI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLZ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOR-BR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOR-KA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOR-SB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOR-SR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOU-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BRC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BRY-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BUL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BUR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZC-DF")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZC-GO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZC-MS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZC-MT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-AL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-BA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-CE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-FN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-MA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-PB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-PE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-PI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-RN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-SE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL-ES")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL-MG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL-RJ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL-SP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL-TR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-AC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-AM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-AP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-PA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-RM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-RO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-TO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZS-PR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZS-RS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZS-SC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAF-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAY-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CBD-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CGS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC-CQ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC-GZ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC-HU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC-SC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC-YN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHH-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHI-NM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHI-NX")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHM-HJ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHM-JL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHM-LN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-BJ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-GS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-HB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-SA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-SD")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-SX")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-TJ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHQ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-AH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-FJ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-GD")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-GX")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-HE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-HK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-HN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-JS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-JX")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-KI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-MA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-MP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-SH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-ZJ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHX-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CKI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-BI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-CO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-LA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-MA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-OH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-SA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-VA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLN-AN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLN-AT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLN-TA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLS-AI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLS-LL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLS-MG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CMN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CNT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CNY-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COM-CO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COM-MA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CON-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COO-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPI-CL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPI-CO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPI-MA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPP-EC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPP-NC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPP-WC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPV-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CRL-MF")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CRL-PA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CRZ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CTA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CTM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CUB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CVI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CYP-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CZE-CZ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CZE-SK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DEL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DEN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DJI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DOM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DSV-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EAI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EAS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ECU-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EGY-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EHM-AP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EHM-BH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EHM-DJ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EHM-SI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ELS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EQG-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ERI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ETH-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FAL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FIJ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FIN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FLA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FOR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRA-CI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRA-FR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRA-MO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRG-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GEO-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GER-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GGI-AN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GGI-BI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GGI-PR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GGI-ST")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GHA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GIL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GNB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GNL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GRB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GRC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GST-BA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GST-QA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GST-UA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUY-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAI-HA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAI-NI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAW-HI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAW-JI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAW-MI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HBI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HMD-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HON-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HUN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ICE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IDA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ILL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-AP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-BI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-CH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-CT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-DD")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-DE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-DI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-DM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-GO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-GU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-HA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-JK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-KE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-KL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-KT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-MH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-MP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-MR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-OR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-PO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-PU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-RA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-TN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-UP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-WB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-YA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("INI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IOW-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRE-IR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRE-NI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRK-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRQ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ITA-IT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ITA-SM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ITA-VC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IVO-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAP-HK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAP-HN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAP-KY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAP-SH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAW-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JNF-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAZ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KEG-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KEN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KER-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KGZ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KHA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KOR-NK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KOR-SK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRY-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KTY-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KUR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KUW-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KZN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LAB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LAO-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBS-LB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBS-SY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBY-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LDV-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-AB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-AG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-AV")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-BV")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-GU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-MO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-NL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-SK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-SM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-VI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LES-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LIN-KI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LIN-US")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LOU-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LSI-BA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LSI-ET")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LSI-LS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAG-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAQ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAU-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MCI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MCS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDG-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDV-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MIC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MIN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLW-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLY-PM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLY-SI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MNT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MON-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOR-MO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOR-SP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOZ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MPE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRN-GU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRN-NM")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRQ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRY-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MSI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MSO-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MTN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC-DF")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC-ME")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC-MO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC-PU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC-TL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-AG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-CO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-CU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-DU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-GU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-HI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-NL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-QU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-SL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-TA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-ZA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXG-VC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXI-GU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXI-RA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXI-RG")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXN-BC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXN-BS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXN-SI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXN-SO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-CL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-GR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-JA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-MI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-NA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-OA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT-CA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT-CI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT-QR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT-TB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT-YU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MYA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NAM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NAT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NBR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-CH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-DA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-IN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-KB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-KC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-KR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-SO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-ST")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NDA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEP-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NET-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEV-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFK-LH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFK-NI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFL-NE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFL-SP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NGA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NGR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NIC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NLA-BO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NLA-CU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NNS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NOR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NRU-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NSC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NSW-CT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NSW-NS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NTA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NUE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NUN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWG-IJ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWG-PN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWH-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWJ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWY-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NZN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NZS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OFS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OGA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OHI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OKL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OMA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ONT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ORE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAK-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAL-IS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAL-JO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PEI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PEN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PER-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PHI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PHX-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PIT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("POL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("POR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PRM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PUE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("QLD-CS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("QLD-QU")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("QUE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("REU-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RHO-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ROD-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ROM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUW-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RWA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAK-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAM-AS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAM-WS")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAU-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCS-PI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCS-SI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCZ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SDA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEY-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SGE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIC-MA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIC-SI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOL-NO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOL-SO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SPA-AN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SPA-GI")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SPA-SP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SRL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SSA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("STH-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUD-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SVA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWC-CC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWC-HC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWC-NC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWZ-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-AB")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-AD")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-AR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-AZ")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-GR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-NA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-NK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TDC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TEN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TEX-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("THA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TKM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOG-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOK-MA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOK-SW")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOK-TO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TON-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TRT-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUR-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUV-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVL-GA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVL-MP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVL-NP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVL-NW")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TZK-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UGA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UKR-MO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UKR-UK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("URU-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UTA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UZB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VAN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VEN-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VER-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VIC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VIE-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VNA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VRG-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAK-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAL-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAU-AC")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAU-WA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WDC-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WHM-HP")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WHM-JK")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WHM-UT")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-BA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-DO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-GR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-MA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-SL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-SV")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WSA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WSB-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WVA-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WYO-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("XMS-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YAK-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YEM-NY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YEM-SY")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-BH")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-CR")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-KO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-MA")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-MN")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-SE")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-SL")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUK-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZAI-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZAM-OO")) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZIM-OO")) { return STR_AREA_ALBANIA; }
			else {
				logger.error("Unknown TdwgArea Area: " + area.getTitleCache());
				return null; // Actually the export has to stop here because AreaFk's are not allowed to be NULL.
			}
		} else if (area.isInstanceOf(WaterbodyOrCountry.class)) {
			if (area.equals(WaterbodyOrCountry.ALBANIAPEOPLESSOCIALISTREPUBLICOF())) {
				return STR_AREA_ALBANIA;
			} else if (area.equals(WaterbodyOrCountry.AUSTRIAREPUBLICOF())) {
				return STR_AREA_AUSTRIA;
			} else if (area.equals(WaterbodyOrCountry.LIECHTENSTEINPRINCIPALITYOF())) {
				return STR_AREA_LIECHTENSTEIN;
			} else if (area.equals(WaterbodyOrCountry.BELGIUMKINGDOMOF())) {
				return STR_AREA_BELGIUM;
			} else if (area.equals(WaterbodyOrCountry.LUXEMBOURGGRANDDUCHYOF())) {
				return STR_AREA_LUXEMBOURG;
			} else if (area.equals(WaterbodyOrCountry.BOSNIAANDHERZEGOVINA())) {
				return STR_AREA_BOSNIA_HERZEGOVINA;
			} else if (area.equals(WaterbodyOrCountry.UNITEDKINGDOMOFGREATBRITAINANDNORTHERNIRELAND())) {
				return STR_AREA_GREAT_BRITAIN;
			} else if (area.equals(WaterbodyOrCountry.BULGARIAPEOPLESREPUBLICOF())) {
				return STR_AREA_BULGARIA;
			} else if (area.equals(WaterbodyOrCountry.BELARUS())) {
				return STR_AREA_BELARUS;
			} else if (area.equals(WaterbodyOrCountry.CZECHREPUBLIC())) {
				return STR_AREA_CZECH_REPUBLIC;
			} else if (area.equals(WaterbodyOrCountry.CYPRUSREPUBLICOF())) {
				return STR_AREA_CYPRUS;
			} else if (area.equals(WaterbodyOrCountry.DENMARKKINGDOMOF())) {
				return STR_AREA_DENMARK_WITH_BORNHOLM; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.ESTONIA())) {
				return STR_AREA_ESTONIA;
			} else if (area.equals(WaterbodyOrCountry.FAEROEISLANDS())) {
				return STR_AREA_FAROE_ISLANDS;
			} else if (area.equals(WaterbodyOrCountry.FINLANDREPUBLICOF())) {
				return STR_AREA_FINLAND_WITH_AHVENANMAA; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.FRANCEFRENCHREPUBLIC())) {
				return STR_AREA_FRANCE;
			} else if (area.equals(WaterbodyOrCountry.MONACOPRINCIPALITYOF())) {
				return STR_AREA_MONACO;
			} else if (area.equals(WaterbodyOrCountry.GERMANY())) {
				return STR_AREA_GERMANY;
			} else if (area.equals(WaterbodyOrCountry.IRELAND())) {
				return STR_AREA_IRELAND;
			} else if (area.equals(WaterbodyOrCountry.SWITZERLANDSWISSCONFEDERATION())) {
				return STR_AREA_SWITZERLAND; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.NETHERLANDSKINGDOMOFTHE())) {
				return STR_AREA_NETHERLANDS;
			} else if (area.equals(WaterbodyOrCountry.SPAINSPANISHSTATE())) {
				return STR_AREA_SPAIN;
			} else if (area.equals(WaterbodyOrCountry.ANDORRAPRINCIPALITYOF())) {
				return STR_AREA_ANDORRA;
			} else if (area.equals(WaterbodyOrCountry.GIBRALTAR())) {
				return STR_AREA_GIBRALTAR;
			} else if (area.equals(WaterbodyOrCountry.HUNGARYHUNGARIANPEOPLESREPUBLIC())) {
				return STR_AREA_HUNGARY;
			} else if (area.equals(WaterbodyOrCountry.ICELANDREPUBLICOF())) {
				return STR_AREA_ICELAND;
			} else if (area.equals(WaterbodyOrCountry.ITALYITALIANREPUBLIC())) {
				return STR_AREA_ITALY;
			} else if (area.equals(WaterbodyOrCountry.SANMARINOREPUBLICOF())) {
				return STR_AREA_SAN_MARINO;
			} else if (area.equals(WaterbodyOrCountry.LATVIA())) {
				return STR_AREA_LATVIA;
			} else if (area.equals(WaterbodyOrCountry.LITHUANIA())) {
				return STR_AREA_LITHUANIA;
			} else if (area.equals(WaterbodyOrCountry.PORTUGALPORTUGUESEREPUBLIC())) {
				return STR_AREA_PORTUGUESE_MAINLAND;
			} else if (area.equals(WaterbodyOrCountry.MACEDONIATHEFORMERYUGOSLAVREPUBLICOF())) {
				return STR_AREA_THE_FORMER_JUGOSLAV_REPUBLIC_OF_MAKEDONIJA;
			} else if (area.equals(WaterbodyOrCountry.MOLDOVAREPUBLICOF())) {
				return STR_AREA_MOLDOVA;
			} else if (area.equals(WaterbodyOrCountry.NORWAYKINGDOMOF())) {
				return STR_AREA_NORWEGIAN_MAINLAND;
			} else if (area.equals(WaterbodyOrCountry.POLANDPOLISHPEOPLESREPUBLIC())) {
				return STR_AREA_POLAND;
			} else if (area.equals(WaterbodyOrCountry.RUSSIANFEDERATION())) {
				return STR_AREA_THE_RUSSIAN_FEDERATION;
			} else if (area.equals(WaterbodyOrCountry.ROMANIASOCIALISTREPUBLICOF())) {
				return STR_AREA_ROMANIA;
			} else if (area.equals(WaterbodyOrCountry.SVALBARDJANMAYENISLANDS())) {
				return STR_AREA_SVALBARD_WITH_BJORNOYA_AND_JAN_MAYEN;
			} else if (area.equals(WaterbodyOrCountry.MALTAREPUBLICOF())) {
				return STR_AREA_MALTA;
			} else if (area.equals(WaterbodyOrCountry.SLOVAKIA())) {
				return STR_AREA_SLOVAKIA;
			} else if (area.equals(WaterbodyOrCountry.SLOVENIA())) {
				return STR_AREA_SLOVENIA;
			} else if (area.equals(WaterbodyOrCountry.SERBIAANDMONTENEGRO())) {
				return STR_AREA_SERBIA_WITH_MONTENEGRO;
			} else if (area.equals(WaterbodyOrCountry.SWEDENKINGDOMOF())) {
				return STR_AREA_SWEDEN;
			} else if (area.equals(WaterbodyOrCountry.UKRAINE())) {
				return STR_AREA_UKRAINE;
			} else if (area.equals(WaterbodyOrCountry.GREECEHELLENICREPUBLIC())) {
				return STR_AREA_GREEK_MAINLAND; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.VATICANCITYSTATE())) {
				return STR_AREA_VATICAN_CITY;
			} else if (area.equals(WaterbodyOrCountry.AZERBAIJANREPUBLICOF())) {
				return STR_AREA_AZERBAIJAN;
			} else if (area.equals(WaterbodyOrCountry.ALGERIAPEOPLESDEMOCRATICREPUBLICOF())) {
				return STR_AREA_ALGERIA;
			} else if (area.equals(WaterbodyOrCountry.ARMENIA())) {
				return STR_AREA_ARMENIA;
			} else if (area.equals(WaterbodyOrCountry.EGYPTARABREPUBLICOF())) {
				return STR_AREA_EGYPT;
			} else if (area.equals(WaterbodyOrCountry.GEORGIA())) {
				return STR_AREA_GEORGIA;
			} else if (area.equals(WaterbodyOrCountry.ISRAELSTATEOF())) {
				return STR_AREA_ISRAEL;
			} else if (area.equals(WaterbodyOrCountry.JORDANHASHEMITEKINGDOMOF())) {
				return STR_AREA_JORDAN; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.LEBANONLEBANESEREPUBLIC())) {
				return STR_AREA_LEBANON;
			} else if (area.equals(WaterbodyOrCountry.LIBYANARABJAMAHIRIYA())) {
				return STR_AREA_LIBYA; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.MOROCCOKINGDOMOF())) {
				return STR_AREA_MOROCCO;
			} else if (area.equals(WaterbodyOrCountry.SYRIANARABREPUBLIC())) {
				return STR_AREA_SYRIA;
			} else if (area.equals(WaterbodyOrCountry.TUNISIAREPUBLICOF())) {
				return STR_AREA_TUNISIA;
			} else if (area.equals(WaterbodyOrCountry.TURKEYREPUBLICOF())) {
				return STR_AREA_TURKEY;
			} else if (area.equals(WaterbodyOrCountry.AUSTRALIACOMMONWEALTHOF())) {
				return STR_AREA_AUSTRALIAN_REGION;
			} else if (area.equals(WaterbodyOrCountry.MEDITERRANEANSEA())) {
				return STR_AREA_MEDITERRANEAN_SEA;
			} else if (area.equals(WaterbodyOrCountry.BLACKSEA())) {
				return STR_AREA_BLACK_SEA;
			} else if (area.equals(WaterbodyOrCountry.CASPIANSEA())) {
				return STR_AREA_CASPIAN_SEA;
			} else {
				logger.error("Unknown WaterbodyOrCountry Area: " + area.getTitleCache());
				return null; // Actually the export has to stop here because AreaFk's are not allowed to be NULL.
			}
		}
		return null; // Actually the export has to stop here because AreaFk's are not allowed to be NULL.
		
		// These areas are unknown:
//		EAST_AEGEAN_ISLANDS
//		GREEK_EAST_AEGEAN_ISLANDS
//		TURKISH_EAST_AEGEAN_ISLANDS
//		AUSTRIA_WITH_LIECHTENSTEIN
//		AZORES
//		CORVO
//		FAIAL
//		GRACIOSA
//		SAO_JORGE
//		FLORES
//		SAO_MIGUEL
//		PICO
//		SANTA_MARIA
//		TERCEIRA
//		BELGIUM_WITH_LUXEMBOURG
//		BALEARES
//		IBIZA_WITH_FORMENTERA
//		MALLORCA
//		MENORCA
//		BALTIC_STATES_AND_KALININGRAD_REGION
//		CANARY_ISLANDS
//		GRAN_CANARIA
//		FUERTEVENTURA_WITH_LOBOS
//		GOMERA
//		HIERRO
//		LANZAROTE_WITH_GRACIOSA
//		LA_PALMA
//		TENERIFE
//		MONTENEGRO
//		CORSE
//		CRETE_WITH_KARPATHOS,_KASOS_AND_GAVDHOS
//		CROATIA
//		FORMER_CZECHOSLOVAKIA
//		CHANNEL_ISLANDS
//		FRENCH_MAINLAND
//		GREECE_WITH_CYCLADES_AND_MORE_ISLANDS
//		REPUBLIC_OF_IRELAND
//		NORTHERN_IRELAND
//		KINGDOM_OF_SPAIN
//		ITALIAN_MAINLAND
//		FORMER_JUGOSLAVIA
//		MADEIRA (This one is: MADEIRA ARCHIPELAGO)
//		DESERTAS
//		MADEIRA
//		PORTO_SANTO
//		NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND
//		CENTRAL_EUROPEAN_RUSSIA
//		EASTERN_EUROPEAN_RUSSIA
//		KALININGRAD
//		NORTHERN_EUROPEAN_RUSSIA
//		NORTHWEST_EUROPEAN_RUSSIA
//		SOUTH_EUROPEAN_RUSSIA
//		FORMER_USSR
//		RUSSIA_BALTIC
//		RUSSIA_CENTRAL
//		RUSSIA_SOUTHEAST
//		RUSSIA_NORTHERN
//		RUSSIA_SOUTHWEST
//		SARDEGNA
//		SELVAGENS_ISLANDS
//		SICILY_WITH_MALTA
//		SICILY
//		SERBIA_INCLUDING_VOJVODINA_AND_WITH_KOSOVO
//		EUROPEAN_TURKEY
//		UKRAINE_INCLUDING_CRIMEA
//		CRIMEA
//		CRETE
//		DODECANESE_ISLANDS
//		CYCLADES_ISLANDS
//		NORTH_AEGEAN_ISLANDS
//		FRANZ_JOSEF_LAND
//		NOVAYA_ZEMLYA
//		AZERBAIJAN_INCLUDING_NAKHICHEVAN
//		NAKHICHEVAN
//		CAUCASUS_REGION
//		ISRAEL-JORDAN
//		LEBANON-SYRIA
//		NORTH_CAUCASUS
//		SINAI
//		ASIATIC_TURKEY
//		NORTHERN_AFRICA
//		AFRO_TROPICAL_REGION
//		EAST_PALAEARCTIC
//		NEARCTIC_REGION
//		NEOTROPICAL_REGION
//		NEAR_EAST
//		ORIENTAL_REGION
//		EUROPEAN_MARINE_WATERS
//		WHITE_SEA
//		NORTH_SEA
//		BALTIC_SEA
//		BARENTS_SEA
//		PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE
//		BELGIAN_EXCLUSIVE_ECONOMIC_ZONE
//		FRENCH_EXCLUSIVE_ECONOMIC_ZONE
//		ENGLISH_CHANNEL
//		ADRIATIC_SEA
//		BISCAY_BAY
//		DUTCH_EXCLUSIVE_ECONOMIC_ZONE
//		UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE
//		SPANISH_EXCLUSIVE_ECONOMIC_ZONE
//		EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE
//		GRECIAN_EXCLUSIVE_ECONOMIC_ZONE
//		TIRRENO_SEA
//		ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE
//		IRISH_EXCLUSIVE_ECONOMIC_ZONE
//		IRISH_SEA
//		ITALIAN_EXCLUSIVE_ECONOMIC_ZONE
//		NORWEGIAN_SEA
//		MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE
//		NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE
//		SKAGERRAK
//		TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE
//		WADDEN_SEA
//		BELT_SEA
//		MARMARA_SEA
//		SEA_OF_AZOV
//		AEGEAN_SEA
//		BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE
//		SOUTH_BALTIC_PROPER
//		BALTIC_PROPER
//		NORTH_BALTIC_PROPER
//		ARCHIPELAGO_SEA
//		BOTHNIAN_SEA
//		GERMAN_EXCLUSIVE_ECONOMIC_ZONE
//		SWEDISH_EXCLUSIVE_ECONOMIC_ZONE
//		UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE
//		MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE
//		LEBANESE_EXCLUSIVE_ECONOMIC_ZONE
//		SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART
//		ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE
//		CROATIAN_EXCLUSIVE_ECONOMIC_ZONE
//		BALEAR_SEA
//		TURKISH_EXCLUSIVE_ECONOMIC_ZONE
//		DANISH_EXCLUSIVE_ECONOMIC_ZONE

	}
	
	/**
	 * Returns the AreaId for a given Area.
	 * @param area
	 * @return
	 */
	public static Integer area2AreaId(NamedArea area) {
		if (area == null) {
			return null;
		} else if (area.isInstanceOf(TdwgArea.class)) {
			// TODO: This will take forever to implement the return of the appropriate datawarehouse area for any TDWG area. The return of AREA_ALBANIA for any TDWG Area found is just a placeholder.
			TdwgArea tdwgArea = CdmBase.deproxy(area, TdwgArea.class);
			if (tdwgArea.isTdwgAreaAbbreviation("")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("1")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("2")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("3")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("4")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("5")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("6")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("7")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("8")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("9")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("10")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("11")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("12")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("13")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("14")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("20")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("21")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("22")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("23")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("24")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("25")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("26")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("27")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("28")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("29")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("30")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("31")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("32")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("33")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("34")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("35")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("36")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("37")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("38")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("40")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("41")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("42")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("43")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("50")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("51")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("60")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("61")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("62")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("63")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("70")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("71")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("72")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("73")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("74")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("75")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("76")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("77")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("78")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("79")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("80")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("81")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("82")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("83")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("84")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("85")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("90")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("91")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ABT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AFG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALD")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AMU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AND")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ANG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ANT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ATP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AUT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AZO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BEN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BER")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BGM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BIS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BKN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLZ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BRC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BRY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BUL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BUR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAF")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CBD")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CGS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHQ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHX")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CKI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CMN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CNT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CNY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CON")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPV")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CRL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CRZ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CTA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CTM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CUB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CVI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CYP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CZE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DEL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DEN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DJI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DOM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DSV")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EAI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EAS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ECU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EGY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EHM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ELS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EQG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ERI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ETH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FAL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FIJ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FIN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FLA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FOR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GEO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GER")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GGI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GHA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GIL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GNB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GNL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GRB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GRC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GST")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAW")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HBI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HMD")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HON")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HUN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ICE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IDA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ILL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("INI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IOW")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRQ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ITA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IVO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAW")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JNF")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAZ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KEG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KEN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KER")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KGZ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KHA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KOR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KTY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KUR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KUW")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KZN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LAB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LAO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LDV")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LES")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LIN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LOU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LSI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAQ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MCI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MCS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDV")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MIC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MIN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLW")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MNT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MON")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOZ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MPE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRQ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MSI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MSO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MTN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MYA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NAM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NAT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NBR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NDA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NET")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEV")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NGA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NGR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NIC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NLA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NNS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NOR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NRU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NSC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NSW")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NTA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NUE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NUN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWJ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NZN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NZS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OFS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OGA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OHI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OKL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OMA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ONT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ORE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PEI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PEN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PER")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PHI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PHX")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PIT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("POL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("POR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PRM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PUE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("QLD")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("QUE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("REU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RHO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ROD")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ROM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUW")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RWA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCZ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SDA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SGE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SPA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SRL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SSA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("STH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUD")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SVA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWZ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TDC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TEN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TEX")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("THA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TKM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TON")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TRT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUV")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TZK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UGA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UKR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("URU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UTA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UZB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VAN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VEN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VER")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VIC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VIE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VNA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VRG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WDC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WHM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WSA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WSB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WVA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WYO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("XMS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YAK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YEM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZAI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZAM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZIM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ABT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AFG-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-BA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-CH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-CN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-CO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-DF")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-ER")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-FO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-LP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGE-MI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-CB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-NE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-RN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-SC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-SF")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGS-TF")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-CA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-JU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-LR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-ME")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-SA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-SE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-SJ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-SL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AGW-TU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALD-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALG-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ALU-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AMU-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AND-AN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AND-CO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ANG-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ANT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARK-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ARU-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASK-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASP-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-AS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-MA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-ME")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-MI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-NA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ASS-TR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ATP-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AUT-AU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AUT-LI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("AZO-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAH-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BAN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BEN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BER-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BGM-BE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BGM-LU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BIS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BKN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLT-ES")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLT-KA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLT-LA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLT-LI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BLZ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOR-BR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOR-KA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOR-SB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOR-SR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BOU-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BRC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BRY-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BUL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BUR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZC-DF")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZC-GO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZC-MS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZC-MT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-AL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-BA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-CE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-FN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-MA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-PB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-PE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-PI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-RN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZE-SE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL-ES")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL-MG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL-RJ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL-SP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZL-TR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-AC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-AM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-AP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-PA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-RM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-RO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZN-TO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZS-PR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZS-RS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("BZS-SC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAF-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CAY-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CBD-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CGS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC-CQ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC-GZ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC-HU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC-SC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHC-YN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHH-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHI-NM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHI-NX")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHM-HJ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHM-JL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHM-LN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-BJ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-GS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-HB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-SA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-SD")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-SX")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHN-TJ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHQ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-AH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-FJ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-GD")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-GX")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-HE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-HK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-HN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-JS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-JX")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-KI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-MA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-MP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-SH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHS-ZJ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CHX-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CKI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-BI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-CO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-LA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-MA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-OH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-SA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLC-VA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLN-AN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLN-AT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLN-TA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLS-AI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLS-LL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CLS-MG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CMN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CNT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CNY-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COM-CO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COM-MA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CON-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COO-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("COS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPI-CL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPI-CO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPI-MA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPP-EC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPP-NC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPP-WC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CPV-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CRL-MF")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CRL-PA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CRZ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CTA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CTM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CUB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CVI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CYP-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CZE-CZ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("CZE-SK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DEL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DEN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DJI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DOM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("DSV-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EAI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EAS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ECU-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EGY-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EHM-AP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EHM-BH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EHM-DJ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EHM-SI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ELS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("EQG-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ERI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ETH-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FAL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FIJ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FIN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FLA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FOR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRA-CI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRA-FR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRA-MO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("FRG-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GAM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GEO-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GER-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GGI-AN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GGI-BI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GGI-PR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GGI-ST")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GHA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GIL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GNB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GNL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GRB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GRC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GST-BA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GST-QA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GST-UA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("GUY-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAI-HA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAI-NI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAW-HI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAW-JI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HAW-MI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HBI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HMD-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HON-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("HUN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ICE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IDA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ILL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-AP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-BI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-CH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-CT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-DD")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-DE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-DI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-DM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-GO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-GU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-HA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-JK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-KE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-KL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-KT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-MH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-MP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-MR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-OR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-PO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-PU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-RA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-TN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-UP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-WB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IND-YA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("INI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IOW-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRE-IR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRE-NI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRK-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IRQ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ITA-IT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ITA-SM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ITA-VC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("IVO-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAP-HK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAP-HN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAP-KY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAP-SH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JAW-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("JNF-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KAZ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KEG-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KEN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KER-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KGZ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KHA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KOR-NK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KOR-SK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KRY-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KTY-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KUR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KUW-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("KZN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LAB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LAO-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBS-LB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBS-SY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LBY-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LDV-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-AB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-AG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-AV")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-BV")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-GU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-MO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-NL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-SK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-SM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LEE-VI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LES-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LIN-KI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LIN-US")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LOU-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LSI-BA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LSI-ET")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("LSI-LS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAG-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAQ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MAU-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MCI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MCS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDG-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MDV-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MIC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MIN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLW-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLY-PM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MLY-SI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MNT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MON-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOR-MO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOR-SP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MOZ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MPE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRN-GU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRN-NM")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRQ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MRY-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MSI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MSO-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MTN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC-DF")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC-ME")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC-MO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC-PU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXC-TL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-AG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-CO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-CU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-DU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-GU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-HI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-NL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-QU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-SL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-TA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXE-ZA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXG-VC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXI-GU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXI-RA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXI-RG")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXN-BC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXN-BS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXN-SI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXN-SO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-CL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-GR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-JA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-MI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-NA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXS-OA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT-CA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT-CI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT-QR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT-TB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MXT-YU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("MYA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NAM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NAT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NBR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-CH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-DA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-IN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-KB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-KC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-KR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-SO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NCS-ST")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NDA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEP-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NET-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NEV-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFK-LH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFK-NI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFL-NE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NFL-SP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NGA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NGR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NIC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NLA-BO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NLA-CU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NNS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NOR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NRU-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NSC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NSW-CT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NSW-NS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NTA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NUE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NUN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWG-IJ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWG-PN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWH-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWJ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NWY-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NZN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("NZS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OFS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OGA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OHI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OKL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("OMA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ONT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ORE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAK-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAL-IS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAL-JO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PAR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PEI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PEN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PER-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PHI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PHX-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PIT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("POL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("POR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PRM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("PUE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("QLD-CS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("QLD-QU")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("QUE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("REU-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RHO-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ROD-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ROM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RUW-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("RWA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAK-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAM-AS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAM-WS")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SAU-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCS-PI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCS-SI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SCZ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SDA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SEY-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SGE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIC-MA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIC-SI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SIN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOL-NO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOL-SO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SOM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SPA-AN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SPA-GI")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SPA-SP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SRL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SSA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("STH-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUD-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SUR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SVA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWC-CC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWC-HC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWC-NC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("SWZ-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TAS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-AB")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-AD")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-AR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-AZ")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-GR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-NA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TCS-NK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TDC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TEN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TEX-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("THA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TKM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOG-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOK-MA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOK-SW")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TOK-TO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TON-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TRT-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUR-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TUV-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVL-GA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVL-MP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVL-NP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TVL-NW")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("TZK-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UGA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UKR-MO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UKR-UK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("URU-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UTA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("UZB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VAN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VEN-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VER-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VIC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VIE-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VNA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("VRG-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAK-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAL-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAU-AC")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WAU-WA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WDC-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WHM-HP")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WHM-JK")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WHM-UT")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-BA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-DO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-GR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-MA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-SL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIN-SV")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WIS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WSA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WSB-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WVA-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("WYO-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("XMS-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YAK-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YEM-NY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YEM-SY")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-BH")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-CR")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-KO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-MA")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-MN")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-SE")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUG-SL")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("YUK-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZAI-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZAM-OO")) { return AREA_ALBANIA; }
			else if (tdwgArea.isTdwgAreaAbbreviation("ZIM-OO")) { return AREA_ALBANIA; }
			else {
				logger.error("Unknown TdwgArea Area: " + area.getTitleCache());
				return null; // Actually the export has to stop here because AreaFk's are not allowed to be NULL.
			}
		} else if (area.isInstanceOf(WaterbodyOrCountry.class)) {
			if (area.equals(WaterbodyOrCountry.ALBANIAPEOPLESSOCIALISTREPUBLICOF())) {
				return AREA_ALBANIA;
			} else if (area.equals(WaterbodyOrCountry.AUSTRIAREPUBLICOF())) {
				return AREA_AUSTRIA;
			} else if (area.equals(WaterbodyOrCountry.LIECHTENSTEINPRINCIPALITYOF())) {
				return AREA_LIECHTENSTEIN;
			} else if (area.equals(WaterbodyOrCountry.BELGIUMKINGDOMOF())) {
				return AREA_BELGIUM;
			} else if (area.equals(WaterbodyOrCountry.LUXEMBOURGGRANDDUCHYOF())) {
				return AREA_LUXEMBOURG;
			} else if (area.equals(WaterbodyOrCountry.BOSNIAANDHERZEGOVINA())) {
				return AREA_BOSNIA_HERZEGOVINA;
			} else if (area.equals(WaterbodyOrCountry.UNITEDKINGDOMOFGREATBRITAINANDNORTHERNIRELAND())) {
				return AREA_GREAT_BRITAIN;
			} else if (area.equals(WaterbodyOrCountry.BULGARIAPEOPLESREPUBLICOF())) {
				return AREA_BULGARIA;
			} else if (area.equals(WaterbodyOrCountry.BELARUS())) {
				return AREA_BELARUS;
			} else if (area.equals(WaterbodyOrCountry.CZECHREPUBLIC())) {
				return AREA_CZECH_REPUBLIC;
			} else if (area.equals(WaterbodyOrCountry.CYPRUSREPUBLICOF())) {
				return AREA_CYPRUS;
			} else if (area.equals(WaterbodyOrCountry.DENMARKKINGDOMOF())) {
				return AREA_DENMARK_WITH_BORNHOLM; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.ESTONIA())) {
				return AREA_ESTONIA;
			} else if (area.equals(WaterbodyOrCountry.FAEROEISLANDS())) {
				return AREA_FAROE_ISLANDS;
			} else if (area.equals(WaterbodyOrCountry.FINLANDREPUBLICOF())) {
				return AREA_FINLAND_WITH_AHVENANMAA; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.FRANCEFRENCHREPUBLIC())) {
				return AREA_FRANCE;
			} else if (area.equals(WaterbodyOrCountry.MONACOPRINCIPALITYOF())) {
				return AREA_MONACO;
			} else if (area.equals(WaterbodyOrCountry.GERMANY())) {
				return AREA_GERMANY;
			} else if (area.equals(WaterbodyOrCountry.IRELAND())) {
				return AREA_IRELAND;
			} else if (area.equals(WaterbodyOrCountry.SWITZERLANDSWISSCONFEDERATION())) {
				return AREA_SWITZERLAND; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.NETHERLANDSKINGDOMOFTHE())) {
				return AREA_NETHERLANDS;
			} else if (area.equals(WaterbodyOrCountry.SPAINSPANISHSTATE())) {
				return AREA_SPAIN;
			} else if (area.equals(WaterbodyOrCountry.ANDORRAPRINCIPALITYOF())) {
				return AREA_ANDORRA;
			} else if (area.equals(WaterbodyOrCountry.GIBRALTAR())) {
				return AREA_GIBRALTAR;
			} else if (area.equals(WaterbodyOrCountry.HUNGARYHUNGARIANPEOPLESREPUBLIC())) {
				return AREA_HUNGARY;
			} else if (area.equals(WaterbodyOrCountry.ICELANDREPUBLICOF())) {
				return AREA_ICELAND;
			} else if (area.equals(WaterbodyOrCountry.ITALYITALIANREPUBLIC())) {
				return AREA_ITALY;
			} else if (area.equals(WaterbodyOrCountry.SANMARINOREPUBLICOF())) {
				return AREA_SAN_MARINO;
			} else if (area.equals(WaterbodyOrCountry.LATVIA())) {
				return AREA_LATVIA;
			} else if (area.equals(WaterbodyOrCountry.LITHUANIA())) {
				return AREA_LITHUANIA;
			} else if (area.equals(WaterbodyOrCountry.PORTUGALPORTUGUESEREPUBLIC())) {
				return AREA_PORTUGUESE_MAINLAND;
			} else if (area.equals(WaterbodyOrCountry.MACEDONIATHEFORMERYUGOSLAVREPUBLICOF())) {
				return AREA_THE_FORMER_JUGOSLAV_REPUBLIC_OF_MAKEDONIJA;
			} else if (area.equals(WaterbodyOrCountry.MOLDOVAREPUBLICOF())) {
				return AREA_MOLDOVA;
			} else if (area.equals(WaterbodyOrCountry.NORWAYKINGDOMOF())) {
				return AREA_NORWEGIAN_MAINLAND;
			} else if (area.equals(WaterbodyOrCountry.POLANDPOLISHPEOPLESREPUBLIC())) {
				return AREA_POLAND;
			} else if (area.equals(WaterbodyOrCountry.RUSSIANFEDERATION())) {
				return AREA_THE_RUSSIAN_FEDERATION;
			} else if (area.equals(WaterbodyOrCountry.ROMANIASOCIALISTREPUBLICOF())) {
				return AREA_ROMANIA;
			} else if (area.equals(WaterbodyOrCountry.SVALBARDJANMAYENISLANDS())) {
				return AREA_SVALBARD_WITH_BJORNOYA_AND_JAN_MAYEN;
			} else if (area.equals(WaterbodyOrCountry.MALTAREPUBLICOF())) {
				return AREA_MALTA;
			} else if (area.equals(WaterbodyOrCountry.SLOVAKIA())) {
				return AREA_SLOVAKIA;
			} else if (area.equals(WaterbodyOrCountry.SLOVENIA())) {
				return AREA_SLOVENIA;
			} else if (area.equals(WaterbodyOrCountry.SERBIAANDMONTENEGRO())) {
				return AREA_SERBIA_WITH_MONTENEGRO;
			} else if (area.equals(WaterbodyOrCountry.SWEDENKINGDOMOF())) {
				return AREA_SWEDEN;
			} else if (area.equals(WaterbodyOrCountry.UKRAINE())) {
				return AREA_UKRAINE;
			} else if (area.equals(WaterbodyOrCountry.GREECEHELLENICREPUBLIC())) {
				return AREA_GREEK_MAINLAND; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.VATICANCITYSTATE())) {
				return AREA_VATICAN_CITY;
			} else if (area.equals(WaterbodyOrCountry.AZERBAIJANREPUBLICOF())) {
				return AREA_AZERBAIJAN;
			} else if (area.equals(WaterbodyOrCountry.ALGERIAPEOPLESDEMOCRATICREPUBLICOF())) {
				return AREA_ALGERIA;
			} else if (area.equals(WaterbodyOrCountry.ARMENIA())) {
				return AREA_ARMENIA;
			} else if (area.equals(WaterbodyOrCountry.EGYPTARABREPUBLICOF())) {
				return AREA_EGYPT;
			} else if (area.equals(WaterbodyOrCountry.GEORGIA())) {
				return AREA_GEORGIA;
			} else if (area.equals(WaterbodyOrCountry.ISRAELSTATEOF())) {
				return AREA_ISRAEL;
			} else if (area.equals(WaterbodyOrCountry.JORDANHASHEMITEKINGDOMOF())) {
				return AREA_JORDAN; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.LEBANONLEBANESEREPUBLIC())) {
				return AREA_LEBANON;
			} else if (area.equals(WaterbodyOrCountry.LIBYANARABJAMAHIRIYA())) {
				return AREA_LIBYA; // maybe wrong?
			} else if (area.equals(WaterbodyOrCountry.MOROCCOKINGDOMOF())) {
				return AREA_MOROCCO;
			} else if (area.equals(WaterbodyOrCountry.SYRIANARABREPUBLIC())) {
				return AREA_SYRIA;
			} else if (area.equals(WaterbodyOrCountry.TUNISIAREPUBLICOF())) {
				return AREA_TUNISIA;
			} else if (area.equals(WaterbodyOrCountry.TURKEYREPUBLICOF())) {
				return AREA_TURKEY;
			} else if (area.equals(WaterbodyOrCountry.AUSTRALIACOMMONWEALTHOF())) {
				return AREA_AUSTRALIAN_REGION;
			} else if (area.equals(WaterbodyOrCountry.MEDITERRANEANSEA())) {
				return AREA_MEDITERRANEAN_SEA;
			} else if (area.equals(WaterbodyOrCountry.BLACKSEA())) {
				return AREA_BLACK_SEA;
			} else if (area.equals(WaterbodyOrCountry.CASPIANSEA())) {
				return AREA_CASPIAN_SEA;
			} else {
				logger.error("Unknown WaterbodyOrCountry Area: " + area.getTitleCache());
				return null; // Actually the export has to stop here because AreaFk's are not allowed to be NULL.
			}
		}
		return null; // Actually the export has to stop here because AreaFk's are not allowed to be NULL.
		
		// These areas are unknown:
//		EAST_AEGEAN_ISLANDS
//		GREEK_EAST_AEGEAN_ISLANDS
//		TURKISH_EAST_AEGEAN_ISLANDS
//		AUSTRIA_WITH_LIECHTENSTEIN
//		AZORES
//		CORVO
//		FAIAL
//		GRACIOSA
//		SAO_JORGE
//		FLORES
//		SAO_MIGUEL
//		PICO
//		SANTA_MARIA
//		TERCEIRA
//		BELGIUM_WITH_LUXEMBOURG
//		BALEARES
//		IBIZA_WITH_FORMENTERA
//		MALLORCA
//		MENORCA
//		BALTIC_STATES_AND_KALININGRAD_REGION
//		CANARY_ISLANDS
//		GRAN_CANARIA
//		FUERTEVENTURA_WITH_LOBOS
//		GOMERA
//		HIERRO
//		LANZAROTE_WITH_GRACIOSA
//		LA_PALMA
//		TENERIFE
//		MONTENEGRO
//		CORSE
//		CRETE_WITH_KARPATHOS,_KASOS_AND_GAVDHOS
//		CROATIA
//		FORMER_CZECHOSLOVAKIA
//		CHANNEL_ISLANDS
//		FRENCH_MAINLAND
//		GREECE_WITH_CYCLADES_AND_MORE_ISLANDS
//		REPUBLIC_OF_IRELAND
//		NORTHERN_IRELAND
//		KINGDOM_OF_SPAIN
//		ITALIAN_MAINLAND
//		FORMER_JUGOSLAVIA
//		MADEIRA (This one is: MADEIRA ARCHIPELAGO)
//		DESERTAS
//		MADEIRA
//		PORTO_SANTO
//		NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND
//		CENTRAL_EUROPEAN_RUSSIA
//		EASTERN_EUROPEAN_RUSSIA
//		KALININGRAD
//		NORTHERN_EUROPEAN_RUSSIA
//		NORTHWEST_EUROPEAN_RUSSIA
//		SOUTH_EUROPEAN_RUSSIA
//		FORMER_USSR
//		RUSSIA_BALTIC
//		RUSSIA_CENTRAL
//		RUSSIA_SOUTHEAST
//		RUSSIA_NORTHERN
//		RUSSIA_SOUTHWEST
//		SARDEGNA
//		SELVAGENS_ISLANDS
//		SICILY_WITH_MALTA
//		SICILY
//		SERBIA_INCLUDING_VOJVODINA_AND_WITH_KOSOVO
//		EUROPEAN_TURKEY
//		UKRAINE_INCLUDING_CRIMEA
//		CRIMEA
//		CRETE
//		DODECANESE_ISLANDS
//		CYCLADES_ISLANDS
//		NORTH_AEGEAN_ISLANDS
//		FRANZ_JOSEF_LAND
//		NOVAYA_ZEMLYA
//		AZERBAIJAN_INCLUDING_NAKHICHEVAN
//		NAKHICHEVAN
//		CAUCASUS_REGION
//		ISRAEL-JORDAN
//		LEBANON-SYRIA
//		NORTH_CAUCASUS
//		SINAI
//		ASIATIC_TURKEY
//		NORTHERN_AFRICA
//		AFRO_TROPICAL_REGION
//		EAST_PALAEARCTIC
//		NEARCTIC_REGION
//		NEOTROPICAL_REGION
//		NEAR_EAST
//		ORIENTAL_REGION
//		EUROPEAN_MARINE_WATERS
//		WHITE_SEA
//		NORTH_SEA
//		BALTIC_SEA
//		BARENTS_SEA
//		PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE
//		BELGIAN_EXCLUSIVE_ECONOMIC_ZONE
//		FRENCH_EXCLUSIVE_ECONOMIC_ZONE
//		ENGLISH_CHANNEL
//		ADRIATIC_SEA
//		BISCAY_BAY
//		DUTCH_EXCLUSIVE_ECONOMIC_ZONE
//		UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE
//		SPANISH_EXCLUSIVE_ECONOMIC_ZONE
//		EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE
//		GRECIAN_EXCLUSIVE_ECONOMIC_ZONE
//		TIRRENO_SEA
//		ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE
//		IRISH_EXCLUSIVE_ECONOMIC_ZONE
//		IRISH_SEA
//		ITALIAN_EXCLUSIVE_ECONOMIC_ZONE
//		NORWEGIAN_SEA
//		MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE
//		NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE
//		SKAGERRAK
//		TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE
//		WADDEN_SEA
//		BELT_SEA
//		MARMARA_SEA
//		SEA_OF_AZOV
//		AEGEAN_SEA
//		BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE
//		SOUTH_BALTIC_PROPER
//		BALTIC_PROPER
//		NORTH_BALTIC_PROPER
//		ARCHIPELAGO_SEA
//		BOTHNIAN_SEA
//		GERMAN_EXCLUSIVE_ECONOMIC_ZONE
//		SWEDISH_EXCLUSIVE_ECONOMIC_ZONE
//		UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE
//		MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE
//		LEBANESE_EXCLUSIVE_ECONOMIC_ZONE
//		SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART
//		ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE
//		CROATIAN_EXCLUSIVE_ECONOMIC_ZONE
//		BALEAR_SEA
//		TURKISH_EXCLUSIVE_ECONOMIC_ZONE
//		DANISH_EXCLUSIVE_ECONOMIC_ZONE

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
	 * Returns the SourceUseCache for a tiven sourceUseId.
	 * @param sourceUseId
	 * @return
	 */
	public static String sourceUseId2SourceUseCache(Integer sourceUseId) {
		// TODO: CDM sourceUseId and PESI sourceUseId are equal for now.
		String result = null;
		switch (sourceUseId) {
			case 3: return STR_ADDITIONAL_SOURCE;
			case 4: return STR_SOURCE_OF_SYNONYMY;
			case 8: return STR_NOMENCLATURAL_REFERENCE;
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
	
	/**
	 * Returns the LanguageCache to a given Language.
	 * @param language
	 * @return
	 */
	public static String language2LanguageCache(Language language) {
		if (language == null ) {
			return null;
		}
		if (language.equals(Language.ALBANIAN())) {
			return STR_LANGUAGE_ALBANIAN;
		} else if (language.equals(Language.ARABIC())) {
			return STR_LANGUAGE_ARABIC;
		} else if (language.equals(Language.ARMENIAN())) {
			return STR_LANGUAGE_ARMENIAN;
		} else if (language.equals(Language.AZERBAIJANI())) {
			return STR_LANGUAGE_AZERBAIJAN;
		} else if (language.equals(Language.BELORUSSIAN())) {
			return STR_LANGUAGE_BELARUSIAN;
		} else if (language.equals(Language.BULGARIAN())) {
			return STR_LANGUAGE_BULGARIAN;
		} else if (language.equals(Language.CATALAN_VALENCIAN())) {
			return STR_LANGUAGE_CATALAN;
		} else if (language.equals(Language.CROATIAN())) {
			return STR_LANGUAGE_CROAT;
		} else if (language.equals(Language.CZECH())) {
			return STR_LANGUAGE_CZECH;
		} else if (language.equals(Language.DANISH())) {
			return STR_LANGUAGE_DANISH;
		} else if (language.equals(Language.DUTCH_MIDDLE())) {
			return STR_LANGUAGE_DUTCH;
		} else if (language.equals(Language.ENGLISH())) {
			return STR_LANGUAGE_ENGLISH;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_EUSKERA;
		} else if (language.equals(Language.ESTONIAN())) {
			return STR_LANGUAGE_ESTONIAN;
		} else if (language.equals(Language.FINNISH())) {
			return STR_LANGUAGE_FINNISH;
		} else if (language.equals(Language.FRENCH())) {
			return STR_LANGUAGE_FRENCH;
		} else if (language.equals(Language.GEORGIAN())) {
			return STR_LANGUAGE_GEORGIAN;
		} else if (language.equals(Language.GERMAN())) {
			return STR_LANGUAGE_GERMAN;
		} else if (language.equals(Language.GREEK_MODERN())) {
			return STR_LANGUAGE_GREEK;
		} else if (language.equals(Language.HUNGARIAN())) {
			return STR_LANGUAGE_HUNGARIAN;
		} else if (language.equals(Language.ICELANDIC())) {
			return STR_LANGUAGE_ICELANDIC;
		} else if (language.equals(Language.IRISH())) {
			return STR_LANGUAGE_IRISH_GAELIC;
		} else if (language.equals(Language.HEBREW())) {
			return STR_LANGUAGE_ISRAEL_HEBREW;
		} else if (language.equals(Language.ITALIAN())) {
			return STR_LANGUAGE_ITALIAN;
		} else if (language.equals(Language.LATVIAN())) {
			return STR_LANGUAGE_LATVIAN;
		} else if (language.equals(Language.LITHUANIAN())) {
			return STR_LANGUAGE_LITHUANIAN;
		} else if (language.equals(Language.MACEDONIAN())) {
			return STR_LANGUAGE_MACEDONIAN;
		} else if (language.equals(Language.MALTESE())) {
			return STR_LANGUAGE_MALTESE;
		} else if (language.equals(Language.MOLDAVIAN())) {
			return STR_LANGUAGE_MOLDOVIAN;
		} else if (language.equals(Language.NORWEGIAN())) {
			return STR_LANGUAGE_NORWEGIAN;
		} else if (language.equals(Language.POLISH())) {
			return STR_LANGUAGE_POLISH;
		} else if (language.equals(Language.PORTUGUESE())) {
			return STR_LANGUAGE_PORTUGUESE;
		} else if (language.equals(Language.ROMANIAN())) {
			return STR_LANGUAGE_ROUMANIAN;
		} else if (language.equals(Language.RUSSIAN())) {
			return STR_LANGUAGE_RUSSIAN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_CAUCASIAN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_ALTAIC_KALMYK_OIRAT;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_ALTAIC_KARACHAY_BALKAR;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_ALTAIC_KUMYK;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_ALTAIC_NOGAI;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_ALTAIC_NORTH_AZERBAIJANI;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_RUSSIAN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_KALMYK_OIRAT;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_OSETIN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_ABAZA;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_ADYGHE;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_CHECHEN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_KABARDIAN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_LAK;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_AVAR;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_IN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_URALIC_CHUVASH;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_URALIC_UDMURT;
		} else if (language.equals(Language.SERBIAN())) {
			return STR_LANGUAGE_SERBIAN;
		} else if (language.equals(Language.SLOVAK())) {
			return STR_LANGUAGE_SLOVAK;
		} else if (language.equals(Language.SLOVENIAN())) {
			return STR_LANGUAGE_SLOVENE;
		} else if (language.equals(Language.SPANISH_CATALAN())) {
			return STR_LANGUAGE_SPANISH_CASTILLIAN;
		} else if (language.equals(Language.SWEDISH())) {
			return STR_LANGUAGE_SWEDISH;
		} else if (language.equals(Language.TURKISH())) {
			return STR_LANGUAGE_TURKISH;
		} else if (language.equals(Language.UKRAINIAN())) {
			return STR_LANGUAGE_UKRAINE;
		} else if (language.equals(Language.WELSH())) {
			return STR_LANGUAGE_WELSH;
		} else if (language.equals(Language.CORSICAN())) {
			return STR_LANGUAGE_CORSICAN;
		} else {
			logger.debug("Unknown Language: " + language.getTitleCache());
			return null;
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
		} else if (language.equals(Language.SPANISH_CATALAN())) {
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
	public static String textData2NodeCategoryCache(Feature feature) {
		if (feature == null) {
			return null;
		}
		if (feature.equals(Feature.DESCRIPTION())) {
			return NoteCategory_STR_description;
		} else if (feature.equals(Feature.ECOLOGY())) {
			return NoteCategory_STR_ecology;
		} else if (feature.equals(Feature.PHENOLOGY())) {
			return NoteCategory_STR_phenology;
		} else if (feature.equals(Feature.COMMON_NAME())) {
			return NoteCategory_STR_Common_names;
		} else if (feature.equals(Feature.OCCURRENCE())) {
			return NoteCategory_STR_Occurrence;
//		} else if (feature.equals(Feature.CITATION())) {
//			return;
			
			// TODO: Unknown NodeCategories
//			NoteCategory_general_distribution_euromed = 10;
//			NoteCategory_general_distribution_world = 11;
//			NoteCategory_Common_names = 12;
//			NoteCategory_Maps =14;
//			NoteCategory_Link_to_maps = 20;
//			NoteCategory_Link_to_images = 21;
//			NoteCategory_Link_to_taxonomy = 22;
//			NoteCategory_Link_to_general_information = 23;
//			NoteCategory_undefined_link = 24;
//			NoteCategory_Editor_Braces = 249;
//			NoteCategory_Editor_Brackets = 250;
//			NoteCategory_Editor_Parenthesis = 251;
//			NoteCategory_Inedited = 252;
//			NoteCategory_Comments_on_editing_process = 253;
//			NoteCategory_Publication_date = 254;
//			NoteCategory_Morphology = 255;
//			NoteCategory_Acknowledgments = 257;
//			NoteCategory_Original_publication = 258;
//			NoteCategory_Type_locality	= 259;
//			NoteCategory_Environment = 260;
//			NoteCategory_Spelling = 261;
//			NoteCategory_Systematics = 262;
//			NoteCategory_Remark = 263;
//			NoteCategory_Date_of_publication = 264;
//			NoteCategory_Additional_information = 266;
//			NoteCategory_Status = 267;
//			NoteCategory_Nomenclature = 268;
//			NoteCategory_Homonymy = 269;
//			NoteCategory_Taxonomy = 270;
//			NoteCategory_Taxonomic_status = 272;
//			NoteCategory_Authority	= 273;
//			NoteCategory_Identification = 274;
//			NoteCategory_Validity = 275;
//			NoteCategory_Classification = 276;
//			NoteCategory_Distribution = 278;
//			NoteCategory_Synonymy = 279;
//			NoteCategory_Habitat = 280;
//			NoteCategory_Biology = 281;
//			NoteCategory_Diagnosis	= 282;
//			NoteCategory_Host = 283;
//			NoteCategory_Note = 284;
//			NoteCategory_Rank = 285;
//			NoteCategory_Taxonomic_Remark = 286;
//			NoteCategory_Taxonomic_Remarks = 287;

 		} else {
			logger.debug("Unknown Feature.");
			return null;
		}
	}

	/**
	 * Returns the NodeCategoryFk for a given TextData.
	 * @param feature
	 * @return
	 */
	public static Integer textData2NodeCategoryFk(Feature feature) {
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
//		} else if (feature.equals(Feature.CITATION())) {
//			return;
			
			// TODO: Unknown NodeCategories
//			NoteCategory_general_distribution_euromed = 10;
//			NoteCategory_general_distribution_world = 11;
//			NoteCategory_Common_names = 12;
//			NoteCategory_Maps =14;
//			NoteCategory_Link_to_maps = 20;
//			NoteCategory_Link_to_images = 21;
//			NoteCategory_Link_to_taxonomy = 22;
//			NoteCategory_Link_to_general_information = 23;
//			NoteCategory_undefined_link = 24;
//			NoteCategory_Editor_Braces = 249;
//			NoteCategory_Editor_Brackets = 250;
//			NoteCategory_Editor_Parenthesis = 251;
//			NoteCategory_Inedited = 252;
//			NoteCategory_Comments_on_editing_process = 253;
//			NoteCategory_Publication_date = 254;
//			NoteCategory_Morphology = 255;
//			NoteCategory_Acknowledgments = 257;
//			NoteCategory_Original_publication = 258;
//			NoteCategory_Type_locality	= 259;
//			NoteCategory_Environment = 260;
//			NoteCategory_Spelling = 261;
//			NoteCategory_Systematics = 262;
//			NoteCategory_Remark = 263;
//			NoteCategory_Date_of_publication = 264;
//			NoteCategory_Additional_information = 266;
//			NoteCategory_Status = 267;
//			NoteCategory_Nomenclature = 268;
//			NoteCategory_Homonymy = 269;
//			NoteCategory_Taxonomy = 270;
//			NoteCategory_Taxonomic_status = 272;
//			NoteCategory_Authority	= 273;
//			NoteCategory_Identification = 274;
//			NoteCategory_Validity = 275;
//			NoteCategory_Classification = 276;
//			NoteCategory_Distribution = 278;
//			NoteCategory_Synonymy = 279;
//			NoteCategory_Habitat = 280;
//			NoteCategory_Biology = 281;
//			NoteCategory_Diagnosis	= 282;
//			NoteCategory_Host = 283;
//			NoteCategory_Note = 284;
//			NoteCategory_Rank = 285;
//			NoteCategory_Taxonomic_Remark = 286;
//			NoteCategory_Taxonomic_Remarks = 287;

		}else{
			logger.debug("Unknown Feature.");
			return null;
		}
	}

	/**
	 * Returns the string representation for a given rank.
	 * @param rank
	 * @param pesiKingdomId
	 * @return
	 */
	public static String rank2RankCache(Rank rank, Integer pesiKingdomId) {
		String result = null;
		if (rank == null) {
			return null;
		}
		
		// We differentiate between Animalia and Plantae only for now.
		if (pesiKingdomId == KINGDOM_ANIMALIA) {
			if (rank.equals(Rank.KINGDOM())) {
				result = Animalia_STR_Kingdom;
			} else if (rank.equals(Rank.SUBKINGDOM())) {
				result = Animalia_STR_Subkingdom;
			} else if (rank.equals(Rank.SUPERPHYLUM())) {
				result = Animalia_STR_Superphylum;
			} else if (rank.equals(Rank.PHYLUM())) {
				result = Animalia_STR_Phylum;
			} else if (rank.equals(Rank.SUBPHYLUM())) {
				result = Animalia_STR_Subphylum;
			} else if (rank.equals(Rank.INFRAPHYLUM())) {
				result = Animalia_STR_Infraphylum;
			} else if (rank.equals(Rank.SUPERCLASS())) {
				result = Animalia_STR_Superclass;
			} else if (rank.equals(Rank.CLASS())) {
				result = Animalia_STR_Class;
			} else if (rank.equals(Rank.SUBCLASS())) {
				result = Animalia_STR_Subclass;
			} else if (rank.equals(Rank.INFRACLASS())) {
				result = Animalia_STR_Infraclass;
			} else if (rank.equals(Rank.SUPERORDER())) {
				result = Animalia_STR_Superorder;
			} else if (rank.equals(Rank.ORDER())) {
				result = Animalia_STR_Order;
			} else if (rank.equals(Rank.SUBORDER())) {
				result = Animalia_STR_Suborder;
			} else if (rank.equals(Rank.INFRAORDER())) {
				result = Animalia_STR_Infraorder;
			} else if (rank.equals(Rank.SECTION_ZOOLOGY())) {
				result = Animalia_STR_Section;
			} else if (rank.equals(Rank.SUBSECTION_ZOOLOGY())) {
				result = Animalia_STR_Subsection;
			} else if (rank.equals(Rank.SUPERFAMILY())) {
				result = Animalia_STR_Superfamily;
			} else if (rank.equals(Rank.FAMILY())) {
				result = Animalia_STR_Family;
			} else if (rank.equals(Rank.SUBFAMILY())) {
				result = Animalia_STR_Subfamily;
			} else if (rank.equals(Rank.TRIBE())) {
				result = Animalia_STR_Tribe;
			} else if (rank.equals(Rank.SUBTRIBE())) {
				result = Animalia_STR_Subtribe;
			} else if (rank.equals(Rank.GENUS())) {
				result = Animalia_STR_Genus;
			} else if (rank.equals(Rank.SUBGENUS())) {
				result = Animalia_STR_Subgenus;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Animalia_STR_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Animalia_STR_Subspecies;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Animalia_STR_Natio;
			} else if (rank.equals(Rank.VARIETY())) {
				result = Animalia_STR_Variety;
			} else if (rank.equals(Rank.SUBVARIETY())) {
				result = Animalia_STR_Subvariety;
			} else if (rank.equals(Rank.FORM())) {
				result = Animalia_STR_Forma;
			}
		} else if (pesiKingdomId == KINGDOM_PLANTAE) {
			if (rank.equals(Rank.KINGDOM())) {
				result = Plantae_STR_Kingdom;
			} else if (rank.equals(Rank.SUBKINGDOM())) {
				result = Plantae_STR_Subkingdom;
			} else if (rank.equals(Rank.DIVISION())) {
				result = Plantae_STR_Division;
			} else if (rank.equals(Rank.SUBDIVISION())) {
				result = Plantae_STR_Subdivision;
			} else if (rank.equals(Rank.CLASS())) {
				result = Plantae_STR_Class;
			} else if (rank.equals(Rank.SUBCLASS())) {
				result = Plantae_STR_Subclass;
			} else if (rank.equals(Rank.ORDER())) {
				result = Plantae_STR_Order;
			} else if (rank.equals(Rank.SUBORDER())) {
				result = Plantae_STR_Suborder;
			} else if (rank.equals(Rank.FAMILY())) {
				result = Plantae_STR_Family;
			} else if (rank.equals(Rank.SUBFAMILY())) {
				result = Plantae_STR_Subfamily;
			} else if (rank.equals(Rank.TRIBE())) {
				result = Plantae_STR_Tribe;
			} else if (rank.equals(Rank.SUBTRIBE())) {
				result = Plantae_STR_Subtribe;
			} else if (rank.equals(Rank.GENUS())) {
				result = Plantae_STR_Genus;
			} else if (rank.equals(Rank.SUBGENUS())) {
				result = Plantae_STR_Subgenus;
			} else if (rank.equals(Rank.SECTION_BOTANY())) {
				result = Plantae_STR_Section;
			} else if (rank.equals(Rank.SUBSECTION_BOTANY())) {
				result = Plantae_STR_Subsection;
			} else if (rank.equals(Rank.SERIES())) {
				result = Plantae_STR_Series;
			} else if (rank.equals(Rank.SUBSERIES())) {
				result = Plantae_STR_Subseries;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Aggregate;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Coll_Species;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Plantae_STR_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Plantae_STR_Subspecies;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Proles;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Race;
			} else if (rank.equals(Rank.CONVAR())) {
				result = Plantae_STR_Convarietas;
			} else if (rank.equals(Rank.VARIETY())) {
				result = Plantae_STR_Variety;
			} else if (rank.equals(Rank.SUBVARIETY())) {
				result = Plantae_STR_Subvariety;
			} else if (rank.equals(Rank.FORM())) {
				result = Plantae_STR_Forma;
			} else if (rank.equals(Rank.SUBFORM())) {
				result = Plantae_STR_Subforma;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Forma_spec;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Taxa_infragen;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Taxa_infraspec;
			}
		} else {
			//TODO Exception
			logger.warn("Rank not yet supported in CDM: "+ rank.getLabel());
			return null;
		}
		return result;
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
		if (pesiKingdomId == KINGDOM_ANIMALIA) {
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
			}
		} else if (pesiKingdomId == KINGDOM_PLANTAE) {
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
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Aggregate;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Coll_Species;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Plantae_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Plantae_Subspecies;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Proles;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Race;
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
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Taxa_infragen;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Taxa_infraspec;
			}
		} else {
			//TODO Exception
			logger.warn("Rank not yet supported in CDM: "+ rank.getLabel());
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
	 * 
	 * @param taxonBase
	 * @return
	 */
	public static Integer taxonBase2statusFk (TaxonBase<?> taxonBase){
		if (taxonBase == null){return null;}		
		if (taxonBase.isInstanceOf(Taxon.class)){
			return T_STATUS_ACCEPTED;
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

	/**
	 * 
	 * @param taxonBase
	 * @return
	 */
	public static String taxonBase2statusCache (TaxonBase<?> taxonBase){
		if (taxonBase == null){return null;}
		if (taxonBase.isInstanceOf(Taxon.class)){
			return T_STATUS_STR_ACCEPTED;
		}else if (taxonBase.isInstanceOf(Synonym.class)){
			return T_STATUS_STR_SYNONYM;
		}else{
			logger.warn("Unknown ");
			return T_STATUS_STR_UNRESOLVED;
		}
		//TODO 
//		public static int T_STATUS_STR_PARTIAL_SYN = 3;
//		public static int T_STATUS_STR_PRO_PARTE_SYN = 4;
//		public static int T_STATUS_STR_UNRESOLVED = 5;
//		public static int T_STATUS_STR_ORPHANED = 6;
	}
		
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

	/**
	 * 
	 * @param status
	 * @return
	 */
	public static String nomStatus2NomStatusCache(NomenclaturalStatusType status) {
		if (status == null){
			return null;
		}
		if (status.equals(NomenclaturalStatusType.INVALID())) {return NAME_ST_STR_NOM_INVAL;
		}else if (status.equals(NomenclaturalStatusType.ILLEGITIMATE())) {return NAME_ST_STR_NOM_ILLEG;
		}else if (status.equals(NomenclaturalStatusType.NUDUM())) {return NAME_ST_STR_NOM_NUD;
		}else if (status.equals(NomenclaturalStatusType.REJECTED())) {return NAME_ST_STR_NOM_REJ;
		}else if (status.equals(NomenclaturalStatusType.REJECTED_PROP())) {return NAME_ST_STR_NOM_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED())) {return NAME_ST_STR_NOM_UTIQUE_REJ;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED_PROP())) {return NAME_ST_STR_NOM_UTIQUE_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.CONSERVED())) {return NAME_ST_STR_NOM_CONS;
	
		}else if (status.equals(NomenclaturalStatusType.CONSERVED_PROP())) {return NAME_ST_STR_NOM_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED())) {return NAME_ST_STR_ORTH_CONS;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP())) {return NAME_ST_STR_ORTH_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.SUPERFLUOUS())) {return NAME_ST_STR_NOM_SUPERFL;
		}else if (status.equals(NomenclaturalStatusType.AMBIGUOUS())) {return NAME_ST_STR_NOM_AMBIG;
		}else if (status.equals(NomenclaturalStatusType.PROVISIONAL())) {return NAME_ST_STR_NOM_PROVIS;
		}else if (status.equals(NomenclaturalStatusType.DOUBTFUL())) {return NAME_ST_STR_NOM_DUB;
		}else if (status.equals(NomenclaturalStatusType.NOVUM())) {return NAME_ST_STR_NOM_NOV;
	
		}else if (status.equals(NomenclaturalStatusType.CONFUSUM())) {return NAME_ST_STR_NOM_CONFUS;
		}else if (status.equals(NomenclaturalStatusType.ALTERNATIVE())) {return NAME_ST_STR_NOM_ALTERN;
		}else if (status.equals(NomenclaturalStatusType.COMBINATION_INVALID())) {return NAME_ST_STR_COMB_INVAL;
		}else if (status.equals(NomenclaturalStatusType.LEGITIMATE())) {return NAME_ST_STR_LEGITIMATE;
		
		// The following are non-existent in CDM
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_COMB_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_COMB_AND_STAT_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_NOM_AND_ORTH_CONS;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_NOM_NOV_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_SP_NOV_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_ALTERNATE_REPRESENTATION;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_TEMPORARY_NAME;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_SPECIES_INQUIRENDA;

		//TODO
		}else {
			//TODO Exception
			logger.warn("NomStatus type not yet supported by PESI export: "+ status);
		return null;
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
		
		// The following are non-existent in CDM
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_COMB_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_COMB_AND_STAT_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_NOM_AND_ORTH_CONS;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_NOM_NOV_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_SP_NOV_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_ALTERNATE_REPRESENTATION;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_TEMPORARY_NAME;
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
	public static String taxonRelation2RelTaxonQualifierCache(RelationshipBase<?,?,?> relation){
		if (relation == null) {
			return null;
		}
		RelationshipTermBase<?> type = relation.getType();
		if (type.equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
			return STR_IS_TAXONOMICALLY_INCLUDED_IN;
		} else if (type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())) {
			return STR_IS_MISAPPLIED_NAME_FOR;
		} else if (type.equals(SynonymRelationshipType.SYNONYM_OF())) {
			return STR_IS_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
			return STR_IS_HOMOTYPIC_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())) {
			return STR_IS_HETEROTYPIC_SYNONYM_OF;
		}

		// The following have no equivalent attribute in CDM
//		IS_BASIONYM_FOR
//		IS_LATER_HOMONYM_OF
//		IS_REPLACED_SYNONYM_FOR
//		IS_VALIDATION_OF
//		IS_LATER_VALIDATION_OF
//		IS_TYPE_OF
//		IS_CONSERVED_TYPE_OF
//		IS_REJECTED_TYPE_OF
//		IS_FIRST_PARENT_OF
//		IS_SECOND_PARENT_OF
//		IS_FEMALE_PARENT_OF
//		IS_MALE_PARENT_OF
//		IS_CONSERVED_AGAINST
//		IS_REJECTED_IN_FAVOUR_OF
//		IS_TREATED_AS_LATER_HOMONYM_OF
//		IS_ORTHOGRAPHIC_VARIANT_OF
//		IS_ALTERNATIVE_NAME_FOR
//		HAS_SAME_TYPE_AS
//		IS_LECTOTYPE_OF
//		TYPE_NOT_DESIGNATED
//		IS_PRO_PARTE_SYNONYM_OF
//		IS_PARTIAL_SYNONYM_OF
//		IS_PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HETEROTYPIC_SYNONYM_OF
//		IS_INFERRED_EPITHET_FOR
//		IS_INFERRED_GENUS_FOR
//		IS_POTENTIAL_COMBINATION_FOR

		return null;
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
		if (type.equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
			return IS_TAXONOMICALLY_INCLUDED_IN;
		} else if (type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())) {
			return IS_MISAPPLIED_NAME_FOR;
		} else if (type.equals(SynonymRelationshipType.SYNONYM_OF())) {
			return IS_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
			return IS_HOMOTYPIC_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())) {
			return IS_HETEROTYPIC_SYNONYM_OF;
		}

		// The following have no equivalent attribute in CDM
//		IS_BASIONYM_FOR
//		IS_LATER_HOMONYM_OF
//		IS_REPLACED_SYNONYM_FOR
//		IS_VALIDATION_OF
//		IS_LATER_VALIDATION_OF
//		IS_TYPE_OF
//		IS_CONSERVED_TYPE_OF
//		IS_REJECTED_TYPE_OF
//		IS_FIRST_PARENT_OF
//		IS_SECOND_PARENT_OF
//		IS_FEMALE_PARENT_OF
//		IS_MALE_PARENT_OF
//		IS_CONSERVED_AGAINST
//		IS_REJECTED_IN_FAVOUR_OF
//		IS_TREATED_AS_LATER_HOMONYM_OF
//		IS_ORTHOGRAPHIC_VARIANT_OF
//		IS_ALTERNATIVE_NAME_FOR
//		HAS_SAME_TYPE_AS
//		IS_LECTOTYPE_OF
//		TYPE_NOT_DESIGNATED
//		IS_PRO_PARTE_SYNONYM_OF
//		IS_PARTIAL_SYNONYM_OF
//		IS_PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HETEROTYPIC_SYNONYM_OF
//		IS_INFERRED_EPITHET_FOR
//		IS_INFERRED_GENUS_FOR
//		IS_POTENTIAL_COMBINATION_FOR

		return null;
	}
}
