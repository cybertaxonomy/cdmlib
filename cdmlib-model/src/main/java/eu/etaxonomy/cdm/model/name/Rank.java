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
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

import org.apache.log4j.Logger;

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
public class Rank extends OrderedTermBase<Rank> {
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
	private static final UUID uuidSection = UUID.fromString("3edff68f-8527-49b5-bf91-7e4398bb975c");
	private static final UUID uuidSubsection = UUID.fromString("d20f5b61-d463-4448-8f8a-c1ff1f262f59");
	private static final UUID uuidSeries = UUID.fromString("d7381ecf-48f8-429b-9c54-f461656978cd");
	private static final UUID uuidSubseries = UUID.fromString("80c9a263-f4db-4a13-b6c2-b7fec1aa1200");
	private static final UUID uuidSpeciesAggregate = UUID.fromString("1ecae058-4217-4f75-9c27-6d8ba099ac7a");
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
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty rank instance.
	 * 
	 * @see 	#Rank(String, String, String)
	 */
	public Rank() {
			super();
	}

	/** 
	 * Class constructor: creates an additional rank instance with a description
	 * (in the {@link common.Language#DEFAULT() default language}), a label and a label abbreviation.
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

	//********* METHODS **************************************/

	/**
	 * Returns the taxonomical rank identified through its immutable universally
	 * unique identifier (UUID).
	 * 
	 * @param	uuid	the universally unique identifier
	 * @return  		the rank corresponding to the given universally unique
	 * 					identifier
	 */
	public static final Rank getByUuid(UUID uuid){
		return (Rank)   findByUuid(uuid);
	}
	
	public static final Rank EMPIRE(){
	  return getByUuid(uuidEmpire);
	}
	public static final Rank DOMAIN(){
	  return getByUuid(uuidDomain);
	}
	public static final Rank SUPERKINGDOM(){
	  return getByUuid(uuidSuperkingdom);
	}
	public static final Rank KINGDOM(){
	  return getByUuid(uuidKingdom);
	}
	public static final Rank SUBKINGDOM(){
	  return getByUuid(uuidSubkingdom);
	}
	public static final Rank INFRAKINGDOM(){
	  return getByUuid(uuidInfrakingdom);
	}
	public static final Rank SUPERPHYLUM(){
	  return getByUuid(uuidSuperphylum);
	}
	public static final Rank PHYLUM(){
	  return getByUuid(uuidPhylum);
	}
	public static final Rank SUBPHYLUM(){
	  return getByUuid(uuidSubphylum);
	}
	public static final Rank INFRAPHYLUM(){
	  return getByUuid(uuidInfraphylum);
	}
	public static final Rank SUPERDIVISION(){
	  return getByUuid(uuidSuperdivision);
	}
	public static final Rank DIVISION(){
	  return getByUuid(uuidDivision);
	}
	public static final Rank SUBDIVISION(){
	  return getByUuid(uuidSubdivision);
	}
	public static final Rank INFRADIVISION(){
	  return getByUuid(uuidInfradivision);
	}
	public static final Rank SUPERCLASS(){
	  return getByUuid(uuidSuperclass);
	}
	public static final Rank CLASS(){
	  return getByUuid(uuidClass);
	}
	public static final Rank SUBCLASS(){
	  return getByUuid(uuidSubclass);
	}
	public static final Rank INFRACLASS(){
	  return getByUuid(uuidInfraclass);
	}
	public static final Rank SUPERORDER(){
	  return getByUuid(uuidSuperorder);
	}
	public static final Rank ORDER(){
	  return getByUuid(uuidOrder);
	}
	public static final Rank SUBORDER(){
	  return getByUuid(uuidSuborder);
	}
	public static final Rank INFRAORDER(){
	  return getByUuid(uuidInfraorder);
	}
	public static final Rank SUPERFAMILY(){
	  return getByUuid(uuidSuperfamily);
	}
	public static final Rank FAMILY(){
	  return getByUuid(uuidFamily);
	}
	public static final Rank SUBFAMILY(){
	  return getByUuid(uuidSubfamily);
	}
	public static final Rank INFRAFAMILY(){
	  return getByUuid(uuidInfrafamily);
	}
	public static final Rank SUPERTRIBE(){
	  return getByUuid(uuidSupertribe);
	}
	public static final Rank TRIBE(){
	  return getByUuid(uuidTribe);
	}
	public static final Rank SUBTRIBE(){
	  return getByUuid(uuidSubtribe);
	}
	public static final Rank INFRATRIBE(){
	  return getByUuid(uuidInfratribe);
	}
	public static final Rank SUPRAGENERICTAXON(){
	  return getByUuid(uuidSupragenericTaxon);
	}
	public static final Rank GENUS(){
	  return getByUuid(uuidGenus);
	}
	public static final Rank SUBGENUS(){
	  return getByUuid(uuidSubgenus);
	}
	public static final Rank INFRAGENUS(){
	  return getByUuid(uuidInfragenus);
	}
	public static final Rank SECTION(){
	  return getByUuid(uuidSection);
	}
	public static final Rank SUBSECTION(){
	  return getByUuid(uuidSubsection);
	}
	public static final Rank SERIES(){
	  return getByUuid(uuidSeries);
	}
	public static final Rank SUBSERIES(){
	  return getByUuid(uuidSubseries);
	}
	public static final Rank SPECIESAGGREGATE(){
	  return getByUuid(uuidSpeciesAggregate);
	}
	public static final Rank INFRAGENERICTAXON(){
	  return getByUuid(uuidInfragenericTaxon);
	}
	public static final Rank SPECIES(){
	  return getByUuid(uuidSpecies);
	}
	public static final Rank SUBSPECIFICAGGREGATE(){
	  return getByUuid(uuidSubspecificAggregate);
	}
	public static final Rank SUBSPECIES(){
	  return getByUuid(uuidSubspecies);
	}
	public static final Rank INFRASPECIES(){
	  return getByUuid(uuidInfraspecies);
	}
	public static final Rank VARIETY(){
	  return getByUuid(uuidVariety);
	}
	public static final Rank BIOVARIETY(){
	  return getByUuid(uuidBioVariety);
	}
	public static final Rank PATHOVARIETY(){
	  return getByUuid(uuidPathoVariety);
	}
	public static final Rank SUBVARIETY(){
	  return getByUuid(uuidSubvariety);
	}
	public static final Rank SUBSUBVARIETY(){
	  return getByUuid(uuidSubsubvariety);
	}
	public static final Rank CONVAR(){
	  return getByUuid(uuidConvar);
	}
	public static final Rank FORM(){
	  return getByUuid(uuidForm);
	}
	public static final Rank SPECIALFORM(){
	  return getByUuid(uuidSpecialForm);
	}
	public static final Rank SUBFORM(){
	  return getByUuid(uuidSubform);
	}
	public static final Rank SUBSUBFORM(){
	  return getByUuid(uuidSubsubform);
	}
	public static final Rank INFRASPECIFICTAXON(){
	  return getByUuid(uuidInfraspecificTaxon);
	}
	public static final Rank CANDIDATE(){
	  return getByUuid(uuidCandidate);
	}
	public static final Rank DENOMINATIONCLASS(){
	  return getByUuid(uuidDenominationClass);
	}
	public static final Rank GREX(){
	  return getByUuid(uuidGrex);
	}
	public static final Rank GRAFTCHIMAERA(){
	  return getByUuid(uuidGraftChimaera);
	}
	public static final Rank CULTIVARGROUP(){
	  return getByUuid(uuidCultivarGroup);
	}
	public static final Rank CULTIVAR(){
	  return getByUuid(uuidCultivar);
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
	 * NOT YET COMPLETE !!
	 * @param strRank
	 * @return Rank
	 * @throws UnknownCdmTypeException
	 */
	@Transient
	public static Rank getRankByNameOrAbbreviation(String strRank)
			throws UnknownCdmTypeException{
		try {
			return getRankByAbbreviation(strRank);
		} catch (UnknownCdmTypeException e) {
			return getRankByName(strRank);
		}
	}
	
	
	/** TODO
	 * preliminary implementation for BotanicalNameParser
	 *  * not yet complete
	 */
	@Transient
	public static Rank getRankByAbbreviation(String abbrev) throws UnknownCdmTypeException{
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
		}else if (abbrev.equalsIgnoreCase("sect.")) { return Rank.SECTION();
		}else if (abbrev.equalsIgnoreCase("subsect.")) { return Rank.SUBSECTION();
		}else if (abbrev.equalsIgnoreCase("ser.")) { return Rank.SERIES();
		}else if (abbrev.equalsIgnoreCase("subser.")) { return Rank.SUBSERIES();
		}else if (abbrev.equalsIgnoreCase("aggr.")) { return Rank.SPECIESAGGREGATE();
		//TODO
		//}else if (abbrev.equalsIgnoreCase("group")) { return Rank.SPECIESGROUP();
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
			throw new UnknownCdmTypeException("Unknown rank abbreviation: " + abbrev);
		}
	}
	
	/** TODO
	 * preliminary implementation for BotanicalNameParser
	 * not yet complete
	 */
	@Transient
	public static Rank getRankByName(String rankName)
			throws UnknownCdmTypeException{
		if (rankName.equalsIgnoreCase("Regnum")){ return Rank.KINGDOM();
		}else if (rankName.equalsIgnoreCase("Subregnum")){ return Rank.SUBKINGDOM();
		}else if (rankName.equalsIgnoreCase("Phylum")){ return Rank.PHYLUM();
		}else if (rankName.equalsIgnoreCase("subphylum")){ return Rank.SUBPHYLUM();
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
		}else if (rankName.equalsIgnoreCase("Sectio")){ return Rank.SECTION();
		}else if (rankName.equalsIgnoreCase("Subsectio")){ return Rank.SUBSECTION();
		}else if (rankName.equalsIgnoreCase("Series")){ return Rank.SERIES();
		}else if (rankName.equalsIgnoreCase("Subseries")){ return Rank.SUBSERIES();
		}else if (rankName.equalsIgnoreCase("Aggregate")){ return Rank.SPECIESAGGREGATE();
		//TODO
		//}else if (rankName.equalsIgnoreCase("Speciesgroup")){ return Rank.SPECIESGROUP();
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
			throw new UnknownCdmTypeException("Unknown rank name: " + rankName);
		}
	}
	
	//TODO
	//dummy implementation for BerlinModelImport
	@Transient
	public String getAbbreviation(){
		if (this.equals(Rank.ORDER()) ){return "ordo";}
		if (this.equals(Rank.FAMILY()) ){return "fam.";}
		else if (this.equals(Rank.SUBFAMILY()) ){return "subfam.";}
		else if (this.equals(Rank.TRIBE()) ){return "trib.";}
		else if (this.equals(Rank.SUBTRIBE()) ){return "subtrib.";}
		else if (this.equals(Rank.GENUS()) ){return "gen.";}
		else if (this.equals(Rank.SUBGENUS()) ){return "subg.";}
		else if (this.equals(Rank.SECTION()) ){return "sect.";}
		else if (this.equals(Rank.SUBSECTION()) ){return "subsect.";}
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

	
	

}