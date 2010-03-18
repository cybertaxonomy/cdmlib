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
			if (tdwgArea.equals(TdwgArea.ABBREV_1())) {	return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_2 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_3 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_4 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_5 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_6 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_7 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_8 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_9 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_10 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_11 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_12 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_13 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_14 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_20 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_21 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_22 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_23 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_24 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_25 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_26 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_27 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_28 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_29 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_30 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_31 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_32 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_33 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_34 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_35 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_36 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_37 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_38 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_40 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_41 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_42 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_43 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_50 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_51 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_60 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_61 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_62 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_63 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_70 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_71 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_72 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_73 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_74 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_75 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_76 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_77 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_78 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_79 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_80 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_81 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_82 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_83 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_84 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_85 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_90 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_91 ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ABT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AFG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALD ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AMU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AND ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ANG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ANT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ATP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AUT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AZO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BEN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BER ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BGM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BIS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BKN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLZ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BRC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BRY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BUL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BUR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAF ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CBD ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CGS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHQ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHX ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CKI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CMN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CNT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CNY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CON ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPV ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CRL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CRZ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CTA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CTM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CUB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CVI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CYP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CZE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DEL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DEN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DJI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DOM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DSV ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EAI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EAS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ECU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EGY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EHM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ELS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EQG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ERI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ETH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FAL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FIJ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FIN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FLA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FOR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GEO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GER ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GGI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GHA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GIL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GNB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GNL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GRB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GRC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GST ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAW ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HBI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HMD ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HON ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HUN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ICE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IDA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ILL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_INI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IOW ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRQ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ITA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IVO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAW ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JNF ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAZ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KEG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KEN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KER ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KGZ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KHA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KOR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KTY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KUR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KUW ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KZN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LAB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LAO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LDV ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LES ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LIN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LOU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LSI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAQ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MCI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MCS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDV ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MIC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MIN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLW ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MNT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MON ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOZ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MPE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRQ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MSI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MSO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MTN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MYA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NAM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NAT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NBR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NDA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NET ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEV ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NGA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NGR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NIC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NLA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NNS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NOR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NRU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NSC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NSW ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NTA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NUE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NUN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWJ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NZN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NZS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OFS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OGA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OHI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OKL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OMA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ONT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ORE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PEI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PEN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PER ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PHI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PHX ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PIT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_POL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_POR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PRM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PUE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_QLD ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_QUE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_REU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RHO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ROD ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ROM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUW ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RWA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCZ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SDA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SGE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SPA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SRL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SSA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_STH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUD ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SVA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWZ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TDC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TEN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TEX ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_THA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TKM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TON ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TRT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUV ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TZK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UGA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UKR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_URU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UTA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UZB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VAN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VEN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VER ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VIC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VIE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VNA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VRG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WDC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WHM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WSA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WSB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WVA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WYO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_XMS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YAK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YEM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZAI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZAM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZIM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ABT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AFG_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_BA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_CH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_CN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_CO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_DF ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_ER ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_FO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_LP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_MI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_CB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_NE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_RN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_SC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_SF ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_TF ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_CA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_JU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_LR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_ME ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_SA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_SE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_SJ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_SL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_TU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALD_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALG_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALU_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AMU_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AND_AN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AND_CO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ANG_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ANT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARK_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARU_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASK_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASP_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_AS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_MA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_ME ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_MI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_NA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_TR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ATP_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AUT_AU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AUT_LI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AZO_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAH_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BEN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BER_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BGM_BE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BGM_LU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BIS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BKN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLT_ES ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLT_KA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLT_LA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLT_LI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLZ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOR_BR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOR_KA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOR_SB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOR_SR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOU_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BRC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BRY_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BUL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BUR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZC_DF ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZC_GO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZC_MS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZC_MT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_AL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_BA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_CE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_FN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_MA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_PB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_PE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_PI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_RN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_SE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL_ES ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL_MG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL_RJ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL_SP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL_TR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_AC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_AM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_AP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_PA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_RM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_RO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_TO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZS_PR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZS_RS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZS_SC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAF_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAY_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CBD_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CGS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC_CQ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC_GZ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC_HU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC_SC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC_YN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHH_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHI_NM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHI_NX ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHM_HJ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHM_JL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHM_LN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_BJ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_GS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_HB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_SA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_SD ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_SX ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_TJ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHQ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_AH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_FJ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_GD ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_GX ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_HE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_HK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_HN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_JS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_JX ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_KI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_MA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_MP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_SH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_ZJ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHX_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CKI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_BI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_CO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_LA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_MA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_OH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_SA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_VA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLN_AN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLN_AT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLN_TA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLS_AI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLS_LL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLS_MG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CMN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CNT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CNY_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COM_CO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COM_MA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CON_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COO_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPI_CL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPI_CO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPI_MA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPP_EC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPP_NC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPP_WC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPV_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CRL_MF ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CRL_PA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CRZ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CTA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CTM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CUB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CVI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CYP_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CZE_CZ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CZE_SK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DEL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DEN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DJI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DOM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DSV_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EAI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EAS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ECU_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EGY_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EHM_AP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EHM_BH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EHM_DJ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EHM_SI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ELS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EQG_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ERI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ETH_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FAL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FIJ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FIN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FLA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FOR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRA_CI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRA_FR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRA_MO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRG_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GEO_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GER_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GGI_AN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GGI_BI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GGI_PR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GGI_ST ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GHA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GIL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GNB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GNL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GRB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GRC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GST_BA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GST_QA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GST_UA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUY_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAI_HA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAI_NI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAW_HI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAW_JI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAW_MI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HBI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HMD_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HON_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HUN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ICE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IDA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ILL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_AP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_BI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_CH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_CT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_DD ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_DE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_DI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_DM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_GO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_GU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_HA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_JK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_KE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_KL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_KT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_MH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_MP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_MR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_OR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_PO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_PU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_RA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_TN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_UP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_WB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_YA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_INI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IOW_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRE_IR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRE_NI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRK_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRQ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ITA_IT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ITA_SM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ITA_VC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IVO_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAP_HK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAP_HN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAP_KY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAP_SH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAW_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JNF_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAZ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KEG_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KEN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KER_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KGZ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KHA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KOR_NK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KOR_SK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRY_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KTY_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KUR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KUW_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KZN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LAB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LAO_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBS_LB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBS_SY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBY_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LDV_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_AB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_AG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_AV ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_BV ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_GU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_MO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_NL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_SK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_SM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_VI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LES_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LIN_KI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LIN_US ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LOU_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LSI_BA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LSI_ET ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LSI_LS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAG_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAQ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAU_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MCI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MCS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDG_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDV_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MIC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MIN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLW_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLY_PM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLY_SI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MNT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MON_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOR_MO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOR_SP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOZ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MPE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRN_GU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRN_NM ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRQ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRY_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MSI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MSO_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MTN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC_DF ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC_ME ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC_MO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC_PU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC_TL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_AG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_CO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_CU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_DU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_GU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_HI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_NL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_QU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_SL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_TA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_ZA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXG_VC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXI_GU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXI_RA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXI_RG ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXN_BC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXN_BS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXN_SI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXN_SO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_CL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_GR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_JA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_MI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_NA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_OA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT_CA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT_CI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT_QR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT_TB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT_YU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MYA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NAM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NAT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NBR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_CH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_DA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_IN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_KB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_KC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_KR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_SO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_ST ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NDA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEP_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NET_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEV_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFK_LH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFK_NI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFL_NE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFL_SP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NGA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NGR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NIC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NLA_BO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NLA_CU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NNS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NOR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NRU_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NSC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NSW_CT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NSW_NS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NTA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NUE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NUN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWG_IJ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWG_PN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWH_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWJ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWY_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NZN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NZS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OFS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OGA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OHI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OKL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OMA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ONT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ORE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAK_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAL_IS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAL_JO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PEI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PEN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PER_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PHI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PHX_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PIT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_POL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_POR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PRM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PUE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_QLD_CS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_QLD_QU ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_QUE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_REU_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RHO_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ROD_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ROM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUW_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RWA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAK_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAM_AS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAM_WS ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAU_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCS_PI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCS_SI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCZ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SDA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEY_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SGE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIC_MA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIC_SI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOL_NO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOL_SO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SPA_AN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SPA_GI ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SPA_SP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SRL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SSA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_STH_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUD_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SVA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWC_CC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWC_HC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWC_NC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWZ_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_AB ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_AD ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_AR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_AZ ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_GR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_NA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_NK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TDC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TEN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TEX_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_THA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TKM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOG_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOK_MA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOK_SW ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOK_TO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TON_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TRT_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUR_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUV_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVL_GA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVL_MP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVL_NP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVL_NW ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TZK_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UGA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UKR_MO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UKR_UK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_URU_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UTA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UZB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VAN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VEN_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VER_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VIC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VIE_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VNA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VRG_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAK_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAL_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAU_AC ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAU_WA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WDC_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WHM_HP ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WHM_JK ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WHM_UT ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_BA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_DO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_GR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_MA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_SL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_SV ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WSA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WSB_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WVA_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WYO_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_XMS_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YAK_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YEM_NY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YEM_SY ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_BH ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_CR ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_KO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_MA ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_MN ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_SE ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_SL ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUK_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZAI_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZAM_OO ())) { return STR_AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZIM_OO ())) { return STR_AREA_ALBANIA; }
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
			if (tdwgArea.equals(TdwgArea.ABBREV_1())) {	return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_2 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_3 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_4 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_5 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_6 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_7 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_8 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_9 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_10 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_11 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_12 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_13 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_14 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_20 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_21 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_22 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_23 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_24 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_25 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_26 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_27 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_28 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_29 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_30 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_31 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_32 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_33 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_34 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_35 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_36 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_37 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_38 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_40 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_41 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_42 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_43 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_50 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_51 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_60 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_61 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_62 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_63 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_70 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_71 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_72 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_73 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_74 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_75 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_76 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_77 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_78 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_79 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_80 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_81 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_82 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_83 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_84 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_85 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_90 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_91 ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ABT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AFG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALD ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AMU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AND ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ANG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ANT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ATP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AUT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AZO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BEN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BER ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BGM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BIS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BKN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLZ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BRC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BRY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BUL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BUR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAF ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CBD ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CGS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHQ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHX ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CKI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CMN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CNT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CNY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CON ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPV ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CRL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CRZ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CTA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CTM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CUB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CVI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CYP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CZE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DEL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DEN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DJI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DOM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DSV ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EAI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EAS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ECU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EGY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EHM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ELS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EQG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ERI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ETH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FAL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FIJ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FIN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FLA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FOR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GEO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GER ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GGI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GHA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GIL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GNB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GNL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GRB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GRC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GST ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAW ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HBI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HMD ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HON ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HUN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ICE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IDA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ILL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_INI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IOW ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRQ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ITA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IVO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAW ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JNF ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAZ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KEG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KEN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KER ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KGZ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KHA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KOR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KTY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KUR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KUW ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KZN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LAB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LAO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LDV ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LES ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LIN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LOU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LSI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAQ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MCI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MCS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDV ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MIC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MIN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLW ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MNT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MON ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOZ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MPE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRQ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MSI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MSO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MTN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MYA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NAM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NAT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NBR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NDA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NET ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEV ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NGA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NGR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NIC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NLA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NNS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NOR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NRU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NSC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NSW ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NTA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NUE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NUN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWJ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NZN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NZS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OFS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OGA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OHI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OKL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OMA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ONT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ORE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PEI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PEN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PER ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PHI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PHX ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PIT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_POL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_POR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PRM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PUE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_QLD ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_QUE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_REU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RHO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ROD ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ROM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUW ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RWA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCZ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SDA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SGE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SPA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SRL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SSA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_STH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUD ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SVA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWZ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TDC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TEN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TEX ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_THA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TKM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TON ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TRT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUV ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TZK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UGA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UKR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_URU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UTA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UZB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VAN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VEN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VER ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VIC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VIE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VNA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VRG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WDC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WHM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WSA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WSB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WVA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WYO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_XMS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YAK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YEM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZAI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZAM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZIM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ABT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AFG_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_BA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_CH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_CN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_CO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_DF ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_ER ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_FO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_LP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGE_MI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_CB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_NE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_RN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_SC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_SF ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGS_TF ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_CA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_JU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_LR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_ME ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_SA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_SE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_SJ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_SL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AGW_TU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALD_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALG_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ALU_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AMU_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AND_AN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AND_CO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ANG_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ANT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARK_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ARU_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASK_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASP_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_AS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_MA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_ME ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_MI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_NA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ASS_TR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ATP_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AUT_AU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AUT_LI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_AZO_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAH_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BAN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BEN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BER_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BGM_BE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BGM_LU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BIS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BKN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLT_ES ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLT_KA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLT_LA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLT_LI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BLZ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOR_BR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOR_KA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOR_SB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOR_SR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BOU_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BRC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BRY_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BUL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BUR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZC_DF ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZC_GO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZC_MS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZC_MT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_AL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_BA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_CE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_FN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_MA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_PB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_PE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_PI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_RN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZE_SE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL_ES ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL_MG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL_RJ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL_SP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZL_TR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_AC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_AM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_AP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_PA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_RM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_RO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZN_TO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZS_PR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZS_RS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_BZS_SC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAF_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CAY_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CBD_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CGS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC_CQ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC_GZ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC_HU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC_SC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHC_YN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHH_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHI_NM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHI_NX ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHM_HJ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHM_JL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHM_LN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_BJ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_GS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_HB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_SA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_SD ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_SX ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHN_TJ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHQ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_AH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_FJ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_GD ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_GX ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_HE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_HK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_HN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_JS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_JX ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_KI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_MA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_MP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_SH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHS_ZJ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CHX_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CKI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_BI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_CO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_LA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_MA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_OH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_SA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLC_VA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLN_AN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLN_AT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLN_TA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLS_AI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLS_LL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CLS_MG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CMN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CNT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CNY_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COM_CO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COM_MA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CON_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COO_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_COS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPI_CL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPI_CO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPI_MA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPP_EC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPP_NC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPP_WC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CPV_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CRL_MF ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CRL_PA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CRZ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CTA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CTM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CUB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CVI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CYP_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CZE_CZ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_CZE_SK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DEL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DEN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DJI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DOM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_DSV_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EAI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EAS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ECU_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EGY_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EHM_AP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EHM_BH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EHM_DJ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EHM_SI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ELS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_EQG_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ERI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ETH_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FAL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FIJ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FIN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FLA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FOR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRA_CI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRA_FR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRA_MO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_FRG_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GAM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GEO_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GER_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GGI_AN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GGI_BI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GGI_PR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GGI_ST ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GHA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GIL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GNB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GNL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GRB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GRC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GST_BA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GST_QA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GST_UA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_GUY_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAI_HA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAI_NI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAW_HI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAW_JI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HAW_MI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HBI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HMD_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HON_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_HUN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ICE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IDA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ILL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_AP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_BI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_CH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_CT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_DD ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_DE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_DI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_DM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_GO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_GU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_HA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_JK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_KE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_KL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_KT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_MH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_MP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_MR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_OR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_PO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_PU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_RA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_TN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_UP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_WB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IND_YA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_INI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IOW_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRE_IR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRE_NI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRK_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IRQ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ITA_IT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ITA_SM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ITA_VC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_IVO_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAP_HK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAP_HN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAP_KY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAP_SH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JAW_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_JNF_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KAZ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KEG_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KEN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KER_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KGZ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KHA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KOR_NK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KOR_SK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KRY_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KTY_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KUR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KUW_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_KZN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LAB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LAO_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBS_LB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBS_SY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LBY_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LDV_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_AB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_AG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_AV ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_BV ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_GU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_MO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_NL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_SK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_SM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LEE_VI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LES_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LIN_KI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LIN_US ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LOU_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LSI_BA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LSI_ET ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_LSI_LS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAG_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAQ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MAU_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MCI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MCS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDG_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MDV_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MIC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MIN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLW_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLY_PM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MLY_SI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MNT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MON_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOR_MO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOR_SP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MOZ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MPE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRN_GU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRN_NM ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRQ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MRY_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MSI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MSO_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MTN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC_DF ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC_ME ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC_MO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC_PU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXC_TL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_AG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_CO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_CU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_DU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_GU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_HI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_NL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_QU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_SL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_TA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXE_ZA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXG_VC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXI_GU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXI_RA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXI_RG ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXN_BC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXN_BS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXN_SI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXN_SO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_CL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_GR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_JA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_MI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_NA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXS_OA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT_CA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT_CI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT_QR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT_TB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MXT_YU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_MYA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NAM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NAT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NBR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_CH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_DA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_IN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_KB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_KC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_KR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_SO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NCS_ST ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NDA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEP_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NET_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NEV_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFK_LH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFK_NI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFL_NE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NFL_SP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NGA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NGR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NIC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NLA_BO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NLA_CU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NNS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NOR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NRU_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NSC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NSW_CT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NSW_NS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NTA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NUE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NUN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWG_IJ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWG_PN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWH_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWJ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NWY_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NZN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_NZS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OFS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OGA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OHI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OKL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_OMA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ONT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ORE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAK_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAL_IS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAL_JO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PAR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PEI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PEN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PER_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PHI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PHX_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PIT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_POL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_POR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PRM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_PUE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_QLD_CS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_QLD_QU ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_QUE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_REU_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RHO_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ROD_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ROM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RUW_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_RWA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAK_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAM_AS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAM_WS ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SAU_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCS_PI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCS_SI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SCZ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SDA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SEY_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SGE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIC_MA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIC_SI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SIN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOL_NO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOL_SO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SOM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SPA_AN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SPA_GI ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SPA_SP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SRL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SSA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_STH_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUD_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SUR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SVA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWC_CC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWC_HC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWC_NC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_SWZ_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TAS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_AB ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_AD ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_AR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_AZ ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_GR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_NA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TCS_NK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TDC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TEN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TEX_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_THA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TKM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOG_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOK_MA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOK_SW ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TOK_TO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TON_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TRT_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUR_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TUV_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVL_GA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVL_MP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVL_NP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TVL_NW ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_TZK_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UGA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UKR_MO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UKR_UK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_URU_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UTA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_UZB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VAN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VEN_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VER_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VIC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VIE_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VNA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_VRG_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAK_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAL_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAU_AC ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WAU_WA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WDC_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WHM_HP ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WHM_JK ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WHM_UT ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_BA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_DO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_GR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_MA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_SL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIN_SV ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WIS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WSA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WSB_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WVA_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_WYO_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_XMS_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YAK_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YEM_NY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YEM_SY ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_BH ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_CR ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_KO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_MA ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_MN ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_SE ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUG_SL ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_YUK_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZAI_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZAM_OO ())) { return AREA_ALBANIA; }
			else if (tdwgArea.equals(TdwgArea.ABBREV_ZIM_OO ())) { return AREA_ALBANIA; }
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
