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

import eu.etaxonomy.cdm.io.erms.ErmsTransformer;
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
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
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
	
	// Animalia Rank Abbreviations only for used Ranks
	public static String Animalia_Abbrev_Subgenus = "subg.";
	public static String Animalia_Abbrev_Species = "sp.";
	public static String Animalia_Abbrev_Subspecies = "subsp.";
	public static String Animalia_Abbrev_Variety = "var.";
	public static String Animalia_Abbrev_Subvariety = "subvar.";
	public static String Animalia_Abbrev_Forma = "f.";

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
	
	// Plantae Rank Abbreviations
	public static String Plantae_Abbrev_Kingdom = "reg.";
	public static String Plantae_Abbrev_Subkingdom = "subreg.";
	public static String Plantae_Abbrev_Division = "div.";
	public static String Plantae_Abbrev_Subdivision = "subdiv.";
	public static String Plantae_Abbrev_Class = "cl.";
	public static String Plantae_Abbrev_Subclass = "subcl.";
	public static String Plantae_Abbrev_Order = "ordo";
	public static String Plantae_Abbrev_Suborder = "subor.";
	public static String Plantae_Abbrev_Family = "fam.";
	public static String Plantae_Abbrev_Subfamily = "subfam.";
	public static String Plantae_Abbrev_Tribe	= "trib.";
	public static String Plantae_Abbrev_Subtribe = "subtrib.";
	public static String Plantae_Abbrev_Genus = "gen.";
	public static String Plantae_Abbrev_Subgenus = "subg.";
	public static String Plantae_Abbrev_Section = "sect.";
	public static String Plantae_Abbrev_Subsection = "subsect.";
	public static String Plantae_Abbrev_Series = "ser.";
	public static String Plantae_Abbrev_Subseries	= "subser.";
	public static String Plantae_Abbrev_Aggregate	= "aggr.";
	public static String Plantae_Abbrev_Coll_Species = "coll. sp.";
	public static String Plantae_Abbrev_Species = "sp.";
	public static String Plantae_Abbrev_Subspecies = "subsp.";
	public static String Plantae_Abbrev_Proles = "prol.";
	public static String Plantae_Abbrev_Race = "race";
	public static String Plantae_Abbrev_Convarietas = "convar.";
	public static String Plantae_Abbrev_Variety = "var.";
	public static String Plantae_Abbrev_Subvariety = "subvar.";
	public static String Plantae_Abbrev_Forma	= "f.";
	public static String Plantae_Abbrev_Subforma = "subf.";
	public static String Plantae_Abbrev_Forma_spec = "f.spec.";
	public static String Plantae_Abbrev_Taxa_infragen = "t.infgen.";
	public static String Plantae_Abbrev_Taxa_infraspec = "t.infr.";
	
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

	public static String Bacteria_STR_Kingdom = "Kingdom";
	public static String Bacteria_STR_Subkingdom = "Subkingdom";
	public static String Bacteria_STR_Phylum = "Phylum";
	public static String Bacteria_STR_Subphylum = "Subphylum";
	public static String Bacteria_STR_Superclass = "Superclass";
	public static String Bacteria_STR_Class = "Class";
	public static String Bacteria_STR_Subclass = "Subclass";
	public static String Bacteria_STR_Infraclass = "Infraclass";
	public static String Bacteria_STR_Superorder = "Superorder";
	public static String Bacteria_STR_Order = "Order";
	public static String Bacteria_STR_Suborder = "Suborder";
	public static String Bacteria_STR_Infraorder = "Infraorder";
	public static String Bacteria_STR_Superfamily = "Superfamily";
	public static String Bacteria_STR_Family = "Family";
	public static String Bacteria_STR_Subfamily = "Subfamily";
	public static String Bacteria_STR_Tribe = "Tribe";
	public static String Bacteria_STR_Subtribe = "Subtribe";
	public static String Bacteria_STR_Genus = "Genus";
	public static String Bacteria_STR_Subgenus = "Subgenus";
	public static String Bacteria_STR_Species = "Species";
	public static String Bacteria_STR_Subspecies = "Subspecies";
	public static String Bacteria_STR_Variety = "Variety";
	public static String Bacteria_STR_Forma = "Forma";

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


	public static String STR_AREA_EAST_AEGEAN_ISLANDS = "East Aegean Islands";
	public static String STR_AREA_GREEK_EAST_AEGEAN_ISLANDS = "Greek East Aegean Islands";
	public static String STR_AREA_TURKISH_EAST_AEGEAN_ISLANDS = "Turkish East Aegean Islands";
	public static String STR_AREA_ALBANIA = "Albania";
	public static String STR_AREA_AUSTRIA_WITH_LIECHTENSTEIN = "Austria with Liechtenstein";
	public static String STR_AREA_AUSTRIA = "Austria";
	public static String STR_AREA_LIECHTENSTEIN = "Liechtenstein";
	public static String STR_AREA_AZORES = "Azores";
	public static String STR_AREA_CORVO = "Corvo";
	public static String STR_AREA_FAIAL = "Faial";
	public static String STR_AREA_GRACIOSA = "Graciosa";
	public static String STR_AREA_SAO_JORGE = "São Jorge";
	public static String STR_AREA_FLORES = "Flores";
	public static String STR_AREA_SAO_MIGUEL = "São Miguel";
	public static String STR_AREA_PICO = "Pico";
	public static String STR_AREA_SANTA_MARIA = "Santa Maria";
	public static String STR_AREA_TERCEIRA = "Terceira";
	public static String STR_AREA_BELGIUM_WITH_LUXEMBOURG = "Belgium with Luxembourg";
	public static String STR_AREA_BELGIUM = "Belgium";
	public static String STR_AREA_LUXEMBOURG = "Luxembourg";
	public static String STR_AREA_BOSNIA_HERZEGOVINA = "Bosnia-Herzegovina";
	public static String STR_AREA_BALEARES = "Baleares";
	public static String STR_AREA_IBIZA_WITH_FORMENTERA = "Ibiza with Formentera";
	public static String STR_AREA_MALLORCA = "Mallorca";
	public static String STR_AREA_MENORCA = "Menorca";
	public static String STR_AREA_GREAT_BRITAIN = "Great Britain";
	public static String STR_AREA_BALTIC_STATES_ESTONIA_LATVIA_LITHUANIA_AND_KALININGRAD_REGION = "Baltic states (Estonia, Latvia, Lithuania) and Kaliningrad region";
	public static String STR_AREA_BULGARIA = "Bulgaria";
	public static String STR_AREA_BELARUS = "Belarus";
	public static String STR_AREA_CANARY_ISLANDS = "Canary Islands";
	public static String STR_AREA_GRAN_CANARIA = "Gran Canaria";
	public static String STR_AREA_FUERTEVENTURA_WITH_LOBOS = "Fuerteventura with Lobos";
	public static String STR_AREA_GOMERA = "Gomera";
	public static String STR_AREA_HIERRO = "Hierro";
	public static String STR_AREA_LANZAROTE_WITH_GRACIOSA = "Lanzarote with Graciosa";
	public static String STR_AREA_LA_PALMA = "La Palma";
	public static String STR_AREA_TENERIFE = "Tenerife";
	public static String STR_AREA_MONTENEGRO = "Montenegro";
	public static String STR_AREA_CORSE = "Corse";
	public static String STR_AREA_CRETE_WITH_KARPATHOS_KASOS_AND_GAVDHOS = "Crete with Karpathos, Kasos & Gavdhos";
	public static String STR_AREA_CZECH_REPUBLIC = "Czech Republic";
	public static String STR_AREA_CROATIA = "Croatia";
	public static String STR_AREA_CYPRUS = "Cyprus";
	public static String STR_AREA_FORMER_CZECHOSLOVAKIA = "Former Czechoslovakia";
	public static String STR_AREA_DENMARK_WITH_BORNHOLM = "Denmark with Bornholm";
	public static String STR_AREA_ESTONIA = "Estonia";
	public static String STR_AREA_FAROE_ISLANDS = "Faroe Islands";
	public static String STR_AREA_FINLAND_WITH_AHVENANMAA = "Finland with Ahvenanmaa";
	public static String STR_AREA_FRANCE = "France";
	public static String STR_AREA_CHANNEL_ISLANDS = "Channel Islands";
	public static String STR_AREA_FRENCH_MAINLAND = "French mainland";
	public static String STR_AREA_MONACO = "Monaco";
	public static String STR_AREA_GERMANY = "Germany";
	public static String STR_AREA_GREECE_WITH_CYCLADES_AND_MORE_ISLANDS = "Greece with Cyclades and more islands";
	public static String STR_AREA_IRELAND = "Ireland";
	public static String STR_AREA_REPUBLIC_OF_IRELAND = "Republic of Ireland";
	public static String STR_AREA_NORTHERN_IRELAND = "Northern Ireland";
	public static String STR_AREA_SWITZERLAND = "Switzerland";
	public static String STR_AREA_NETHERLANDS = "Netherlands";
	public static String STR_AREA_SPAIN = "Spain";
	public static String STR_AREA_ANDORRA = "Andorra";
	public static String STR_AREA_GIBRALTAR = "Gibraltar";
	public static String STR_AREA_KINGDOM_OF_SPAIN = "Kingdom of Spain";
	public static String STR_AREA_HUNGARY = "Hungary";
	public static String STR_AREA_ICELAND = "Iceland";
	public static String STR_AREA_ITALY = "Italy";
	public static String STR_AREA_ITALIAN_MAINLAND = "Italian mainland";
	public static String STR_AREA_SAN_MARINO = "San Marino";
	public static String STR_AREA_FORMER_JUGOSLAVIA = "Former Jugoslavia";
	public static String STR_AREA_LATVIA = "Latvia";
	public static String STR_AREA_LITHUANIA = "Lithuania";
	public static String STR_AREA_PORTUGUESE_MAINLAND = "Portuguese mainland";
	public static String STR_AREA_MADEIRA_ARCHIPELAGO = "Madeira";
	public static String STR_AREA_DESERTAS = "Desertas";
	public static String STR_AREA_MADEIRA = "Madeira";
	public static String STR_AREA_PORTO_SANTO = "Porto Santo";
	public static String STR_AREA_THE_FORMER_JUGOSLAV_REPUBLIC_OF_MAKEDONIJA = "The former Jugoslav Republic of Makedonija";
	public static String STR_AREA_MOLDOVA = "Moldova";
	public static String STR_AREA_NORWEGIAN_MAINLAND = "Norwegian mainland";
	public static String STR_AREA_POLAND = "Poland";
	public static String STR_AREA_THE_RUSSIAN_FEDERATION = "The Russian Federation";
	public static String STR_AREA_NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND = "Novaya Zemlya & Franz-Joseph Land";
	public static String STR_AREA_CENTRAL_EUROPEAN_RUSSIA = "Central European Russia";
	public static String STR_AREA_EASTERN_EUROPEAN_RUSSIA = "Eastern European Russia";
	public static String STR_AREA_KALININGRAD = "Kaliningrad";
	public static String STR_AREA_NORTHERN_EUROPEAN_RUSSIA = "Northern European Russia";
	public static String STR_AREA_NORTHWEST_EUROPEAN_RUSSIA = "Northwest European Russia";
	public static String STR_AREA_SOUTH_EUROPEAN_RUSSIA = "South European Russia";
	public static String STR_AREA_ROMANIA = "Romania";
	public static String STR_AREA_FORMER_USSR = "Former USSR";
	public static String STR_AREA_RUSSIA_BALTIC = "Russia Baltic";
	public static String STR_AREA_RUSSIA_CENTRAL = "Russia Central";
	public static String STR_AREA_RUSSIA_SOUTHEAST = "Russia Southeast";
	public static String STR_AREA_RUSSIA_NORTHERN = "Russia Northern";
	public static String STR_AREA_RUSSIA_SOUTHWEST = "Russia Southwest";
	public static String STR_AREA_SARDEGNA = "Sardegna";
	public static String STR_AREA_SVALBARD_WITH_BJORNOYA_AND_JAN_MAYEN = "Svalbard with Björnöya and Jan Mayen";
	public static String STR_AREA_SELVAGENS_ISLANDS = "Selvagens Islands";
	public static String STR_AREA_SICILY_WITH_MALTA = "Sicily with Malta";
	public static String STR_AREA_MALTA = "Malta";
	public static String STR_AREA_SICILY = "Sicily";
	public static String STR_AREA_SLOVAKIA = "Slovakia";
	public static String STR_AREA_SLOVENIA = "Slovenia";
	public static String STR_AREA_SERBIA_WITH_MONTENEGRO = "Serbia with Montenegro";
	public static String STR_AREA_SERBIA_INCLUDING_VOJVODINA_AND_WITH_KOSOVO = "Serbia including Vojvodina and with Kosovo";
	public static String STR_AREA_SWEDEN = "Sweden";
	public static String STR_AREA_EUROPEAN_TURKEY = "European Turkey";
	public static String STR_AREA_UKRAINE_INCLUDING_CRIMEA = "Ukraine including Crimea";
	public static String STR_AREA_CRIMEA = "Crimea";
	public static String STR_AREA_UKRAINE = "Ukraine";
	public static String STR_AREA_GREEK_MAINLAND = "Greek mainland";
	public static String STR_AREA_CRETE = "Crete";
	public static String STR_AREA_DODECANESE_ISLANDS = "Dodecanese Islands";
	public static String STR_AREA_CYCLADES_ISLANDS = "Cyclades Islands";
	public static String STR_AREA_NORTH_AEGEAN_ISLANDS = "North Aegean Islands";
	public static String STR_AREA_VATICAN_CITY = "Vatican City";
	public static String STR_AREA_FRANZ_JOSEF_LAND = "Franz Josef Land";
	public static String STR_AREA_NOVAYA_ZEMLYA = "Novaya Zemlya";
	public static String STR_AREA_AZERBAIJAN_INCLUDING_NAKHICHEVAN = "Azerbaijan including Nakhichevan";
	public static String STR_AREA_AZERBAIJAN = "Azerbaijan";
	public static String STR_AREA_NAKHICHEVAN = "Nakhichevan";
	public static String STR_AREA_ALGERIA = "Algeria";
	public static String STR_AREA_ARMENIA = "Armenia";
	public static String STR_AREA_CAUCASUS_REGION = "Caucasus region";
	public static String STR_AREA_EGYPT = "Egypt";
	public static String STR_AREA_GEORGIA = "Georgia";
	public static String STR_AREA_ISRAEL_JORDAN = "Israel-Jordan";
	public static String STR_AREA_ISRAEL = "Israel";
	public static String STR_AREA_JORDAN = "Jordan";
	public static String STR_AREA_LEBANON = "Lebanon";
	public static String STR_AREA_LIBYA = "Libya";
	public static String STR_AREA_LEBANON_SYRIA = "Lebanon-Syria";
	public static String STR_AREA_MOROCCO = "Morocco";
	public static String STR_AREA_NORTH_CAUCASUS = "North Caucasus";
	public static String STR_AREA_SINAI = "Sinai";
	public static String STR_AREA_SYRIA = "Syria";
	public static String STR_AREA_TUNISIA = "Tunisia";
	public static String STR_AREA_ASIATIC_TURKEY = "Asiatic Turkey";
	public static String STR_AREA_TURKEY = "Turkey";
	public static String STR_AREA_NORTHERN_AFRICA = "Northern Africa";
	public static String STR_AREA_AFRO_TROPICAL_REGION = "Afro-tropical region";
	public static String STR_AREA_AUSTRALIAN_REGION = "Australian region";
	public static String STR_AREA_EAST_PALAEARCTIC = "East Palaearctic";
	public static String STR_AREA_NEARCTIC_REGION = "Nearctic region";
	public static String STR_AREA_NEOTROPICAL_REGION = "Neotropical region";
	public static String STR_AREA_NEAR_EAST = "Near East";
	public static String STR_AREA_ORIENTAL_REGION = "Oriental region";
	public static String STR_AREA_EUROPEAN_MARINE_WATERS = "European Marine Waters";
	public static String STR_AREA_MEDITERRANEAN_SEA = "Mediterranean Sea";
	public static String STR_AREA_WHITE_SEA = "White Sea";
	public static String STR_AREA_NORTH_SEA = "North Sea";
	public static String STR_AREA_BALTIC_SEA = "Baltic Sea";
	public static String STR_AREA_BLACK_SEA = "Black Sea";
	public static String STR_AREA_BARENTS_SEA = "Barents Sea";
	public static String STR_AREA_CASPIAN_SEA = "Caspian Sea";
	public static String STR_AREA_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE = "Portuguese Exclusive Economic Zone";
	public static String STR_AREA_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE = "Belgian Exclusive Economic Zone";
	public static String STR_AREA_FRENCH_EXCLUSIVE_ECONOMIC_ZONE = "French Exclusive Economic Zone";
	public static String STR_AREA_ENGLISH_CHANNEL = "English Channel";
	public static String STR_AREA_ADRIATIC_SEA = "Adriatic Sea";
	public static String STR_AREA_BISCAY_BAY = "Biscay Bay";
	public static String STR_AREA_DUTCH_EXCLUSIVE_ECONOMIC_ZONE = "Dutch Exclusive Economic Zone";
	public static String STR_AREA_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE = "United Kingdom Exclusive Economic Zone";
	public static String STR_AREA_SPANISH_EXCLUSIVE_ECONOMIC_ZONE = "Spanish Exclusive Economic Zone";
	public static String STR_AREA_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE = "Egyptian Exclusive Economic Zone";
	public static String STR_AREA_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE = "Grecian Exclusive Economic Zone";
	public static String STR_AREA_TIRRENO_SEA = "Tirreno Sea";
	public static String STR_AREA_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE = "Icelandic Exclusive Economic Zone";
	public static String STR_AREA_IRISH_EXCLUSIVE_ECONOMIC_ZONE = "Irish Exclusive economic Zone";
	public static String STR_AREA_IRISH_SEA = "Irish Sea";
	public static String STR_AREA_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE = "Italian Exclusive Economic Zone";
	public static String STR_AREA_NORWEGIAN_SEA = "Norwegian Sea";
	public static String STR_AREA_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE = "Moroccan Exclusive Economic Zone";
	public static String STR_AREA_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE = "Norwegian Exclusive Economic Zone";
	public static String STR_AREA_SKAGERRAK = "Skagerrak";
	public static String STR_AREA_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE = "Tunisian Exclusive Economic Zone";
	public static String STR_AREA_WADDEN_SEA = "Wadden Sea";
	public static String STR_AREA_BELT_SEA = "Belt Sea";
	public static String STR_AREA_MARMARA_SEA = "Marmara Sea";
	public static String STR_AREA_SEA_OF_AZOV = "Sea of Azov";
	public static String STR_AREA_AEGEAN_SEA = "Aegean Sea";
	public static String STR_AREA_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE = "Bulgarian Exclusive Economic Zone";
	public static String STR_AREA_SOUTH_BALTIC_PROPER = "South Baltic proper";
	public static String STR_AREA_BALTIC_PROPER = "Baltic Proper";
	public static String STR_AREA_NORTH_BALTIC_PROPER = "North Baltic proper";
	public static String STR_AREA_ARCHIPELAGO_SEA = "Archipelago Sea";
	public static String STR_AREA_BOTHNIAN_SEA = "Bothnian Sea";
	public static String STR_AREA_GERMAN_EXCLUSIVE_ECONOMIC_ZONE = "German Exclusive Economic Zone";
	public static String STR_AREA_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE = "Swedish Exclusive Economic Zone";
	public static String STR_AREA_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE = "Ukrainian Exclusive Economic Zone";
	public static String STR_AREA_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE = "Madeiran Exclusive Economic Zone";
	public static String STR_AREA_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE = "Lebanese Exclusive Economic Zone";
	public static String STR_AREA_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART = "Spanish Exclusive Economic Zone [Mediterranean part]";
	public static String STR_AREA_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE = "Estonian Exclusive Economic Zone";
	public static String STR_AREA_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE = "Croatian Exclusive Economic Zone";
	public static String STR_AREA_BALEAR_SEA = "Balear Sea";
	public static String STR_AREA_TURKISH_EXCLUSIVE_ECONOMIC_ZONE = "Turkish Exclusive Economic Zone";
	public static String STR_AREA_DANISH_EXCLUSIVE_ECONOMIC_ZONE = "Danish Exclusive Economic Zone";


	
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
	 * Returns the OccurrenceStatusCache for a given PresenceAbsenceTerm.
	 * @param term
	 * @return
	 * @throws UnknownCdmTypeException 
	 */
	public static String presenceAbsenceTerm2OccurrenceStatusCache(PresenceAbsenceTermBase<?> term) {
		String result = null;
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
			} else if (presenceTerm.equals(PresenceTerm.INTRODUCED_CULTIVATED())) {
				result = STR_STATUS_MANAGED;
			} else if (presenceTerm.equals(PresenceTerm.PRESENT_DOUBTFULLY())) {
				result = STR_STATUS_DOUBTFUL;
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
			} else if (presenceTerm.equals(PresenceTerm.CULTIVATED())) {
				result = STATUS_MANAGED;
			} else if (presenceTerm.equals(PresenceTerm.PRESENT_DOUBTFULLY())) {
				result = STATUS_DOUBTFUL;
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
			NamedArea namedArea = CdmBase.deproxy(area, NamedArea.class);

			// TODO: Areas identified by the string "TODO" (for now) have to be identified correctly after additions have been made to the list of NamedArea's according to specific imports, i.e. euro+med
			if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_EAST_AEGEAN_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("EAI")) { return STR_AREA_GREEK_EAST_AEGEAN_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("EAI-OO")) { return STR_AREA_GREEK_EAST_AEGEAN_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_TURKISH_EAST_AEGEAN_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ALB")) { return STR_AREA_ALBANIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ALB-OO")) { return STR_AREA_ALBANIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AUT")) { return STR_AREA_AUSTRIA_WITH_LIECHTENSTEIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AUT-AU")) { return STR_AREA_AUSTRIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AUT-LI")) { return STR_AREA_LIECHTENSTEIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AZO")) { return STR_AREA_AZORES; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AZO-OO")) { return STR_AREA_AZORES; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_CORVO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_FAIAL; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_GRACIOSA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_SAO_JORGE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_FLORES; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_SAO_MIGUEL; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_PICO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_SANTA_MARIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_TERCEIRA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BGM")) { return STR_AREA_BELGIUM_WITH_LUXEMBOURG; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BGM-BE")) { return STR_AREA_BELGIUM; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BGM-LU")) { return STR_AREA_LUXEMBOURG; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-BH")) { return STR_AREA_BOSNIA_HERZEGOVINA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BAL")) { return STR_AREA_BALEARES; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BAL-OO")) { return STR_AREA_BALEARES; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_IBIZA_WITH_FORMENTERA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_MALLORCA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_MENORCA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GRB")) { return STR_AREA_GREAT_BRITAIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GRB-OO")) { return STR_AREA_GREAT_BRITAIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLT")) { return STR_AREA_BALTIC_STATES_ESTONIA_LATVIA_LITHUANIA_AND_KALININGRAD_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BUL")) { return STR_AREA_BULGARIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BUL-OO")) { return STR_AREA_BULGARIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLR")) { return STR_AREA_BELARUS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLR-OO")) { return STR_AREA_BELARUS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CNY")) { return STR_AREA_CANARY_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CNY-OO")) { return STR_AREA_CANARY_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_GRAN_CANARIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_FUERTEVENTURA_WITH_LOBOS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_GOMERA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_HIERRO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_LANZAROTE_WITH_GRACIOSA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_LA_PALMA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_TENERIFE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-MN")) { return STR_AREA_MONTENEGRO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("COR")) { return STR_AREA_CORSE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("COR-OO")) { return STR_AREA_CORSE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("KRI")) { return STR_AREA_CRETE_WITH_KARPATHOS_KASOS_AND_GAVDHOS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("KRI-OO")) { return STR_AREA_CRETE_WITH_KARPATHOS_KASOS_AND_GAVDHOS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CZE-CZ")) { return STR_AREA_CZECH_REPUBLIC; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-CR")) { return STR_AREA_CROATIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CYP")) { return STR_AREA_CYPRUS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CYP-OO")) { return STR_AREA_CYPRUS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CZE")) { return STR_AREA_FORMER_CZECHOSLOVAKIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("DEN")) { return STR_AREA_DENMARK_WITH_BORNHOLM; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("DEN-OO")) { return STR_AREA_DENMARK_WITH_BORNHOLM; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLT-ES")) { return STR_AREA_ESTONIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FOR")) { return STR_AREA_FAROE_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FOR-OO")) { return STR_AREA_FAROE_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FIN")) { return STR_AREA_FINLAND_WITH_AHVENANMAA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FIN-OO")) { return STR_AREA_FINLAND_WITH_AHVENANMAA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FRA")) { return STR_AREA_FRANCE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FRA-CI")) { return STR_AREA_CHANNEL_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FRA-FR")) { return STR_AREA_FRENCH_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FRA-MO")) { return STR_AREA_MONACO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GER")) { return STR_AREA_GERMANY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GER-OO")) { return STR_AREA_GERMANY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GRC")) { return STR_AREA_GREECE_WITH_CYCLADES_AND_MORE_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GRC-OO")) { return STR_AREA_GREECE_WITH_CYCLADES_AND_MORE_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("IRE")) { return STR_AREA_IRELAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("IRE-IR")) { return STR_AREA_REPUBLIC_OF_IRELAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("IRE-NI")) { return STR_AREA_NORTHERN_IRELAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SWI")) { return STR_AREA_SWITZERLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SWI-OO")) { return STR_AREA_SWITZERLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NET")) { return STR_AREA_NETHERLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NET-OO")) { return STR_AREA_NETHERLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SPA")) { return STR_AREA_SPAIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SPA-AN")) { return STR_AREA_ANDORRA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SPA-GI")) { return STR_AREA_GIBRALTAR; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SPA-SP")) { return STR_AREA_KINGDOM_OF_SPAIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("HUN")) { return STR_AREA_HUNGARY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("HUN-OO")) { return STR_AREA_HUNGARY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ICE")) { return STR_AREA_ICELAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ICE-OO")) { return STR_AREA_ICELAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ITA")) { return STR_AREA_ITALY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ITA-IT")) { return STR_AREA_ITALIAN_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ITA-SM")) { return STR_AREA_SAN_MARINO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG")) { return STR_AREA_FORMER_JUGOSLAVIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLT-LA")) { return STR_AREA_LATVIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLT-LI")) { return STR_AREA_LITHUANIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("POR")) { return STR_AREA_PORTUGUESE_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("POR-OO")) { return STR_AREA_PORTUGUESE_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("MDR")) { return STR_AREA_MADEIRA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("MDR-OO")) { return STR_AREA_MADEIRA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_DESERTAS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_PORTO_SANTO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-MA")) { return STR_AREA_THE_FORMER_JUGOSLAV_REPUBLIC_OF_MAKEDONIJA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("UKR-MO")) { return STR_AREA_MOLDOVA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NOR")) { return STR_AREA_NORWEGIAN_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NOR-OO")) { return STR_AREA_NORWEGIAN_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("POL")) { return STR_AREA_POLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("POL-OO")) { return STR_AREA_POLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUS")) { return STR_AREA_THE_RUSSIAN_FEDERATION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("WSB")) { return STR_AREA_NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("WSB-OO")) { return STR_AREA_NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUC")) { return STR_AREA_CENTRAL_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUC-OO")) { return STR_AREA_CENTRAL_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUE")) { return STR_AREA_EASTERN_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUE-OO")) { return STR_AREA_EASTERN_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLT-KA")) { return STR_AREA_KALININGRAD; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUN")) { return STR_AREA_NORTHERN_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUN-OO")) { return STR_AREA_NORTHERN_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUW")) { return STR_AREA_NORTHWEST_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUW-OO")) { return STR_AREA_NORTHWEST_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUS")) { return STR_AREA_SOUTH_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUS-OO")) { return STR_AREA_SOUTH_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ROM")) { return STR_AREA_ROMANIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ROM-OO")) { return STR_AREA_ROMANIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_FORMER_USSR; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_RUSSIA_BALTIC; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUC")) { return STR_AREA_RUSSIA_CENTRAL; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_RUSSIA_SOUTHEAST; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUN")) { return STR_AREA_RUSSIA_NORTHERN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_RUSSIA_SOUTHWEST; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SAR")) { return STR_AREA_SARDEGNA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SAR-OO")) { return STR_AREA_SARDEGNA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SVA")) { return STR_AREA_SVALBARD_WITH_BJORNOYA_AND_JAN_MAYEN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SVA-OO")) { return STR_AREA_SVALBARD_WITH_BJORNOYA_AND_JAN_MAYEN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SEL")) { return STR_AREA_SELVAGENS_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SEL-OO")) { return STR_AREA_SELVAGENS_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SIC")) { return STR_AREA_SICILY_WITH_MALTA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SIC-MA")) { return STR_AREA_MALTA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SIC-SI")) { return STR_AREA_SICILY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CZE-SK")) { return STR_AREA_SLOVAKIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-SL")) { return STR_AREA_SLOVENIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CS")) { return STR_AREA_SERBIA_WITH_MONTENEGRO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-SE")) { return STR_AREA_SERBIA_INCLUDING_VOJVODINA_AND_WITH_KOSOVO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SWE")) { return STR_AREA_SWEDEN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SWE-OO")) { return STR_AREA_SWEDEN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUE")) { return STR_AREA_EUROPEAN_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUE-OO")) { return STR_AREA_EUROPEAN_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_UKRAINE_INCLUDING_CRIMEA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("KRY")) { return STR_AREA_CRIMEA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("KRY-OO")) { return STR_AREA_CRIMEA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("UKR-UK")) { return STR_AREA_UKRAINE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GRE")) { return STR_AREA_GREEK_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GR-KRI")) { return STR_AREA_CRETE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GR-DOD")) { return STR_AREA_DODECANESE_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GR-CYC")) { return STR_AREA_CYCLADES_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GR-AEG")) { return STR_AREA_NORTH_AEGEAN_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ITA-VC")) { return STR_AREA_VATICAN_CITY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_FRANZ_JOSEF_LAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_NOVAYA_ZEMLYA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AZ")) { return STR_AREA_AZERBAIJAN_INCLUDING_NAKHICHEVAN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TCS-AZ")) { return STR_AREA_AZERBAIJAN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TCS-NA")) { return STR_AREA_NAKHICHEVAN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ALG")) { return STR_AREA_ALGERIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ALG-OO")) { return STR_AREA_ALGERIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TCS-AR")) { return STR_AREA_ARMENIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("33")) { return STR_AREA_CAUCASUS_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("EGY")) { return STR_AREA_EGYPT; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("EGY-OO")) { return STR_AREA_EGYPT; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_GEORGIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("PAL")) { return STR_AREA_ISRAEL_JORDAN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("PAL-IS")) { return STR_AREA_ISRAEL; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("PAL-JO")) { return STR_AREA_JORDAN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("LBS-LB")) { return STR_AREA_LEBANON; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("LBY-OO")) { return STR_AREA_LIBYA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("LBS")) { return STR_AREA_LEBANON_SYRIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("MOR")) { return STR_AREA_MOROCCO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NCS")) { return STR_AREA_NORTH_CAUCASUS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SIN")) { return STR_AREA_SINAI; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SIN-OO")) { return STR_AREA_SINAI; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("LBS-SY")) { return STR_AREA_SYRIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUN")) { return STR_AREA_TUNISIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUN-OO")) { return STR_AREA_TUNISIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_ASIATIC_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_ASIATIC_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUR")) { return STR_AREA_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUR-OO")) { return STR_AREA_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("20")) { return STR_AREA_NORTHERN_AFRICA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AFR")) { return STR_AREA_AFRO_TROPICAL_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AUS")) { return STR_AREA_AUSTRALIAN_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return STR_AREA_EAST_PALAEARCTIC; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NEA")) { return STR_AREA_NEARCTIC_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NEO")) { return STR_AREA_NEOTROPICAL_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NRE")) { return STR_AREA_NEAR_EAST; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ORR")) { return STR_AREA_ORIENTAL_REGION; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidEuropeanMarineWaters)) { return STR_AREA_EUROPEAN_MARINE_WATERS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("MES") ||
					(namedArea.getUuid().equals(ErmsTransformer.uuidMediterraneanSea))) { return STR_AREA_MEDITERRANEAN_SEA; } // abbreviated label missing
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidWhiteSea)) { return STR_AREA_WHITE_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidNorthSea)) { return STR_AREA_NORTH_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBalticSea)) { return STR_AREA_BALTIC_SEA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLS") ||
					(namedArea.getUuid().equals(ErmsTransformer.uuidBlackSea))) { return STR_AREA_BLACK_SEA; } // abbreviated label missing
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBarentsSea)) { return STR_AREA_BARENTS_SEA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CAS") ||
					(namedArea.getUuid().equals(ErmsTransformer.uuidCaspianSea))) { return STR_AREA_CASPIAN_SEA; } // abbreviated label missing
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidPortugueseExclusiveEconomicZone)) { return STR_AREA_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBelgianExclusiveEconomicZone)) { return STR_AREA_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidFrenchExclusiveEconomicZone)) { return STR_AREA_FRENCH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidEnglishChannel)) { return STR_AREA_ENGLISH_CHANNEL; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidAdriaticSea)) { return STR_AREA_ADRIATIC_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBiscayBay)) { return STR_AREA_BISCAY_BAY; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidDutchExclusiveEconomicZone)) { return STR_AREA_DUTCH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidUnitedKingdomExclusiveEconomicZone)) { return STR_AREA_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSpanishExclusiveEconomicZone)) { return STR_AREA_SPANISH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidEgyptianExclusiveEconomicZone)) { return STR_AREA_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidGrecianExclusiveEconomicZone)) { return STR_AREA_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidTirrenoSea)) { return STR_AREA_TIRRENO_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidIcelandicExclusiveEconomicZone)) { return STR_AREA_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidIrishExclusiveeconomicZone)) { return STR_AREA_IRISH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidIrishSea)) { return STR_AREA_IRISH_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidItalianExclusiveEconomicZone)) { return STR_AREA_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidNorwegianSea)) { return STR_AREA_NORWEGIAN_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidMoroccanExclusiveEconomicZone)) { return STR_AREA_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidNorwegianExclusiveEconomicZone)) { return STR_AREA_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSkagerrak)) { return STR_AREA_SKAGERRAK; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidTunisianExclusiveEconomicZone)) { return STR_AREA_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidWaddenSea)) { return STR_AREA_WADDEN_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBeltSea)) { return STR_AREA_BELT_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidMarmaraSea)) { return STR_AREA_MARMARA_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSeaofAzov)) { return STR_AREA_SEA_OF_AZOV; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidAegeanSea)) { return STR_AREA_AEGEAN_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBulgarianExclusiveEconomicZone)) { return STR_AREA_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSouthBalticproper)) { return STR_AREA_SOUTH_BALTIC_PROPER; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBalticProper)) { return STR_AREA_BALTIC_PROPER; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidNorthBalticproper)) { return STR_AREA_NORTH_BALTIC_PROPER; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidArchipelagoSea)) { return STR_AREA_ARCHIPELAGO_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBothnianSea)) { return STR_AREA_BOTHNIAN_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidGermanExclusiveEconomicZone)) { return STR_AREA_GERMAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSwedishExclusiveEconomicZone)) { return STR_AREA_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidUkrainianExclusiveEconomicZone)) { return STR_AREA_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidMadeiranExclusiveEconomicZone)) { return STR_AREA_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidLebaneseExclusiveEconomicZone)) { return STR_AREA_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidSpanishExclusiveEconomicZoneMediterraneanpart)) { return STR_AREA_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidEstonianExclusiveEconomicZone)) { return STR_AREA_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidCroatianExclusiveEconomicZone)) { return STR_AREA_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidBalearSea)) { return STR_AREA_BALEAR_SEA; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidTurkishExclusiveEconomicZone)) { return STR_AREA_TURKISH_EXCLUSIVE_ECONOMIC_ZONE; }
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidDanishExclusiveEconomicZone)) { return STR_AREA_DANISH_EXCLUSIVE_ECONOMIC_ZONE; }
			else {
				logger.error("Unknown NamedArea Area: " + area.getTitleCache());
				return null; // Actually the export has to stop here because AreaFk's are not allowed to be NULL.
			}
		}
		return null; // Actually the export has to stop here because AreaFk's are not allowed to be NULL.
		
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
			NamedArea namedArea = CdmBase.deproxy(area, NamedArea.class);

			// TODO: Areas identified by the string "TODO" (for now) have to be identified correctly after additions have been made to the list of NamedArea's according to specific imports, i.e. euro+med
			if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_EAST_AEGEAN_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("EAI")) { return AREA_GREEK_EAST_AEGEAN_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("EAI-OO")) { return AREA_GREEK_EAST_AEGEAN_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_TURKISH_EAST_AEGEAN_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ALB")) { return AREA_ALBANIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ALB-OO")) { return AREA_ALBANIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AUT")) { return AREA_AUSTRIA_WITH_LIECHTENSTEIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AUT-AU")) { return AREA_AUSTRIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AUT-LI")) { return AREA_LIECHTENSTEIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AZO")) { return AREA_AZORES; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AZO-OO")) { return AREA_AZORES; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_CORVO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_FAIAL; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_GRACIOSA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_SAO_JORGE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_FLORES; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_SAO_MIGUEL; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_PICO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_SANTA_MARIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_TERCEIRA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BGM")) { return AREA_BELGIUM_WITH_LUXEMBOURG; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BGM-BE")) { return AREA_BELGIUM; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BGM-LU")) { return AREA_LUXEMBOURG; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-BH")) { return AREA_BOSNIA_HERZEGOVINA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BAL")) { return AREA_BALEARES; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BAL-OO")) { return AREA_BALEARES; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_IBIZA_WITH_FORMENTERA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_MALLORCA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_MENORCA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GRB")) { return AREA_GREAT_BRITAIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GRB-OO")) { return AREA_GREAT_BRITAIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLT")) { return AREA_BALTIC_STATES_ESTONIA_LATVIA_LITHUANIA_AND_KALININGRAD_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BUL")) { return AREA_BULGARIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BUL-OO")) { return AREA_BULGARIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLR")) { return AREA_BELARUS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLR-OO")) { return AREA_BELARUS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CNY")) { return AREA_CANARY_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CNY-OO")) { return AREA_CANARY_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_GRAN_CANARIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_FUERTEVENTURA_WITH_LOBOS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_GOMERA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_HIERRO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_LANZAROTE_WITH_GRACIOSA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_LA_PALMA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_TENERIFE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-MN")) { return AREA_MONTENEGRO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("COR")) { return AREA_CORSE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("COR-OO")) { return AREA_CORSE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("KRI")) { return AREA_CRETE_WITH_KARPATHOS_KASOS_AND_GAVDHOS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("KRI-OO")) { return AREA_CRETE_WITH_KARPATHOS_KASOS_AND_GAVDHOS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CZE-CZ")) { return AREA_CZECH_REPUBLIC; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-CR")) { return AREA_CROATIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CYP")) { return AREA_CYPRUS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CYP-OO")) { return AREA_CYPRUS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CZE")) { return AREA_FORMER_CZECHOSLOVAKIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("DEN")) { return AREA_DENMARK_WITH_BORNHOLM; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("DEN-OO")) { return AREA_DENMARK_WITH_BORNHOLM; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLT-ES")) { return AREA_ESTONIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FOR")) { return AREA_FAROE_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FOR-OO")) { return AREA_FAROE_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FIN")) { return AREA_FINLAND_WITH_AHVENANMAA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FIN-OO")) { return AREA_FINLAND_WITH_AHVENANMAA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FRA")) { return AREA_FRANCE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FRA-CI")) { return AREA_CHANNEL_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FRA-FR")) { return AREA_FRENCH_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("FRA-MO")) { return AREA_MONACO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GER")) { return AREA_GERMANY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GER-OO")) { return AREA_GERMANY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GRC")) { return AREA_GREECE_WITH_CYCLADES_AND_MORE_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GRC-OO")) { return AREA_GREECE_WITH_CYCLADES_AND_MORE_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("IRE")) { return AREA_IRELAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("IRE-IR")) { return AREA_REPUBLIC_OF_IRELAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("IRE-NI")) { return AREA_NORTHERN_IRELAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SWI")) { return AREA_SWITZERLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SWI-OO")) { return AREA_SWITZERLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NET")) { return AREA_NETHERLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NET-OO")) { return AREA_NETHERLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SPA")) { return AREA_SPAIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SPA-AN")) { return AREA_ANDORRA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SPA-GI")) { return AREA_GIBRALTAR; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SPA-SP")) { return AREA_KINGDOM_OF_SPAIN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("HUN")) { return AREA_HUNGARY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("HUN-OO")) { return AREA_HUNGARY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ICE")) { return AREA_ICELAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ICE-OO")) { return AREA_ICELAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ITA")) { return AREA_ITALY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ITA-IT")) { return AREA_ITALIAN_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ITA-SM")) { return AREA_SAN_MARINO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG")) { return AREA_FORMER_JUGOSLAVIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLT-LA")) { return AREA_LATVIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLT-LI")) { return AREA_LITHUANIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("POR")) { return AREA_PORTUGUESE_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("POR-OO")) { return AREA_PORTUGUESE_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("MDR")) { return AREA_MADEIRA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("MDR-OO")) { return AREA_MADEIRA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_DESERTAS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_PORTO_SANTO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-MA")) { return AREA_THE_FORMER_JUGOSLAV_REPUBLIC_OF_MAKEDONIJA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("UKR-MO")) { return AREA_MOLDOVA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NOR")) { return AREA_NORWEGIAN_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NOR-OO")) { return AREA_NORWEGIAN_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("POL")) { return AREA_POLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("POL-OO")) { return AREA_POLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUS")) { return AREA_THE_RUSSIAN_FEDERATION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("WSB")) { return AREA_NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("WSB-OO")) { return AREA_NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUC")) { return AREA_CENTRAL_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUC-OO")) { return AREA_CENTRAL_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUE")) { return AREA_EASTERN_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUE-OO")) { return AREA_EASTERN_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("BLT-KA")) { return AREA_KALININGRAD; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUN")) { return AREA_NORTHERN_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUN-OO")) { return AREA_NORTHERN_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUW")) { return AREA_NORTHWEST_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUW-OO")) { return AREA_NORTHWEST_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUS")) { return AREA_SOUTH_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUS-OO")) { return AREA_SOUTH_EUROPEAN_RUSSIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ROM")) { return AREA_ROMANIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ROM-OO")) { return AREA_ROMANIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_FORMER_USSR; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_RUSSIA_BALTIC; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUC")) { return AREA_RUSSIA_CENTRAL; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_RUSSIA_SOUTHEAST; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("RUN")) { return AREA_RUSSIA_NORTHERN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_RUSSIA_SOUTHWEST; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SAR")) { return AREA_SARDEGNA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SAR-OO")) { return AREA_SARDEGNA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SVA")) { return AREA_SVALBARD_WITH_BJORNOYA_AND_JAN_MAYEN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SVA-OO")) { return AREA_SVALBARD_WITH_BJORNOYA_AND_JAN_MAYEN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SEL")) { return AREA_SELVAGENS_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SEL-OO")) { return AREA_SELVAGENS_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SIC")) { return AREA_SICILY_WITH_MALTA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SIC-MA")) { return AREA_MALTA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SIC-SI")) { return AREA_SICILY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CZE-SK")) { return AREA_SLOVAKIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-SL")) { return AREA_SLOVENIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("CS")) { return AREA_SERBIA_WITH_MONTENEGRO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("YUG-SE")) { return AREA_SERBIA_INCLUDING_VOJVODINA_AND_WITH_KOSOVO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SWE")) { return AREA_SWEDEN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SWE-OO")) { return AREA_SWEDEN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUE")) { return AREA_EUROPEAN_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUE-OO")) { return AREA_EUROPEAN_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_UKRAINE_INCLUDING_CRIMEA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("KRY")) { return AREA_CRIMEA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("KRY-OO")) { return AREA_CRIMEA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("UKR-UK")) { return AREA_UKRAINE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GRE")) { return AREA_GREEK_MAINLAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GR-KRI")) { return AREA_CRETE; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GR-DOD")) { return AREA_DODECANESE_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GR-CYC")) { return AREA_CYCLADES_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("GR-AEG")) { return AREA_NORTH_AEGEAN_ISLANDS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ITA-VC")) { return AREA_VATICAN_CITY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_FRANZ_JOSEF_LAND; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_NOVAYA_ZEMLYA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AZ")) { return AREA_AZERBAIJAN_INCLUDING_NAKHICHEVAN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TCS-AZ")) { return AREA_AZERBAIJAN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TCS-NA")) { return AREA_NAKHICHEVAN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ALG")) { return AREA_ALGERIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ALG-OO")) { return AREA_ALGERIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TCS-AR")) { return AREA_ARMENIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("33")) { return AREA_CAUCASUS_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("EGY")) { return AREA_EGYPT; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("EGY-OO")) { return AREA_EGYPT; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_GEORGIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("PAL")) { return AREA_ISRAEL_JORDAN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("PAL-IS")) { return AREA_ISRAEL; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("PAL-JO")) { return AREA_JORDAN; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("LBS-LB")) { return AREA_LEBANON; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("LBY-OO")) { return AREA_LIBYA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("LBS")) { return AREA_LEBANON_SYRIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("MOR")) { return AREA_MOROCCO; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NCS")) { return AREA_NORTH_CAUCASUS; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SIN")) { return AREA_SINAI; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("SIN-OO")) { return AREA_SINAI; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("LBS-SY")) { return AREA_SYRIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUN")) { return AREA_TUNISIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUN-OO")) { return AREA_TUNISIA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_ASIATIC_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_ASIATIC_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUR")) { return AREA_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TUR-OO")) { return AREA_TURKEY; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("20")) { return AREA_NORTHERN_AFRICA; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AFR")) { return AREA_AFRO_TROPICAL_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("AUS")) { return AREA_AUSTRALIAN_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("TODO")) { return AREA_EAST_PALAEARCTIC; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NEA")) { return AREA_NEARCTIC_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NEO")) { return AREA_NEOTROPICAL_REGION; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("NRE")) { return AREA_NEAR_EAST; }
			else if ((namedArea.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel()).equals("ORR")) { return AREA_ORIENTAL_REGION; }
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
					(namedArea.getUuid().equals(ErmsTransformer.uuidCaspianSea))) { return AREA_CASPIAN_SEA; } // abbreviated label missing
			else if (namedArea.getUuid().equals(ErmsTransformer.uuidPortugueseExclusiveEconomicZone)) { return AREA_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE; }
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
				logger.error("Unknown NamedArea Area: " + area.getTitleCache());
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
		} else if (feature.getUuid().equals(ErmsTransformer.uuidRemark)) {
			return NoteCategory_STR_Remark;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidAdditionalinformation)) {
			return NoteCategory_STR_Additional_information;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidSpelling)) {
			return NoteCategory_STR_Spelling;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidDateofPublication)) {
			return NoteCategory_STR_Date_of_publication;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidSystematics)) {
			return NoteCategory_STR_Systematics;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidClassification)) {
			return NoteCategory_STR_Classification;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidEnvironment)) {
			return NoteCategory_STR_Environment;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidHabitat)) {
			return NoteCategory_STR_Habitat;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidAuthority)) {
			return NoteCategory_STR_Authority;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidMorphology)) {
			return NoteCategory_STR_Morphology;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidTaxonomicRemarks)) {
			return NoteCategory_STR_Taxonomic_Remarks;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidNote)){
			return NoteCategory_STR_Note;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidTaxonomy)) {
			return NoteCategory_STR_Taxonomy;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidTaxonomicstatus)) {
			return NoteCategory_STR_Taxonomic_status;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidStatus)){
			return NoteCategory_STR_Status;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidRank)) {
			return NoteCategory_STR_Rank;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidHomonymy)) {
			return NoteCategory_STR_Homonymy;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidNomenclature)) {
			return NoteCategory_STR_Nomenclature;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidTaxonomicRemark)) {
			return NoteCategory_STR_Taxonomic_Remark;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidAcknowledgments)){
			return NoteCategory_STR_Acknowledgments;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidOriginalpublication)) {
			return NoteCategory_STR_Original_publication;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidTypelocality)) {
			return NoteCategory_STR_Type_locality;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidValidity)) {
			return NoteCategory_STR_Validity;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidIdentification)) {
			return NoteCategory_STR_Identification;
		} else if (feature.getUuid().equals(ErmsTransformer.uuidSynonymy)) {
			return NoteCategory_STR_Synonymy;
//		} else if (feature.equals(Feature.CITATION())) {
//			return;
			
			// TODO: Unknown NodeCategories
//			NoteCategory_STR_general_distribution_euromed = 10;
//			NoteCategory_STR_general_distribution_world = 11;
//			NoteCategory_STR_Common_names = 12;
//			NoteCategory_STR_Maps =14;
//			NoteCategory_STR_Link_to_maps = 20;
//			NoteCategory_STR_Link_to_images = 21;
//			NoteCategory_STR_Link_to_taxonomy = 22;
//			NoteCategory_STR_Link_to_general_information = 23;
//			NoteCategory_STR_undefined_link = 24;
//			NoteCategory_STR_Editor_Braces = 249;
//			NoteCategory_STR_Editor_Brackets = 250;
//			NoteCategory_STR_Editor_Parenthesis = 251;
//			NoteCategory_STR_Inedited = 252;
//			NoteCategory_STR_Comments_on_editing_process = 253;
//			NoteCategory_STR_Publication_date = 254;
//			NoteCategory_STR_Distribution = 278;
//			NoteCategory_STR_Biology = 281;
//			NoteCategory_STR_Diagnosis	= 282;
//			NoteCategory_STR_Host = 283;
		
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
//			NoteCategory_Distribution = 278;
//			NoteCategory_Biology = 281;
//			NoteCategory_Diagnosis	= 282;
//			NoteCategory_Host = 283;

		}else{
			logger.warn("Unknown Feature.");
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
		if (pesiKingdomId != null && pesiKingdomId.intValue() == KINGDOM_ANIMALIA) {
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
			} else {
				//TODO Exception
				logger.warn("Rank for Kingdom Animalia not yet supported in CDM: "+ rank.getLabel());
				return null;
			}
		} else if (pesiKingdomId != null && pesiKingdomId.intValue() == KINGDOM_PLANTAE) {
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
			} else {
				//TODO Exception
				logger.warn("Rank for Kingdom Plantae not yet supported in CDM: "+ rank.getLabel());
				return null;
			}
		} else if (pesiKingdomId != null && pesiKingdomId.intValue() == KINGDOM_BACTERIA) {
			if (rank.equals(Rank.KINGDOM())) { result = Bacteria_STR_Kingdom; }
			else if (rank.equals(Rank.SUBKINGDOM())) { result = Bacteria_STR_Subkingdom; }
			else if (rank.equals(Rank.PHYLUM())) { result = Bacteria_STR_Phylum; }
			else if (rank.equals(Rank.SUBPHYLUM())) { result = Bacteria_STR_Subphylum; }
			else if (rank.equals(Rank.SUPERCLASS())) { result = Bacteria_STR_Superclass; }
			else if (rank.equals(Rank.CLASS())) { result = Bacteria_STR_Class; }
			else if (rank.equals(Rank.SUBCLASS())) { result = Bacteria_STR_Subclass; }
			else if (rank.equals(Rank.INFRACLASS())) { result = Bacteria_STR_Infraclass; }
			else if (rank.equals(Rank.SUPERORDER())) { result = Bacteria_STR_Superorder; }
			else if (rank.equals(Rank.ORDER())) { result = Bacteria_STR_Order; }
			else if (rank.equals(Rank.SUBORDER())) { result = Bacteria_STR_Suborder; }
			else if (rank.equals(Rank.INFRAORDER())) { result = Bacteria_STR_Infraorder; }
			else if (rank.equals(Rank.SUPERFAMILY())) { result = Bacteria_STR_Superfamily; }
			else if (rank.equals(Rank.FAMILY())) { result = Bacteria_STR_Family; }
			else if (rank.equals(Rank.SUBFAMILY())) { result = Bacteria_STR_Subfamily; }
			else if (rank.equals(Rank.TRIBE())) { result = Bacteria_STR_Tribe; }
			else if (rank.equals(Rank.SUBTRIBE())) { result = Bacteria_STR_Subtribe; }
			else if (rank.equals(Rank.GENUS())) { result = Bacteria_STR_Genus; }
			else if (rank.equals(Rank.SUBGENUS())) { result = Bacteria_STR_Subgenus; }
			else if (rank.equals(Rank.SPECIES())) { result = Bacteria_STR_Species; }
			else if (rank.equals(Rank.SUBSPECIES())) { result = Bacteria_STR_Subspecies; }
			else if (rank.equals(Rank.VARIETY())) { result = Bacteria_STR_Variety; }
			else if (rank.equals(Rank.FORM())) { result = Bacteria_STR_Forma; }
		} else {
			//TODO Exception
			logger.warn("Kingdom not yet supported in CDM: "+ pesiKingdomId);
			return null;
		}
		return result;
	}
	
	/**
	 * Returns the abbreviation for a given rank.
	 * @param rank
	 * @param pesiKingdomId
	 * @return
	 */
	public static String rank2RankAbbrev(Rank rank, Integer pesiKingdomId) {
		String result = null;
		if (rank == null) {
			return null;
		}
		
		// We differentiate between Animalia and Plantae only for now.
		if (pesiKingdomId != null && pesiKingdomId.intValue() == KINGDOM_ANIMALIA) {
			if (rank.equals(Rank.SUBGENUS())) {
				result = Animalia_Abbrev_Subgenus;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Animalia_Abbrev_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Animalia_Abbrev_Subspecies;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Animalia_STR_Natio;
			} else if (rank.equals(Rank.VARIETY())) {
				result = Animalia_Abbrev_Variety;
			} else if (rank.equals(Rank.SUBVARIETY())) {
				result = Animalia_Abbrev_Subvariety;
			} else if (rank.equals(Rank.FORM())) {
				result = Animalia_Abbrev_Forma;
			} else {
				//TODO Exception
				logger.warn("Abbreviation for Rank of Kingdom Animalia not supported in CDM: "+ rank.getLabel());
				return null;
			}
		} else if (pesiKingdomId != null && pesiKingdomId.intValue() == KINGDOM_PLANTAE) {
			if (rank.equals(Rank.KINGDOM())) {
				result = Plantae_Abbrev_Kingdom;
			} else if (rank.equals(Rank.SUBKINGDOM())) {
				result = Plantae_Abbrev_Subkingdom;
			} else if (rank.equals(Rank.DIVISION())) {
				result = Plantae_Abbrev_Division;
			} else if (rank.equals(Rank.SUBDIVISION())) {
				result = Plantae_Abbrev_Subdivision;
			} else if (rank.equals(Rank.CLASS())) {
				result = Plantae_Abbrev_Class;
			} else if (rank.equals(Rank.SUBCLASS())) {
				result = Plantae_Abbrev_Subclass;
			} else if (rank.equals(Rank.ORDER())) {
				result = Plantae_Abbrev_Order;
			} else if (rank.equals(Rank.SUBORDER())) {
				result = Plantae_Abbrev_Suborder;
			} else if (rank.equals(Rank.FAMILY())) {
				result = Plantae_Abbrev_Family;
			} else if (rank.equals(Rank.SUBFAMILY())) {
				result = Plantae_Abbrev_Subfamily;
			} else if (rank.equals(Rank.TRIBE())) {
				result = Plantae_Abbrev_Tribe;
			} else if (rank.equals(Rank.SUBTRIBE())) {
				result = Plantae_Abbrev_Subtribe;
			} else if (rank.equals(Rank.GENUS())) {
				result = Plantae_Abbrev_Genus;
			} else if (rank.equals(Rank.SUBGENUS())) {
				result = Plantae_Abbrev_Subgenus;
			} else if (rank.equals(Rank.SECTION_BOTANY())) {
				result = Plantae_Abbrev_Section;
			} else if (rank.equals(Rank.SUBSECTION_BOTANY())) {
				result = Plantae_Abbrev_Subsection;
			} else if (rank.equals(Rank.SERIES())) {
				result = Plantae_Abbrev_Series;
			} else if (rank.equals(Rank.SUBSERIES())) {
				result = Plantae_Abbrev_Subseries;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Abbrev_Aggregate;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Abbrev_Coll_Species;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Plantae_Abbrev_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Plantae_Abbrev_Subspecies;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Abbrev_Proles;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Abbrev_Race;
			} else if (rank.equals(Rank.CONVAR())) {
				result = Plantae_Abbrev_Convarietas;
			} else if (rank.equals(Rank.VARIETY())) {
				result = Plantae_Abbrev_Variety;
			} else if (rank.equals(Rank.SUBVARIETY())) {
				result = Plantae_Abbrev_Subvariety;
			} else if (rank.equals(Rank.FORM())) {
				result = Plantae_Abbrev_Forma;
			} else if (rank.equals(Rank.SUBFORM())) {
				result = Plantae_Abbrev_Subforma;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Abbrev_Forma_spec;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Abbrev_Taxa_infragen;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Abbrev_Taxa_infraspec;
			} else {
				//TODO Exception
				logger.warn("Abbreviation for Rank of Kingdom Plantae not supported in CDM: "+ rank.getLabel());
				return null;
			}
		} else {
			//TODO Exception
			logger.warn("Kingdom not yet supported in CDM: "+ pesiKingdomId);
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
	 * Returns the NomenclaturalCode for a given TaxonNameBase.
	 * @param taxonName
	 * @return
	 */
	public static NomenclaturalCode getNomenclaturalCode(TaxonNameBase taxonName) {
		NomenclaturalCode code = null;
		if (taxonName.isInstanceOf(ZoologicalName.class)) {
			code = NomenclaturalCode.ICZN;
		} else if (taxonName.isInstanceOf(BotanicalName.class)) {
			code = NomenclaturalCode.ICBN;
		} else if (taxonName.isInstanceOf(BacterialName.class)) {
			code = NomenclaturalCode.ICNB;
//		} else if (taxonName.isInstanceOf(NonViralName.class)) { // Biota
//			code = NomenclaturalCode.
		} else {
			logger.error("NomenclaturalCode could not be determined for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			logger.error("");
		}
		return code;
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
		if (type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())) {
			return STR_IS_MISAPPLIED_NAME_FOR;
		} else if (type.equals(SynonymRelationshipType.SYNONYM_OF())) {
			return STR_IS_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
			return STR_IS_HOMOTYPIC_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())) {
			return STR_IS_HETEROTYPIC_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.INFERRED_EPITHET_OF())) {
			return STR_IS_INFERRED_EPITHET_FOR;
		} else if (type.equals(SynonymRelationshipType.INFERRED_GENUS_OF())) {
			return STR_IS_INFERRED_GENUS_FOR;
		} else if (type.equals(SynonymRelationshipType.POTENTIAL_COMBINATION_OF())) {
			return STR_IS_POTENTIAL_COMBINATION_FOR;
		} else if (type.equals(NameRelationshipType.BASIONYM())) {
			return STR_IS_BASIONYM_FOR;
		} else if (type.equals(NameRelationshipType.LATER_HOMONYM())) {
			return STR_IS_LATER_HOMONYM_OF;
		} else if (type.equals(NameRelationshipType.REPLACED_SYNONYM())) {
			return STR_IS_REPLACED_SYNONYM_FOR;
		} else if (type.equals(NameRelationshipType.VALIDATED_BY_NAME())) {
			return STR_IS_VALIDATION_OF;
		} else if (type.equals(NameRelationshipType.LATER_VALIDATED_BY_NAME())) {
			return STR_IS_LATER_VALIDATION_OF;
		} else if (type.equals(NameRelationshipType.CONSERVED_AGAINST())) {
			return STR_IS_CONSERVED_AGAINST;
		} else if (type.equals(NameRelationshipType.TREATED_AS_LATER_HOMONYM())) {
			return STR_IS_TREATED_AS_LATER_HOMONYM_OF;
		} else if (type.equals(NameRelationshipType.ORTHOGRAPHIC_VARIANT())) {
			return STR_IS_ORTHOGRAPHIC_VARIANT_OF;
		} else if (type.equals(NameRelationshipType.ALTERNATIVE_NAME())) {
			return STR_IS_ALTERNATIVE_NAME_FOR;
		} else {
			logger.warn("No equivalent RelationshipType found in datawarehouse for: " + type.getTitleCache());
		}
			
		// The following have no equivalent attribute in CDM
//		IS_TYPE_OF
//		IS_CONSERVED_TYPE_OF
//		IS_REJECTED_TYPE_OF
//		IS_FIRST_PARENT_OF
//		IS_SECOND_PARENT_OF
//		IS_FEMALE_PARENT_OF
//		IS_MALE_PARENT_OF
//		IS_REJECTED_IN_FAVOUR_OF
//		HAS_SAME_TYPE_AS
//		IS_LECTOTYPE_OF
//		TYPE_NOT_DESIGNATED
//		IS_PRO_PARTE_SYNONYM_OF
//		IS_PARTIAL_SYNONYM_OF
//		IS_PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HETEROTYPIC_SYNONYM_OF

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
		if (type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())) {
			return IS_MISAPPLIED_NAME_FOR;
		} else if (type.equals(SynonymRelationshipType.SYNONYM_OF())) {
			return IS_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
			return IS_HOMOTYPIC_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())) {
			return IS_HETEROTYPIC_SYNONYM_OF;
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
		} else {
			logger.warn("No equivalent RelationshipType found in datawarehouse for: " + type.getTitleCache());
		}

		// The following have no equivalent attribute in CDM
//		IS_TYPE_OF
//		IS_CONSERVED_TYPE_OF
//		IS_REJECTED_TYPE_OF
//		IS_FIRST_PARENT_OF
//		IS_SECOND_PARENT_OF
//		IS_FEMALE_PARENT_OF
//		IS_MALE_PARENT_OF
//		IS_REJECTED_IN_FAVOUR_OF
//		HAS_SAME_TYPE_AS
//		IS_LECTOTYPE_OF
//		TYPE_NOT_DESIGNATED
//		IS_PRO_PARTE_SYNONYM_OF
//		IS_PARTIAL_SYNONYM_OF
//		IS_PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HETEROTYPIC_SYNONYM_OF

		return null;
	}
	
	/**
	 * Returns the QualityStatusFk for a given QualityStatusCache.
	 * @param qualityStatusCache
	 * @return
	 */
	public Integer QualityStatusCache2QualityStatusFk(String qualityStatusCache) {
		Integer result = null;
		if (qualityStatusCache.equalsIgnoreCase("Checked by Taxonomic Editor: included in ERMS 1.1")) {
			return 0;
		} else if (qualityStatusCache.equalsIgnoreCase("Added by Database Management Team")) {
			return 2;
		} else if (qualityStatusCache.equalsIgnoreCase("Checked by Taxonomic Editor")) {
			return 3;
		} else if (qualityStatusCache.equalsIgnoreCase("Edited by Database Management Team")) {
			return 4;
		} else {
			logger.error("QualityStatusFk could not be determined. QualityStatusCache unknown: " + qualityStatusCache);
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

}
