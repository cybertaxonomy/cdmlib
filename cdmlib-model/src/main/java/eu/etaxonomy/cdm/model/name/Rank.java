/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import java.util.*;

import javax.persistence.*;

/**
 * The class representing the taxonomical ranks (like "Family", "Genus" or
 * "Species") used for {@link TaxonNameBase taxon names} across all {@link NomenclaturalCode nomenclatural codes}
 * for bacteria (ICNB), viruses (ICVCN), plants and fungi (ICBN),
 * cultivars (ICNCP) and animals (ICZN).
 * <P>
 * A standard (ordered) list of taxonomical rank instances will be automatically
 * created as the project starts. But this class allows to extend this standard
 * list by creating new instances of additional taxonomical ranks if needed. 
 * <P>
 * This class corresponds to: <ul>
 * <li> TaxonRankTerm according to the TDWG ontology
 * <li> TaxonomicRankEnum according to the TCS
 * <li> Rank according to the ABCD schema
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:46
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Rank")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class Rank extends OrderedTermBase<Rank> {
	private static final long serialVersionUID = -8648081681348758485L;
	private static final Logger logger = Logger.getLogger(Rank.class);
	
	private static final UUID uuidEmpire = UUID.fromString("ac470211-1586-4b24-95ca-1038050b618d");
	private static final UUID uuidDomain = UUID.fromString("ffca6ec8-8b88-417b-a6a0-f7c992aac19b");
	private static final UUID uuidSuperkingdom = UUID.fromString("64223610-7625-4cfd-83ad-b797bf7f0edd");
	private static final UUID uuidKingdom = UUID.fromString("fbe7109d-66b3-498c-a697-c6c49c686162");
	private static final UUID uuidSubkingdom = UUID.fromString("a71bd9d8-f3ab-4083-afb5-d89315d71655");
	private static final UUID uuidInfrakingdom = UUID.fromString("1e37930c-86cf-44f6-90fd-7822928df260");
	private static final UUID uuidSuperphylum = UUID.fromString("0d0cecb1-e254-4607-b210-6801e7ecbb04");
	private static final UUID uuidPhylum = UUID.fromString("773430d2-76b4-438c-b817-97a543a33287");
	private static final UUID uuidSubphylum = UUID.fromString("23a9b6ff-9408-49c9-bd9e-7a2ca5ab4725");
	private static final UUID uuidInfraphylum = UUID.fromString("1701de3a-7693-42a5-a2d3-42697f944190");
	private static final UUID uuidSuperdivision = UUID.fromString("a735a48f-4fc8-49a7-ae0c-6a984f658131");
	private static final UUID uuidDivision = UUID.fromString("7e56f5cc-123a-4fd1-8cbb-6fd80358b581");
	private static final UUID uuidSubdivision = UUID.fromString("931c840f-7a6b-4d76-ad38-bfdd77d7b2e8");
	private static final UUID uuidInfradivision = UUID.fromString("c0ede273-be52-4dee-b411-66ee08d30c94");
	private static final UUID uuidSuperclass = UUID.fromString("e65b4e1a-21ec-428d-9b9f-e87721ab967c");
	private static final UUID uuidClass = UUID.fromString("f23d14c4-1d34-4ee6-8b4e-eee2eb9a3daf");
	private static final UUID uuidSubclass = UUID.fromString("8cb26733-e2f5-46cb-ab5c-f99254f877aa");
	private static final UUID uuidInfraclass = UUID.fromString("ad23cfda-879a-4021-8629-c54d27caf717");
	private static final UUID uuidSuperorder = UUID.fromString("c8c67a22-301a-4219-b882-4a49121232ff");
	private static final UUID uuidOrder = UUID.fromString("b0785a65-c1c1-4eb4-88c7-dbd3df5aaad1");
	private static final UUID uuidSuborder = UUID.fromString("768ad378-fa85-42ab-b668-763225832f57");
	private static final UUID uuidInfraorder = UUID.fromString("84099182-a6f5-47d7-8586-33c9e9955a10");
	private static final UUID uuidSectionZoology = UUID.fromString("691d371e-10d7-43f0-93db-3d7fa1a62c54");
	private static final UUID uuidSubsectionZoology = UUID.fromString("0ed32d28-adc4-4303-a9ca-68e2acd67e33");
	private static final UUID uuidSuperfamily = UUID.fromString("2cfa510a-dcea-4a03-b66a-b1528f9b0796");
	private static final UUID uuidFamily = UUID.fromString("af5f2481-3192-403f-ae65-7c957a0f02b6");
	private static final UUID uuidSubfamily = UUID.fromString("862526ee-7592-4760-a23a-4ff3641541c5");
	private static final UUID uuidInfrafamily = UUID.fromString("c3f2e3bb-6eef-4a26-9fb7-b14f4c8c5e4f");
	private static final UUID uuidSupertribe = UUID.fromString("11e94828-8c61-499b-87d6-1de35ce2c51c");
	private static final UUID uuidTribe = UUID.fromString("4aa6890b-0363-4899-8d7c-ee0cb78e6166");
	private static final UUID uuidSubtribe = UUID.fromString("ae41ecc5-5165-4126-9d24-79939ae5d822");
	private static final UUID uuidInfratribe = UUID.fromString("1ec02e8f-f2b7-4c65-af9f-b436b34c79a3");
	private static final UUID uuidSupragenericTaxon = UUID.fromString("1fdc0b93-c354-441a-8406-091e0303ff5c");
	private static final UUID uuidGenus = UUID.fromString("1b11c34c-48a8-4efa-98d5-84f7f66ef43a");
	private static final UUID uuidSubgenus = UUID.fromString("78786e16-2a70-48af-a608-494023b91904");
	private static final UUID uuidInfragenus = UUID.fromString("a9972969-82cd-4d54-b693-a096422f13fa");
	private static final UUID uuidSectionBotany = UUID.fromString("3edff68f-8527-49b5-bf91-7e4398bb975c");
	private static final UUID uuidSubsectionBotany = UUID.fromString("d20f5b61-d463-4448-8f8a-c1ff1f262f59");
	private static final UUID uuidSeries = UUID.fromString("d7381ecf-48f8-429b-9c54-f461656978cd");
	private static final UUID uuidSubseries = UUID.fromString("80c9a263-f4db-4a13-b6c2-b7fec1aa1200");
	private static final UUID uuidSpeciesAggregate = UUID.fromString("1ecae058-4217-4f75-9c27-6d8ba099ac7a");
	private static final UUID uuidSpeciesGroup = UUID.fromString("d1988a11-292b-46fa-8fb7-bc64ea6d8fc6");
	private static final UUID uuidInfragenericTaxon = UUID.fromString("41bcc6ac-37d3-4fd4-bb80-3cc5b04298b9");
	private static final UUID uuidSpecies = UUID.fromString("b301f787-f319-4ccc-a10f-b4ed3b99a86d");
	private static final UUID uuidSubspecificAggregate = UUID.fromString("72c248b9-027d-4402-b375-dd4f0850c9ad");
	private static final UUID uuidSubspecies = UUID.fromString("462a7819-8b00-4190-8313-88b5be81fad5");
	private static final UUID uuidInfraspecies = UUID.fromString("f28ebc9e-bd50-4194-9af1-42f5cb971a2c");
	private static final UUID uuidVariety = UUID.fromString("d5feb6a5-af5c-45ef-9878-bb4f36aaf490");
	private static final UUID uuidBioVariety = UUID.fromString("a3a364cb-1a92-43fc-a717-3c44980a0991");
	private static final UUID uuidPathoVariety = UUID.fromString("2f4f4303-a099-47e3-9048-d749d735423b");
	private static final UUID uuidSubvariety = UUID.fromString("9a83862a-7aee-480c-a98d-4bceaf8712ca");
	private static final UUID uuidSubsubvariety = UUID.fromString("bff22f84-553a-4429-a4e7-c4b3796c3a18");
	private static final UUID uuidConvar = UUID.fromString("2cc740c9-cebb-43c8-9b06-1bef79e6a56a");
	private static final UUID uuidForm = UUID.fromString("0461281e-458a-47b9-8d41-19a3d39356d5");
	private static final UUID uuidSpecialForm = UUID.fromString("bed20aee-2f5a-4635-9c02-eff06246d067");
	private static final UUID uuidSubform = UUID.fromString("47cfc5b0-0fb7-4ceb-b61d-e1dd8de8b569");
	private static final UUID uuidSubsubform = UUID.fromString("1c8ac389-4349-4ae0-87be-7239f6635068");
	private static final UUID uuidInfraspecificTaxon = UUID.fromString("eb75c27d-e154-4570-9d96-227b2df60474");
	private static final UUID uuidCandidate = UUID.fromString("ead9a1f5-dfd4-4de2-9121-70a47accb10b");
	private static final UUID uuidDenominationClass = UUID.fromString("49bdf74a-2170-40ed-8be2-887a0db517bf");
	private static final UUID uuidGrex = UUID.fromString("08dcb4ff-ac58-48a3-93af-efb3d836ac84");
	private static final UUID uuidGraftChimaera = UUID.fromString("6b4063bc-f934-4796-9bf3-0ef3aea5c1cb");
	private static final UUID uuidCultivarGroup = UUID.fromString("d763e7d3-e7de-4bb1-9d75-225ca6948659");
	private static final UUID uuidCultivar = UUID.fromString("5e98415b-dc6e-440b-95d6-ea33dbb39ad0");
	private static final UUID uuidUnknownRank = UUID.fromString("5c4d6755-2cf6-44ca-9220-cccf8881700b");

	private static Rank UNKNOWN_RANK;
	private static Rank CULTIVAR;
	private static Rank CULTIVARGROUP;
	private static Rank GRAFTCHIMAERA;
	private static Rank GREX;
	private static Rank DENOMINATIONCLASS;
	private static Rank CANDIDATE;
	private static Rank INFRASPECIFICTAXON;
	private static Rank SUBSUBFORM;
	private static Rank SUBFORM;
	private static Rank SPECIALFORM;
	private static Rank FORM;
	private static Rank CONVAR;
	private static Rank SUBSUBVARIETY;
	private static Rank SUBVARIETY;
	private static Rank PATHOVARIETY;
	private static Rank BIOVARIETY;
	private static Rank VARIETY;
	private static Rank INFRASPECIES;
	private static Rank SUBSPECIES;
	private static Rank SUBSPECIFICAGGREGATE;
	private static Rank SPECIES;
	private static Rank INFRAGENERICTAXON;
	private static Rank SPECIESAGGREGATE;
	private static Rank SPECIESGROUP;
	private static Rank SUBSERIES;
	private static Rank SERIES;
	private static Rank SUBSECTION_BOTANY;
	private static Rank SECTION_BOTANY;
	private static Rank INFRAGENUS;
	private static Rank SUBGENUS;
	private static Rank GENUS;
	private static Rank SUPRAGENERICTAXON;
	private static Rank INFRATRIBE;
	private static Rank SUBTRIBE;
	private static Rank TRIBE;
	private static Rank SUPERTRIBE;
	private static Rank INFRAFAMILY;
	private static Rank SUBFAMILY;
	private static Rank FAMILY;
	private static Rank SUPERFAMILY;
	private static Rank SUBSECTION_ZOOLOGY;
	private static Rank SECTION_ZOOLOGY;
	private static Rank INFRAORDER;
	private static Rank SUBORDER;
	private static Rank ORDER;
	private static Rank SUPERORDER;
	private static Rank INFRACLASS;
	private static Rank SUBCLASS;
	private static Rank CLASS;
	private static Rank SUPERCLASS;
	private static Rank INFRADIVISION;
	private static Rank SUBDIVISION;
	private static Rank DIVISION;
	private static Rank SUPERDIVISION;
	private static Rank INFRAPHYLUM;
	private static Rank SUBPHYLUM;
	private static Rank PHYLUM;
	private static Rank SUPERPHYLUM;
	private static Rank INFRAKINGDOM;
	private static Rank SUBKINGDOM;
	private static Rank KINGDOM;
	private static Rank SUPERKINGDOM;
	private static Rank DOMAIN;
	private static Rank EMPIRE;
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty rank instance.
	 * 
	 * @see 	#Rank(String, String, String)
	 */
	public Rank() {
	}

	/** 
	 * Class constructor: creates an additional rank instance with a description
	 * (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}), a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new rank to be created 
	 * @param	label  		 the string identifying the new rank to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new rank to be created
	 * @see 	#Rank()
	 */
	public Rank(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	/** 
	 * Creates a new empty rank.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	private static Rank NewInstance(){
		return new Rank();
	}
	
	/** 
	 * Creates an additional rank with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new rank to be created 
	 * @param	label  		 the string identifying the new rank to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new rank to be created
	 * @see 				 #NewInstance()
	 */
	private static Rank NewInstance(String term, String label, String labelAbbrev){
		return new Rank(term, label, labelAbbrev);
	}

	//********* METHODS **************************************/
	
	public static final Rank EMPIRE(){
	  return EMPIRE;
	}
	public static final Rank DOMAIN(){
	  return DOMAIN; 
	}
	public static final Rank SUPERKINGDOM(){
	  return SUPERKINGDOM; 
	}
	public static final Rank KINGDOM(){
	  return KINGDOM;
	}
	public static final Rank SUBKINGDOM(){
	  return SUBKINGDOM;
	}
	public static final Rank INFRAKINGDOM(){
	  return INFRAKINGDOM; 
	}
	public static final Rank SUPERPHYLUM(){
	  return SUPERPHYLUM;
	}
	public static final Rank PHYLUM(){
	  return PHYLUM;
	}
	public static final Rank SUBPHYLUM(){
	  return SUBPHYLUM;
	}
	public static final Rank INFRAPHYLUM(){
	  return INFRAPHYLUM; 
	}
	public static final Rank SUPERDIVISION(){
	  return SUPERDIVISION;
	}
	public static final Rank DIVISION(){
	  return DIVISION;
	}
	public static final Rank SUBDIVISION(){
	  return SUBDIVISION;
	}
	public static final Rank INFRADIVISION(){
	  return INFRADIVISION; 
	}
	public static final Rank SUPERCLASS(){
	  return SUPERCLASS;
	}
	public static final Rank CLASS(){
	  return CLASS;
	}
	public static final Rank SUBCLASS(){
	  return SUBCLASS;
	}
	public static final Rank INFRACLASS(){
	  return INFRACLASS;
	}
	public static final Rank SUPERORDER(){
	  return SUPERORDER;
	}
	public static final Rank ORDER(){
	  return ORDER;
	}
	public static final Rank SUBORDER(){
	  return SUBORDER;
	}
	public static final Rank INFRAORDER(){
	  return INFRAORDER;
	}
	public static final Rank SUPERFAMILY(){
	  return SUPERFAMILY;
	}
	public static final Rank FAMILY(){
	  return FAMILY;
	}
	public static final Rank SUBFAMILY(){
	  return SUBFAMILY;
	}
	public static final Rank INFRAFAMILY(){
	  return INFRAFAMILY;
	}
	public static final Rank SUPERTRIBE(){
	  return SUPERTRIBE;
	}
	public static final Rank TRIBE(){
	  return TRIBE;
	}
	public static final Rank SUBTRIBE(){
	  return SUBTRIBE;
	}
	public static final Rank INFRATRIBE(){
	  return INFRATRIBE;
	}
	public static final Rank SUPRAGENERICTAXON(){
	  return SUPRAGENERICTAXON;
	}
	public static final Rank GENUS(){
	  return GENUS;
	}
	public static final Rank SUBGENUS(){
	  return SUBGENUS;
	}
	public static final Rank INFRAGENUS(){
	  return INFRAGENUS;
	}
	public static final Rank SECTION_BOTANY(){
	  return SECTION_BOTANY;
	}
	public static final Rank SUBSECTION_BOTANY(){
	  return SUBSECTION_BOTANY;
	}
	public static final Rank SECTION_ZOOLOGY(){
		return SECTION_ZOOLOGY;
	}
	public static final Rank SUBSECTION_ZOOLOGY(){
		return SUBSECTION_ZOOLOGY;
	}
	public static final Rank SERIES(){
	  return SERIES;
	}
	public static final Rank SUBSERIES(){
	  return SUBSERIES;
	}
	public static final Rank SPECIESAGGREGATE(){
	  return SPECIESAGGREGATE;
	}
	public static final Rank SPECIESGROUP(){
	  return SPECIESGROUP;
	}
	public static final Rank INFRAGENERICTAXON(){
	  return INFRAGENERICTAXON;
	}
	public static final Rank SPECIES(){
	  return SPECIES;
	}
	public static final Rank SUBSPECIFICAGGREGATE(){
	  return SUBSPECIFICAGGREGATE; 
	}
	public static final Rank SUBSPECIES(){
	  return SUBSPECIES;
	}
	public static final Rank INFRASPECIES(){
	  return INFRASPECIES;
	}
	public static final Rank VARIETY(){
	  return VARIETY;
	}
	public static final Rank BIOVARIETY(){
	  return BIOVARIETY;
	}
	public static final Rank PATHOVARIETY(){
	  return PATHOVARIETY;
	}
	public static final Rank SUBVARIETY(){
	  return SUBVARIETY;
	}
	public static final Rank SUBSUBVARIETY(){
	  return SUBSUBVARIETY;
	}
	public static final Rank CONVAR(){
	  return CONVAR;
	}
	public static final Rank FORM(){
	  return FORM;
	}
	public static final Rank SPECIALFORM(){
	  return SPECIALFORM;
	}
	public static final Rank SUBFORM(){
	  return SUBFORM;
	}
	public static final Rank SUBSUBFORM(){
	  return SUBSUBFORM;
	}
	public static final Rank INFRASPECIFICTAXON(){
	  return INFRASPECIFICTAXON;
	}
	public static final Rank CANDIDATE(){
	  return CANDIDATE;
	}
	public static final Rank DENOMINATIONCLASS(){
	  return DENOMINATIONCLASS;
	}
	public static final Rank GREX(){
	  return GREX;
	}
	public static final Rank GRAFTCHIMAERA(){
	  return GRAFTCHIMAERA;
	}
	public static final Rank CULTIVARGROUP(){
	  return CULTIVARGROUP;
	}
	public static final Rank CULTIVAR(){
	  return CULTIVAR;
	}
	public static final Rank UNKNOWN_RANK(){
		  return UNKNOWN_RANK;
	}
	
	/**
	 * Returns the boolean value indicating whether <i>this</i> rank is higher than 
	 * the genus rank (true) or not (false). Returns false if <i>this</i> rank is null.
	 *
	 * @see  #isGenus()
	 * @see  #isInfraGeneric()
	 * @see  #isSpecies()
	 * @see  #isInfraSpecific()
	 */
	@Transient
	public boolean isSupraGeneric(){
		return (this.isHigher(Rank.GENUS()));
	}
	
	/**
	 * Returns the boolean value indicating whether <i>this</i> rank is the genus rank
	 * (true) or not (false). Returns false if <i>this</i> rank is null.
	 *
	 * @see  #isSupraGeneric()
	 * @see  #isInfraGeneric()
	 * @see  #isSpecies()
	 * @see  #isInfraSpecific()
	 */
	@Transient
	public boolean isGenus(){
		return (this.equals(Rank.GENUS()));
	}

	/**
	 * Returns the boolean value indicating whether <i>this</i> rank is higher than the
	 * species rank and lower than the genus rank (true) or not (false).
	 * Returns false if <i>this</i> rank is null.
	 *
	 * @see  #isSupraGeneric()
	 * @see  #isGenus()
	 * @see  #isSpecies()
	 * @see  #isInfraSpecific()
	 */
	@Transient
	public boolean isInfraGeneric(){
		return (this.isLower(Rank.GENUS()) && this.isHigher(Rank.SPECIES()));
	}

	/**
	 * Returns true if this rank indicates a rank that aggregates species
	 * like species aggregates or species groups, false otherwise. This methods 
	 * currently returns false for all user defined ranks.
	 * @return
	 */
	@Transient
	public boolean isSpeciesAggregate(){
		return (this.equals(Rank.SPECIESAGGREGATE) || this.equals(Rank.SPECIESGROUP()));
	}	
	
	/**
	 * Returns the boolean value indicating whether <i>this</i> rank is the species
	 * rank (true) or not (false). Returns false if <i>this</i> rank is null.
	 *
	 * @see  #isSupraGeneric()
	 * @see  #isGenus()
	 * @see  #isInfraGeneric()
	 * @see  #isInfraSpecific()
	 */
	@Transient
	public boolean isSpecies(){
		return (this.equals(Rank.SPECIES()));
	}

	/**
	 * Returns the boolean value indicating whether <i>this</i> rank is lower than the
	 * species rank (true) or not (false). Returns false if <i>this</i> rank is null.
	 *
	 * @see  #isSupraGeneric()
	 * @see  #isGenus()
	 * @see  #isInfraGeneric()
	 * @see  #isSpecies()
	 */
	@Transient
	public boolean isInfraSpecific(){
		return (this.isLower(Rank.SPECIES()));
	}


	/**
	 * Returns the rank identified through a name (abbreviated or not).
	 * Preliminary implementation for BotanicalNameParser.
	 * 
	 * @param	strRank	the string identifying the rank
	 * @return  		the rank
	 */
	public static Rank getRankByNameOrAbbreviation(String strRank)
				throws UnknownCdmTypeException{
		return getRankByNameOrAbbreviation(strRank, false);
	}

	/**
	 * Returns the rank identified through a name (abbreviated or not) for a given nomenclatural code.
	 * Preliminary implementation for BotanicalNameParser.
	 * 
	 * @param	strRank	the string identifying the rank
	 * @param   nc      the nomenclatural code
	 * @return  		the rank
	 */
	public static Rank getRankByNameOrAbbreviation(String strRank, NomenclaturalCode nc)
				throws UnknownCdmTypeException{
		return getRankByNameOrAbbreviation(strRank, nc, false);
	}
	
	// TODO
	// Preliminary implementation for BotanicalNameParser.
	// not yet complete
	/**
	 * Returns the rank identified through a name (abbreviated or not).
	 * Preliminary implementation for BotanicalNameParser.
	 * 
	 * @param	strRank	the string identifying the rank
	 * @param 	useUnknown 	if true the rank UNKNOWN_RANK is returned if the abbrev is 
	 * 			unknown or not yet implemented
	 * @return  		the rank
	 */
	public static Rank getRankByNameOrAbbreviation(String strRank, boolean useUnknown)
			throws UnknownCdmTypeException{
		try {
			return getRankByAbbreviation(strRank);
		} catch (UnknownCdmTypeException e) {
			return getRankByName(strRank, useUnknown);
		}
	}
	
	// TODO
	// Preliminary implementation for BotanicalNameParser.
	// not yet complete
	/**
	 * Returns the rank identified through a name (abbreviated or not).
	 * Preliminary implementation for BotanicalNameParser.
	 * 
	 * @param	strRank	the string identifying the rank
	 * @param   nc      the nomenclatural code
	 * @param 	useUnknown 	if true the rank UNKNOWN_RANK is returned if the abbrev is 
	 * 			unknown or not yet implemented
	 * @return  		the rank
	 */
	public static Rank getRankByNameOrAbbreviation(String strRank, NomenclaturalCode nc, boolean useUnknown)
			throws UnknownCdmTypeException{
		try {
			return getRankByAbbreviation(strRank, nc);
		} catch (UnknownCdmTypeException e) {
			return getRankByName(strRank, nc, useUnknown);
		}
	}
	
	/**
	 * Returns the rank identified through an abbreviated name.
	 * Preliminary implementation for BotanicalNameParser.
	 * 
	 * @param	abbrev	the string for the name abbreviation
	 * @return  		the rank
	 */
	public static Rank getRankByAbbreviation(String abbrev) 
						throws UnknownCdmTypeException{
		return getRankByAbbreviation(abbrev, false);
	}
	
	/**
	 * Returns the rank identified through an abbreviated name for a given nomenclatural code.
	 * Preliminary implementation.
	 * 
	 * @param	abbrev	the string for the name abbreviation
	 * @param	nc	    the nomenclatural code
	 * @return  		the rank
	 */
	public static Rank getRankByAbbreviation(String abbrev, NomenclaturalCode nc) 
	throws UnknownCdmTypeException{
		return getRankByAbbreviation(abbrev, nc, false);
	}
	
	// TODO
	// Preliminary implementation for BotanicalNameParser.
	// not yet complete
	/**
	 * Returns the rank identified through an abbreviated name.
	 * Preliminary implementation for BotanicalNameParser.
	 * 
	 * @param	abbrev		the string for the name abbreviation
	 * @param 	useUnknown 	if true the rank UNKNOWN_RANK is returned if the abbrev is 
	 * 			unknown or not yet implemented
	 * @return  the rank
	 */
	public static Rank getRankByAbbreviation(String abbrev, boolean useUnknown) 
						throws UnknownCdmTypeException{
		if (abbrev == null){ throw new NullPointerException("abbrev is 'null' in getRankByAbbreviation");
		}else if (abbrev.equalsIgnoreCase("reg.")){	return Rank.KINGDOM();
		}else if (abbrev.equalsIgnoreCase("subreg.")){ return Rank.SUBKINGDOM();
		}else if (abbrev.equalsIgnoreCase("phyl.")){return Rank.PHYLUM();
		}else if (abbrev.equalsIgnoreCase("subphyl.")) { return Rank.SUBPHYLUM();
		}else if (abbrev.equalsIgnoreCase("div.")) { return Rank.DIVISION();
		}else if (abbrev.equalsIgnoreCase("subdiv.")) { return Rank.SUBDIVISION();
		}else if (abbrev.equalsIgnoreCase("cl.")) { return Rank.CLASS();
		}else if (abbrev.equalsIgnoreCase("subcl.")) { return Rank.SUBCLASS();
		}else if (abbrev.equalsIgnoreCase("superor.")) { return Rank.SUPERORDER();
		}else if (abbrev.equalsIgnoreCase("ordo")) { return Rank.ORDER();
		}else if (abbrev.equalsIgnoreCase("subor.")) { return Rank.SUBORDER();
		}else if (abbrev.equalsIgnoreCase("fam.")) { return Rank.FAMILY();
		}else if (abbrev.equalsIgnoreCase("subfam.")) { return Rank.SUBFAMILY();
		}else if (abbrev.equalsIgnoreCase("trib.")) { return Rank.TRIBE();
		}else if (abbrev.equalsIgnoreCase("subtrib.")) { return Rank.SUBTRIBE();
		}else if (abbrev.equalsIgnoreCase("gen.")) { return Rank.GENUS();
		}else if (abbrev.equalsIgnoreCase("subg.")) { return Rank.SUBGENUS();
		}else if (abbrev.equalsIgnoreCase("sect.")) { return Rank.SECTION_BOTANY();
		}else if (abbrev.equalsIgnoreCase("subsect.")) { return Rank.SUBSECTION_BOTANY();
		}else if (abbrev.equalsIgnoreCase("ser.")) { return Rank.SERIES();
		}else if (abbrev.equalsIgnoreCase("subser.")) { return Rank.SUBSERIES();
		}else if (abbrev.equalsIgnoreCase("aggr.")) { return Rank.SPECIESAGGREGATE();
		}else if (abbrev.equalsIgnoreCase("group")) { return Rank.SPECIESGROUP();
		}else if (abbrev.equalsIgnoreCase("sp.")) { return Rank.SPECIES();
		}else if (abbrev.equalsIgnoreCase("subsp.")) { return Rank.SUBSPECIES();
		}else if (abbrev.equalsIgnoreCase("convar.")) { return Rank.CONVAR();
		}else if (abbrev.equalsIgnoreCase("var.")) { return Rank.VARIETY();
		}else if (abbrev.equalsIgnoreCase("subvar.")) { return Rank.SUBVARIETY();
		}else if (abbrev.equalsIgnoreCase("f.")) { return Rank.FORM();
		}else if (abbrev.equalsIgnoreCase("subf.")) { return Rank.SUBFORM();
		//TODO
		//}else if (abbrev.equalsIgnoreCase("f.spec.")) { return Rank.FORMA_SPEC();
		}else if (abbrev.equalsIgnoreCase("t.infgen.")) { return Rank.INFRAGENERICTAXON();
		}else if (abbrev.equalsIgnoreCase("t.infr.")) { return Rank.INFRASPECIFICTAXON();
		}else { 
			if (abbrev == null){
				abbrev = "(null)";
			}
			if (useUnknown){
				logger.info("Unknown rank name: " + abbrev+". Rank 'UNKNOWN_RANK' created instead");
				return Rank.UNKNOWN_RANK();
			}else{
				if (abbrev == null){
					abbrev = "(null)";
				}
				throw new UnknownCdmTypeException("Unknown rank abbreviation: " + abbrev);
			}
		}
	}
	
	// TODO
	// Preliminary implementation to cover Botany and Zoology.
	/**
	 * Returns the rank identified through an abbreviated name for a given nomenclatural code.
	 * Preliminary implementation for ICBN and ICZN.
	 * 
	 * @param	abbrev		the string for the name abbreviation
	 * @param	nc	        the nomenclatural code
	 * @param 	useUnknown 	if true the rank UNKNOWN_RANK is returned if the abbrev is 
	 * 			unknown or not yet implemented
	 * @return  the rank
	 */
	public static Rank getRankByAbbreviation(String abbrev, NomenclaturalCode nc,  boolean useUnknown) 
	throws UnknownCdmTypeException{

		if (nc.equals(NomenclaturalCode.ICZN)) {
			if (abbrev.equalsIgnoreCase("sect.")) { return Rank.SECTION_ZOOLOGY();
			} else if (abbrev.equalsIgnoreCase("subsect.")) { return Rank.SUBSECTION_ZOOLOGY();
			}
		}
		return getRankByAbbreviation(abbrev, useUnknown);
	}
	
	// TODO
	// Preliminary implementation for BotanicalNameParser.
	// not yet complete
	/**
	 * Returns the rank identified through a name.
	 * Preliminary implementation for BotanicalNameParser.
	 * 
	 * @param	rankName	the string for the name of the rank
	 * @return  			the rank
	 */
	public static Rank getRankByName(String rankName) throws UnknownCdmTypeException{
		return getRankByName(rankName, false);
	}
	

	// TODO
	// Preliminary implementation for ICBN and ICZN.
	// not yet complete
	/**
	 * Returns the rank identified through a name for a given nomenclatural code.
	 * Preliminary implementation for ICBN and ICZN.
	 * 
	 * @param	rankName	the string for the name of the rank
	 * @param	nc	        the nomenclatural code
	 * @return  			the rank
	 */
	public static Rank getRankByName(String rankName, NomenclaturalCode nc) throws UnknownCdmTypeException{
		return getRankByName(rankName, nc, false);
	}

	/**
	 * Returns the rank identified through a name.
	 * Preliminary implementation for BotanicalNameParser.
	 * 
	 * @param	rankName	the string for the name of the rank
	 * @param 	useUnknown 	if true the rank UNKNOWN_RANK is returned if the rank name is 
	 * 			unknown or not yet implemented
	 * @return  			the rank
	 */
	public static Rank getRankByName(String rankName, boolean useUnknown)
			throws UnknownCdmTypeException{
		if (rankName.equalsIgnoreCase("Regnum")){ return Rank.KINGDOM();
		}else if (rankName.equalsIgnoreCase("Subregnum")){ return Rank.SUBKINGDOM();
		}else if (rankName.equalsIgnoreCase("Phylum")){ return Rank.PHYLUM();
		}else if (rankName.equalsIgnoreCase("Subphylum")){ return Rank.SUBPHYLUM();
		}else if (rankName.equalsIgnoreCase("Divisio")){ return Rank.DIVISION();
		}else if (rankName.equalsIgnoreCase("Subdivisio")){ return Rank.SUBDIVISION();
		}else if (rankName.equalsIgnoreCase("Classis")){ return Rank.CLASS();
		}else if (rankName.equalsIgnoreCase("Subclassis")){ return Rank.SUBCLASS();
		}else if (rankName.equalsIgnoreCase("Superordo")){ return Rank.SUPERORDER();
		}else if (rankName.equalsIgnoreCase("Ordo")){ return Rank.ORDER();
		}else if (rankName.equalsIgnoreCase("Subordo")){ return Rank.SUBORDER();
		}else if (rankName.equalsIgnoreCase("Familia")){ return Rank.FAMILY();
		}else if (rankName.equalsIgnoreCase("Subfamilia")){ return Rank.SUBFAMILY();
		}else if (rankName.equalsIgnoreCase("Tribus")){ return Rank.TRIBE();
		}else if (rankName.equalsIgnoreCase("Subtribus")){ return Rank.SUBTRIBE();
		}else if (rankName.equalsIgnoreCase("Genus")){ return Rank.GENUS();
		}else if (rankName.equalsIgnoreCase("Subgenus")){ return Rank.SUBGENUS();
		}else if (rankName.equalsIgnoreCase("Sectio")){ return Rank.SECTION_BOTANY();
		}else if (rankName.equalsIgnoreCase("Subsectio")){ return Rank.SUBSECTION_BOTANY();
		}else if (rankName.equalsIgnoreCase("Series")){ return Rank.SERIES();
		}else if (rankName.equalsIgnoreCase("Subseries")){ return Rank.SUBSERIES();
		}else if (rankName.equalsIgnoreCase("Aggregate")){ return Rank.SPECIESAGGREGATE();
		}else if (rankName.equalsIgnoreCase("Speciesgroup")){ return Rank.SPECIESGROUP();
		}else if (rankName.equalsIgnoreCase("Species")){ return Rank.SPECIES();
		}else if (rankName.equalsIgnoreCase("Subspecies")){ return Rank.SUBSPECIES();
		}else if (rankName.equalsIgnoreCase("Convarietas")){ return Rank.CONVAR();
		}else if (rankName.equalsIgnoreCase("Varietas")){ return Rank.VARIETY();
		}else if (rankName.equalsIgnoreCase("Subvarietas")){ return Rank.SUBVARIETY();
		}else if (rankName.equalsIgnoreCase("Forma")){ return Rank.FORM();
		}else if (rankName.equalsIgnoreCase("Subforma")){ return Rank.SUBFORM();
		}else if (rankName.equalsIgnoreCase("Forma spec.")){ return Rank.SPECIALFORM();
		}else if (rankName.equalsIgnoreCase("tax.infragen.")){ return Rank.INFRAGENERICTAXON();
		}else if (rankName.equalsIgnoreCase("tax.infrasp.")){ return Rank.INFRASPECIFICTAXON();
		// old ranks 
		}else if (rankName.equalsIgnoreCase("proles")){ return Rank.INFRASPECIFICTAXON(); //to create the name put prol. and the infraspeciesepi to the field unnamed namephrase
		}else if (rankName.equalsIgnoreCase("race")){ return Rank.INFRASPECIFICTAXON(); //to create the name put prol. and the infraspeciesepi to the field unnamed namephrase
		}else if (rankName.equalsIgnoreCase("taxon")){ return Rank.INFRASPECIFICTAXON(); //to create the name put prol. and the infraspeciesepi to the field unnamed namephrase
		}else if (rankName.equalsIgnoreCase("sublusus")){ return Rank.INFRASPECIFICTAXON(); //to create the name put prol. and the infraspeciesepi to the field unnamed namephrase
		
		}else{ 
			if (rankName == null){
				rankName = "(null)";
			}
			if (useUnknown){
				logger.info("Unknown rank name: " + rankName+". Rank 'UNKNOWN_RANK' created instead");
				return Rank.UNKNOWN_RANK();
			}else{
				if (rankName == null){
					rankName = "(null)";
				}
				throw new UnknownCdmTypeException("Unknown rank name: " + rankName);
			}
		}
	}
	
	public static Rank getRankByEnglishName(String rankName, NomenclaturalCode nc, boolean useUnknown)
				throws UnknownCdmTypeException{
		if (rankName.equalsIgnoreCase("Kingdom")){ return Rank.KINGDOM();
		}else if (rankName.equalsIgnoreCase("Subkingdom")){ return Rank.SUBKINGDOM();
		}else if (rankName.equalsIgnoreCase("Infrakingdom")){ return Rank.INFRAKINGDOM();
		}else if (rankName.equalsIgnoreCase("Division")){ return Rank.DIVISION();
		}else if (rankName.equalsIgnoreCase("Phylum")){ return Rank.PHYLUM();
		}else if (rankName.equalsIgnoreCase("Subdivision")){ return Rank.SUBDIVISION();
		}else if (rankName.equalsIgnoreCase("Subphylum")){ return Rank.SUBPHYLUM();
		}else if (rankName.equalsIgnoreCase("Superclass")){ return Rank.SUPERCLASS();
		}else if (rankName.equalsIgnoreCase("Class")){ return Rank.CLASS();
		}else if (rankName.equalsIgnoreCase("Subclass")){ return Rank.SUBCLASS();
		}else if (rankName.equalsIgnoreCase("Infraclass")){ return Rank.INFRACLASS();
		}else if (rankName.equalsIgnoreCase("Superorder")){ return Rank.SUPERORDER();
		}else if (rankName.equalsIgnoreCase("Order")){ return Rank.ORDER();
		}else if (rankName.equalsIgnoreCase("Suborder")){ return Rank.SUBORDER();
		}else if (rankName.equalsIgnoreCase("Infraorder")){ return Rank.INFRAORDER();
		}else if (rankName.equalsIgnoreCase("Section")){ return Rank.SECTION_ZOOLOGY();
		//(Sub-)Sectio
		}else if (rankName.equalsIgnoreCase("Section")){ 
			if (nc != null && nc.equals(NomenclaturalCode.ICZN)){	return Rank.SECTION_ZOOLOGY;
			}else if (nc != null && nc.equals(NomenclaturalCode.ICBN)){return Rank.SECTION_BOTANY;
			}else{
				String errorWarning = "Section is only defined for ICZN and ICBN at the moment but here needed for " + ((nc == null)? "(null)": nc.toString());
				logger.warn(errorWarning);
				throw new UnknownCdmTypeException (errorWarning);
			}
		}else if (rankName.equalsIgnoreCase("Subsection")){ 
			if (nc != null && nc.equals(NomenclaturalCode.ICZN)){ return Rank.SECTION_ZOOLOGY;
			}else if (nc != null && nc.equals(NomenclaturalCode.ICBN)){ return Rank.SECTION_BOTANY;
			}else{
				String errorWarning = "Subsection is only defined for ICZN and ICBN at the moment but here needed for " + ((nc == null)? "(null)": nc.toString());
				logger.warn(errorWarning);
				throw new UnknownCdmTypeException (errorWarning);
			}
		}else if (rankName.equalsIgnoreCase("Superfamily")){ return Rank.SUPERFAMILY();
		}else if (rankName.equalsIgnoreCase("Family")){ return Rank.FAMILY();
		}else if (rankName.equalsIgnoreCase("Subfamily")){ return Rank.SUBFAMILY();
		}else if (rankName.equalsIgnoreCase("Tribe")){ return Rank.TRIBE();
		}else if (rankName.equalsIgnoreCase("Subtribe")){ return Rank.SUBTRIBE();
		}else if (rankName.equalsIgnoreCase("Genus")){ return Rank.GENUS();
		}else if (rankName.equalsIgnoreCase("Subgenus")){ return Rank.SUBGENUS();
		}else if (rankName.equalsIgnoreCase("Species")){ return Rank.SPECIES();
		}else if (rankName.equalsIgnoreCase("Subspecies")){ return Rank.SUBSPECIES();
		//Natio
//		}else if (rankName.equalsIgnoreCase("Natio")){ return Rank.NATIO();
		
		}else if (rankName.equalsIgnoreCase("Variety")){ return Rank.VARIETY();
		}else if (rankName.equalsIgnoreCase("Subvariety")){ return Rank.SUBVARIETY();
		
		}else if (rankName.equalsIgnoreCase("Forma")){ return Rank.FORM();
		}else if (rankName.equalsIgnoreCase("Subforma")){ return Rank.SUBFORM();
		
		}else{ 
			if (rankName == null){
				rankName = "(null)";
			}
			if (useUnknown){
				logger.info("Unknown rank name: " + rankName+". Rank 'UNKNOWN_RANK' created instead");
				return Rank.UNKNOWN_RANK();
			}else{
				if (rankName == null){
					rankName = "(null)";
				}
				throw new UnknownCdmTypeException("Unknown rank name: " + rankName);
			}
		}
	}
	
	public static Rank getRankByName(String rankName, NomenclaturalCode nc, boolean useUnknown)
	throws UnknownCdmTypeException {
		
	if (nc.equals(NomenclaturalCode.ICZN)) {
		if (rankName.equalsIgnoreCase("Sectio")) { return Rank.SECTION_ZOOLOGY();
			}else if (rankName.equalsIgnoreCase("Subsectio")) { return Rank.SUBSECTION_ZOOLOGY();
		}
	}
	return getRankByName(rankName, useUnknown);
	}
	
	//TODO
	//dummy implementation for BerlinModelImport
	// not yet complete
	/**
	 * Returns the abbreviated rank name for <i>this</i> rank according to the
	 * Berlin Model. Preliminary implementation for BerlinModelImport.
	 * 
	 * @return	the abbreviation string for <i>this</i> rank
	 */
	public String getAbbreviation(){
		if (this.equals(Rank.ORDER()) ){return "ordo";}
		if (this.equals(Rank.FAMILY()) ){return "fam.";}
		else if (this.equals(Rank.SUBFAMILY()) ){return "subfam.";}
		else if (this.equals(Rank.TRIBE()) ){return "trib.";}
		else if (this.equals(Rank.SUBTRIBE()) ){return "subtrib.";}
		else if (this.equals(Rank.GENUS()) ){return "gen.";}
		else if (this.equals(Rank.SUBGENUS()) ){return "subg.";}
		else if (this.equals(Rank.SECTION_BOTANY()) ){return "sect.";}
		else if (this.equals(Rank.SUBSECTION_BOTANY()) ){return "subsect.";}
		else if (this.equals(Rank.SERIES()) ){return "ser.";}
		//else if (this.equals(Rank.AGGREGATE()) ){return "aggr.";}
		else if (this.equals(Rank.SPECIES()) ){return "sp.";}
		else if (this.equals(Rank.SUBSPECIES()) ){return "subsp.";}
		else if (this.equals(Rank.VARIETY()) ){return "var.";}
		else if (this.equals(Rank.CONVAR()) ){return "convar.";}
		else if (this.equals(Rank.SUBVARIETY()) ){return "subvar.";}
		else if (this.equals(Rank.FORM()) ){return "f.";}
		else if (this.equals(Rank.SPECIALFORM()) ){return "f.spec.";}
		else if (this.equals(Rank.INFRAGENERICTAXON()) ){return "t.infgen.";}
		else if (this.equals(Rank.INFRASPECIFICTAXON()) ){return "t.infr.";}
		else {
			logger.warn("Abbreviation for this Rank " + this.toString() +  " not yet implemented");
			return "xxx.";
		}
	}
	@Transient
	public String getInfraGenericMarker() throws UnknownCdmTypeException{
		if (! this.isInfraGeneric()){
			throw new IllegalStateException("An infrageneric marker is only available for a infrageneric rank but was asked for rank: " + this.toString());
		}else if (this.equals(Rank.SUBGENUS())){
			return "subg.";
		}else if (this.equals(Rank.INFRAGENUS())){
			return "infrag.";  //??
		}else if (this.equals(Rank.SECTION_BOTANY())){ 
			return "sect.";
		}else if (this.equals(Rank.SUBSECTION_BOTANY())){
			return "subsect.";
		}else if (this.equals(Rank.SERIES())){
			return "ser.";
		}else if (this.equals(Rank.SUBSERIES())){
			return "subser.";
		}else if (this.equals(Rank.SPECIESAGGREGATE())){
			return "aggr.";
		}else if (this.equals(Rank.SPECIESGROUP())){
			return "group";
		}else {
			throw new UnknownCdmTypeException("Abbreviation for rank unknown: " + this.toString());
		}
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<Rank> termVocabulary) {
		Rank.BIOVARIETY = termVocabulary.findTermByUuid(Rank.uuidBioVariety);
		Rank.CANDIDATE = termVocabulary.findTermByUuid(Rank.uuidCandidate);
		Rank.CLASS = termVocabulary.findTermByUuid(Rank.uuidClass);
		Rank.CONVAR = termVocabulary.findTermByUuid(Rank.uuidConvar);
		Rank.CULTIVAR = termVocabulary.findTermByUuid(Rank.uuidCultivar);
		Rank.CULTIVARGROUP = termVocabulary.findTermByUuid(Rank.uuidCultivarGroup);
		Rank.DENOMINATIONCLASS = termVocabulary.findTermByUuid(Rank.uuidDenominationClass);
		Rank.DIVISION = termVocabulary.findTermByUuid(Rank.uuidDivision);
		Rank.DOMAIN = termVocabulary.findTermByUuid(Rank.uuidDomain);
		Rank.EMPIRE = termVocabulary.findTermByUuid(Rank.uuidEmpire);
		Rank.FAMILY = termVocabulary.findTermByUuid(Rank.uuidFamily);
		Rank.FORM = termVocabulary.findTermByUuid(Rank.uuidForm);
		Rank.GENUS = termVocabulary.findTermByUuid(Rank.uuidGenus);
		Rank.GRAFTCHIMAERA = termVocabulary.findTermByUuid(Rank.uuidGraftChimaera);
		Rank.GREX = termVocabulary.findTermByUuid(Rank.uuidGrex);
		Rank.INFRACLASS = termVocabulary.findTermByUuid(Rank.uuidInfraclass);
		Rank.INFRADIVISION = termVocabulary.findTermByUuid(Rank.uuidInfradivision);
		Rank.INFRAFAMILY = termVocabulary.findTermByUuid(Rank.uuidInfrafamily);
		Rank.INFRAGENERICTAXON = termVocabulary.findTermByUuid(Rank.uuidInfragenericTaxon);
		Rank.INFRAGENUS = termVocabulary.findTermByUuid(Rank.uuidInfragenus);
		Rank.INFRAKINGDOM = termVocabulary.findTermByUuid(Rank.uuidInfrakingdom);
		Rank.INFRAORDER = termVocabulary.findTermByUuid(Rank.uuidInfraorder);
		Rank.INFRAPHYLUM = termVocabulary.findTermByUuid(Rank.uuidInfraphylum);
		Rank.INFRASPECIES = termVocabulary.findTermByUuid(Rank.uuidInfraspecies);
		Rank.INFRASPECIFICTAXON = termVocabulary.findTermByUuid(Rank.uuidInfraspecificTaxon);
		Rank.INFRATRIBE = termVocabulary.findTermByUuid(Rank.uuidInfratribe);
		Rank.KINGDOM = termVocabulary.findTermByUuid(Rank.uuidKingdom);
		Rank.ORDER = termVocabulary.findTermByUuid(Rank.uuidOrder);
		Rank.PATHOVARIETY = termVocabulary.findTermByUuid(Rank.uuidPathoVariety);
		Rank.PHYLUM = termVocabulary.findTermByUuid(Rank.uuidPhylum);
		Rank.SECTION_BOTANY = termVocabulary.findTermByUuid(Rank.uuidSectionBotany);
		Rank.SECTION_ZOOLOGY = termVocabulary.findTermByUuid(Rank.uuidSectionZoology);
		Rank.SERIES = termVocabulary.findTermByUuid(Rank.uuidSeries);
		Rank.SPECIALFORM = termVocabulary.findTermByUuid(Rank.uuidSpecialForm);
		Rank.SPECIES = termVocabulary.findTermByUuid(Rank.uuidSpecies);
		Rank.SPECIESAGGREGATE = termVocabulary.findTermByUuid(Rank.uuidSpeciesAggregate);
		Rank.SPECIESGROUP = termVocabulary.findTermByUuid(Rank.uuidSpeciesGroup);
		Rank.SUBCLASS = termVocabulary.findTermByUuid(Rank.uuidSubclass);
		Rank.SUBDIVISION = termVocabulary.findTermByUuid(Rank.uuidSubdivision);
		Rank.SUBFAMILY = termVocabulary.findTermByUuid(Rank.uuidSubfamily);
		Rank.SUBFORM = termVocabulary.findTermByUuid(Rank.uuidSubform);
		Rank.SUBGENUS = termVocabulary.findTermByUuid(Rank.uuidSubgenus);
		Rank.SUBKINGDOM = termVocabulary.findTermByUuid(Rank.uuidSubkingdom);
		Rank.SUBORDER = termVocabulary.findTermByUuid(Rank.uuidSuborder);
		Rank.SUBPHYLUM = termVocabulary.findTermByUuid(Rank.uuidSubphylum);
		Rank.SUBSECTION_BOTANY = termVocabulary.findTermByUuid(Rank.uuidSubsectionBotany);
		Rank.SUBSECTION_ZOOLOGY = termVocabulary.findTermByUuid(Rank.uuidSubsectionZoology);
		Rank.SUBSERIES = termVocabulary.findTermByUuid(Rank.uuidSubseries);
		Rank.SUBSPECIES = termVocabulary.findTermByUuid(Rank.uuidSubspecies);
		Rank.SUBSPECIFICAGGREGATE = termVocabulary.findTermByUuid(Rank.uuidSubspecificAggregate);
		Rank.SUBSUBFORM = termVocabulary.findTermByUuid(Rank.uuidSubsubform);
		Rank.SUBSUBVARIETY = termVocabulary.findTermByUuid(Rank.uuidSubsubvariety);
		Rank.SUBTRIBE = termVocabulary.findTermByUuid(Rank.uuidSubtribe);
		Rank.SUBVARIETY = termVocabulary.findTermByUuid(Rank.uuidSubvariety);
		Rank.SUPERCLASS = termVocabulary.findTermByUuid(Rank.uuidSuperclass);
		Rank.SUPERDIVISION = termVocabulary.findTermByUuid(Rank.uuidSuperdivision);
		Rank.SUPERFAMILY = termVocabulary.findTermByUuid(Rank.uuidSuperfamily);
		Rank.SUPERKINGDOM = termVocabulary.findTermByUuid(Rank.uuidSuperkingdom);
		Rank.SUPERORDER = termVocabulary.findTermByUuid(Rank.uuidSuperorder);
		Rank.SUPERPHYLUM = termVocabulary.findTermByUuid(Rank.uuidSuperphylum);
		Rank.SUPERTRIBE = termVocabulary.findTermByUuid(Rank.uuidSupertribe);
		Rank.SUPRAGENERICTAXON = termVocabulary.findTermByUuid(Rank.uuidSupragenericTaxon);
		Rank.TRIBE = termVocabulary.findTermByUuid(Rank.uuidTribe);
		Rank.UNKNOWN_RANK = termVocabulary.findTermByUuid(Rank.uuidUnknownRank);
		Rank.VARIETY = termVocabulary.findTermByUuid(Rank.uuidVariety);
	}
}