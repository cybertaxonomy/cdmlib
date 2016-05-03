/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.tcsxml;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
/*import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.PersonalCommunication;
import eu.etaxonomy.cdm.model.reference.PrintSeries;*/
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
//import eu.etaxonomy.cdm.model.reference.WebPage;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

public final class TcsXmlTransformer {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TcsXmlTransformer.class);
 

	//TypeDesignation
	public static SpecimenTypeDesignationStatus typeStatusId2TypeStatus (int typeStatusId)  throws UnknownCdmTypeException{
		switch (typeStatusId){
			case 1: return SpecimenTypeDesignationStatus.HOLOTYPE();
			case 2: return SpecimenTypeDesignationStatus.LECTOTYPE();
			case 3: return SpecimenTypeDesignationStatus.NEOTYPE();
			case 4: return SpecimenTypeDesignationStatus.EPITYPE();
			case 5: return SpecimenTypeDesignationStatus.ISOLECTOTYPE();
			case 6: return SpecimenTypeDesignationStatus.ISONEOTYPE();
			case 7: return SpecimenTypeDesignationStatus.ISOTYPE();
			case 8: return SpecimenTypeDesignationStatus.PARANEOTYPE();
			case 9: return SpecimenTypeDesignationStatus.PARATYPE();
			case 10: return SpecimenTypeDesignationStatus.SECOND_STEP_LECTOTYPE();
			case 11: return SpecimenTypeDesignationStatus.SECOND_STEP_NEOTYPE();
			case 12: return SpecimenTypeDesignationStatus.SYNTYPE();
			case 21: return SpecimenTypeDesignationStatus.ICONOTYPE();
			case 22: return SpecimenTypeDesignationStatus.PHOTOTYPE();
			default: {
				throw new UnknownCdmTypeException("Unknown TypeDesignationStatus (id=" + Integer.valueOf(typeStatusId).toString() + ")");
			}
		}
	}
	
	
	
	
	/** Creates an cdm-Rank by the tcs rank
	 */
	public static Rank rankCode2Rank (String strRank) throws UnknownCdmTypeException{
		if (strRank == null){return null;
		//genus group
		}else if (strRank.equals("infragen")){return Rank.INFRAGENUS();
		}else if (strRank.equals("subgen")){return Rank.SUBGENUS();
		}else if (strRank.equals("gen")){return Rank.GENUS();
		//genus subdivision
		//TODO 
		}else if (strRank.equals("aggr")){return Rank.SPECIESAGGREGATE();
		}else if (strRank.equals("taxinfragen")){return Rank.INFRAGENERICTAXON();
		}else if (strRank.equals("subser")){return Rank.SUBSERIES();
		}else if (strRank.equals("ser")){return Rank.SERIES();
		}else if (strRank.equals("subsect")){return Rank.SUBSECTION_BOTANY();
		}else if (strRank.equals("sect")){return Rank.SECTION_BOTANY();
		//species group
		}else if (strRank.equals("subsp_aggr")){return Rank.SUBSPECIFICAGGREGATE();
		}else if (strRank.equals("ssp")){return Rank.SUBSPECIES();
		}else if (strRank.equals("sp")){return Rank.SPECIES();
		//below subspecies
		}else if (strRank.equals("cand")){return Rank.CANDIDATE();
		}else if (strRank.equals("taxinfrasp")){return Rank.INFRASPECIFICTAXON();
		}else if (strRank.equals("fsp")){return Rank.SPECIALFORM();
		}else if (strRank.equals("subsubfm")){return Rank.SUBSUBFORM();
		}else if (strRank.equals("subfm")){return Rank.SUBFORM();
		}else if (strRank.equals("fm")){return Rank.FORM();
		}else if (strRank.equals("subsubvar")){return Rank.SUBSUBVARIETY();
		}else if (strRank.equals("subvar")){return Rank.SUBVARIETY();
		}else if (strRank.equals("var")){return Rank.VARIETY();
		//TODO -> see documentation, Bacteria status
//		}else if (strRank.equals("pv")){return Rank;
//		}else if (strRank.equals("bv")){return Rank.;
		}else if (strRank.equals("infrasp")){return Rank.INFRASPECIES();
		//above superfamily
		}else if (strRank.equals("infraord")){return Rank.INFRAORDER();
		}else if (strRank.equals("ord")){return Rank.ORDER();
		}else if (strRank.equals("superord")){return Rank.SUPERORDER();
		}else if (strRank.equals("infracl")){return Rank.INFRACLASS();
		}else if (strRank.equals("subcl")){return Rank.SUBCLASS();
		}else if (strRank.equals("cl")){return Rank.CLASS();
		}else if (strRank.equals("supercl")){return Rank.SUPERCLASS();
		}else if (strRank.equals("infraphyl_div")){return Rank.INFRAPHYLUM();
		}else if (strRank.equals("subphyl_div")){return Rank.SUBPHYLUM();
		}else if (strRank.equals("phyl_div")){return Rank.PHYLUM();
		}else if (strRank.equals("superphyl_div")){return Rank.SUPERPHYLUM();
		}else if (strRank.equals("infrareg")){return Rank.INFRAKINGDOM();
		}else if (strRank.equals("subreg")){return Rank.SUBKINGDOM();
		}else if (strRank.equals("reg")){return Rank.KINGDOM();
		}else if (strRank.equals("superreg")){return Rank.SUPERKINGDOM();
		}else if (strRank.equals("dom")){return Rank.DOMAIN();
		}else if (strRank.equals("taxsupragen")){return Rank.SUPRAGENERICTAXON();
		//family group
		}else if (strRank.equals("infrafam")){return Rank.INFRAFAMILY();
		}else if (strRank.equals("subfam")){return Rank.SUBFAMILY();
		}else if (strRank.equals("fam")){return Rank.FAMILY();
		}else if (strRank.equals("superfam")){return Rank.SUPERFAMILY();
		//family subdivision
		}else if (strRank.equals("intratrib")){return Rank.TRIBE();
		}else if (strRank.equals("subtrib")){return Rank.SUBTRIBE();
		}else if (strRank.equals("trib")){return Rank.TRIBE();
		}else if (strRank.equals("supertrib")){return Rank.SUPERTRIBE();
		}	
		else {
			throw new UnknownCdmTypeException("Unknown Rank " + strRank);
		}
		
		
	}
	
	public static Rank rankString2Rank (String strRank) throws UnknownCdmTypeException{
		
		String tcsFamily = "family";
		String tcsSubFamily = "subfamily";
		String tcsTribe =  "tribe";
		String tcsSubtribe =  "subtribe";
		String tcsGenus =  "genus";
		String tcsSection = "section";
		String tcsSpecies =  "species";
		String tcsSubSpecies = "subspecies";
		String tcsVariety = "variety";
		String tcsSubVariety =  "subvariety";
		String tcsForm =  "form";

		
		String tcsAbbFamily = "fam.";
		String tcsAbbrSubFamily =  "subfam.";
		String tcsAbbrTribe =  "trib.";
		String tcsAbbrSubtribe =  "subtrib.";
		String tcsAbbrGenus =  "gen.";
		String tcsAbbrSubGenus =  "subgen.";
		String tcsAbbrSection = "sect.";
		String tcsAbbrSubSection = "subsect.";
		String tcsAbbrSeries = "ser.";
		String tcsAbbrSpecies =  "spec.";
		String tcsAbbrSubSpecies = "subsp.";
		String tcsAbbrVariety = "var.";
		String tcsAbbrSubVariety ="subvar.";
		String tcsAbbrForm = "f.";
		String tcsAbbrForma = "forma";
		String tcsAbbrSubForm = "subf.";
		String tcsAbbrInfraspecUnranked ="[infrasp.unranked]";
		String tcsAbbrInfragenUnranked ="[infragen.unranked]";
		String tcsAbbrNothoSubSpecies = "nothosubsp.";
		
		if (strRank == null){return null;
		}else{
			strRank = strRank.toLowerCase();
		}
		if (tcsFamily.equals(strRank) ){return Rank.FAMILY();
		}else if (tcsSubFamily.equals(strRank)){return Rank.SUBFAMILY();
		}else if (tcsTribe.equals(strRank)){return Rank.TRIBE();
		}else if (tcsSubtribe.equals(strRank)){return Rank.SUBTRIBE();
		}else if (tcsGenus.equals(strRank)){return Rank.GENUS();
		}else if (tcsSection.equals(strRank)){return Rank.SECTION_BOTANY();
		}else if (tcsSpecies.equals(strRank)){return Rank.SPECIES();
		}else if (tcsVariety.equals(strRank)){return Rank.VARIETY();
		}else if (tcsSubVariety.equals(strRank)){return Rank.SUBVARIETY();
		}else if (tcsSubSpecies.equals(strRank) ){return Rank.SUBSPECIES();
		}else if (tcsForm.equals(strRank)){return Rank.FORM();
		}else if (tcsAbbFamily.equals(strRank)){return Rank.FAMILY();
		}else if (tcsAbbrSubFamily.equals(strRank)){return Rank.SUBFAMILY();
		}else if (tcsAbbrTribe.equals(strRank)){return Rank.TRIBE();
		}else if (tcsAbbrSubtribe.equals(strRank)){return Rank.SUBTRIBE();
		}else if (tcsAbbrGenus.equals(strRank)){return Rank.GENUS();
		}else if (tcsAbbrSubGenus.equals(strRank)){return Rank.SUBGENUS();
		}else if (tcsAbbrSection.equals(strRank)){return Rank.SECTION_BOTANY();
		}else if (tcsAbbrSubSection.equals(strRank)){return Rank.SUBSECTION_BOTANY();
		}else if (tcsAbbrSeries.equals(strRank)){return Rank.SERIES();
		}else if (tcsAbbrSpecies.equals(strRank)){return Rank.SPECIES();
		}else if (tcsAbbrSubSpecies.equals(strRank) || tcsAbbrNothoSubSpecies.equals(strRank)){return Rank.SUBSPECIES();
		}else if (tcsAbbrVariety.equals(strRank)){return Rank.VARIETY();
		}else if (tcsAbbrSubVariety.equals(strRank)){return Rank.SUBVARIETY();
		}else if (tcsAbbrForm.equals(strRank) ||tcsAbbrForma.equals(strRank)){return Rank.FORM();
		}else if (tcsAbbrSubForm.equals(strRank)){return Rank.SUBFORM();
		}else if (tcsAbbrInfraspecUnranked.equals(strRank)){return Rank.UNRANKED_INFRASPECIFIC();
		}else if (tcsAbbrInfragenUnranked.equals(strRank)){return Rank.UNRANKED_INFRAGENERIC();
		}else{
			throw new UnknownCdmTypeException("Unknown Rank " + strRank);
		}
	}
	
	/** Creates an cdm-NomenclaturalCode by the tcs NomenclaturalCode
	 */
	public static NomenclaturalCode nomCodeString2NomCode (String nomCode) throws UnknownCdmTypeException{
		if (nomCode != null){
			nomCode = nomCode.trim();
		}
		if (nomCode == null){ return null;
		}else if (nomCode.equals("Botanical")){return NomenclaturalCode.ICNAFP;
		}else if (nomCode.equals("Zoological")){return NomenclaturalCode.ICZN;
		}else if (nomCode.equals("Viral")){return NomenclaturalCode.ICVCN;
		}else if (nomCode.equals("Bacteriological")){return NomenclaturalCode.ICNB;
		}else if (nomCode.equals("CultivatedPlant")){return NomenclaturalCode.ICNCP;
		//TODO code Indeterminate
//		}else if (nomCode.equals("Indeterminate")){return NomenclaturalCode.XXX();
		}	
		else {
			throw new UnknownCdmTypeException("Unknown Nomenclatural Code " + nomCode);
		}
	}
	
	public static boolean isReverseRelationshipCategory (String tcsRelationshipCategory){
		String str = tcsRelationshipCategory.replace("http://rs.tdwg.org/ontology/voc/TaxonConcept#", "");
		if ("HasSynonym".equalsIgnoreCase(str) 
				|| "IsParentTaxonOf".equalsIgnoreCase(str) 
				|| "IsIncludedIn".equalsIgnoreCase(str) 
				|| "DoesNotInclude".equalsIgnoreCase(str) 
									){
			
			return true;
		}
		return false;
	}
	
	/** Creates an cdm-Rank by the tcs rank
	 */
	public static Reference<?> pubTypeStr2PubType (String strPubType) throws UnknownCdmTypeException{
		String tcsRoot = "http://rs.tdwg.org/ontology/voc/PublicationCitation#";
		String tcsBook = tcsRoot + "Book";
		String tcsJournal = tcsRoot + "Journal";
		String tcsWebPage = tcsRoot + "WebPage";
		String tcsCommunication = tcsRoot + "Communication";
		String tcsBookSeries = tcsRoot + "BookSeries";
		String tcsArticle = tcsRoot + "JournalArticle";
		String tcsBookSection = tcsRoot + "BookSection";
		
		
//		Artwork	An Artwork type publication.
//		AudiovisualMaterial	A Audiovisual Material type publication.
//		BookSeries	A Book Series type publication.
//		Commentary	A Commentary type publication.
//		Communication	A Communication type publication.
//		ComputerProgram	A Computer Program type publication.
//		ConferenceProceedings	A Conference Proceedings type publication.
//		Determination	A Determination type publication.
//		EditedBook	A Edited Book type publication.
//		Generic	A generic publication.
//		Journal	A Journal type publication.
//		MagazineArticle	A Magazine Article type publication.
//		Map	A Map type publication.
//		NewspaperArticle	A Newspaper Article type publication.
//		Patent	A Patent type publication.
//		Report	A Report type publication.
//		SubReference	A Sub-Reference type publication.
//		Thesis	A Thesis type publication.

		if (strPubType == null){return null;
		}else if (tcsBookSection.equals(strPubType)){return ReferenceFactory.newBookSection();
		}else if (tcsBook.equals(strPubType)){return ReferenceFactory.newBook();
		}else if (tcsArticle.equals(strPubType)){return ReferenceFactory.newArticle();
		}else if (tcsJournal.equals(strPubType)){return ReferenceFactory.newJournal();
		}else if (tcsWebPage.equals(strPubType)){return ReferenceFactory.newWebPage();
		}else if (tcsCommunication.equals(strPubType)){return ReferenceFactory.newPersonalCommunication();
		}else if (tcsBookSeries.equals(strPubType)){return ReferenceFactory.newPrintSeries();
		}	
		else {
			throw new UnknownCdmTypeException("Unknown publication type " + strPubType);
		}
	}
	
	/** Creates an cdm-RelationshipTermBase by the tcsRelationshipCategory
	 */
	public static RelationshipTermBase<?> tcsRelationshipType2Relationship (String tcsRelationshipType, ResultWrapper<Boolean> inverse) throws UnknownCdmTypeException{
		if (tcsRelationshipType == null){ return null;
		
		//Synonym relationships
//		}else if (tcsRelationshipType.equals("is synonym for")){return SynonymRelationshipType.SYNONYM_OF(); 
		}else if (tcsRelationshipType.equals("has synonym")){inverse.setValue(true); return SynonymRelationshipType.SYNONYM_OF(); 
		
		//Taxon relationships
		}else if (tcsRelationshipType.equals("is child taxon of")){return TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(); 
		}else if (tcsRelationshipType.equals("is parent taxon of")){inverse.setValue(true);  return TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(); 
		
		//concept relationships
		}else if (tcsRelationshipType.equals("does not overlap")){return TaxonRelationshipType.DOES_NOT_OVERLAP(); 
		}else if (tcsRelationshipType.equals("excludes")){return TaxonRelationshipType.EXCLUDES(); 
		}else if (tcsRelationshipType.equals("includes")){return TaxonRelationshipType.INCLUDES(); 
		}else if (tcsRelationshipType.equals("is congruent to")){return TaxonRelationshipType.CONGRUENT_TO(); 
		}else if (tcsRelationshipType.equals("is not congruent to")){return TaxonRelationshipType.NOT_CONGRUENT_TO(); 
		}else if (tcsRelationshipType.equals("is not included in")){return TaxonRelationshipType.NOT_INCLUDED_IN(); 
		}else if (tcsRelationshipType.equals("overlaps")){return TaxonRelationshipType.OVERLAPS(); 
		//reverse concept relationships
		}else if (tcsRelationshipType.equals("is included in")){inverse.setValue(true); return TaxonRelationshipType.INCLUDES();
		}else if (tcsRelationshipType.equals("does not include")){inverse.setValue(true); return TaxonRelationshipType.NOT_INCLUDED_IN(); 
		
	//TODO
		
//		}else if (tcsRelationshipType.equals("has vernacular")){return TaxonRelationshipType.X; 
//		}else if (tcsRelationshipType.equals("is vernacular for")){return TaxonRelationshipType.X; 
//		}else if (tcsRelationshipType.equals("is ambiregnal of")){return TaxonRelationshipType.X; 
		}else if (tcsRelationshipType.equals("is hybrid child of")){return HybridRelationshipType.FIRST_PARENT();
//		}else if (tcsRelationshipType.equals("is hybrid parent of")){return TaxonRelationshipType.X; 
//		}else if (tcsRelationshipType.equals("is male parent of")){return TaxonRelationshipType.X; 
//		}else if (tcsRelationshipType.equals("is first parent of")){return TaxonRelationshipType.X; 
//		}else if (tcsRelationshipType.equals("is female parent of")){return TaxonRelationshipType.X; 
//		}else if (tcsRelationshipType.equals("is second parent of")){return TaxonRelationshipType.X; 
//		}else if (tcsRelationshipType.equals("is teleomorph of")){return TaxonRelationshipType.X; 
//		}else if (tcsRelationshipType.equals("is anamorph of")){return TaxonRelationshipType.X; 
		
		}else {
			throw new UnknownCdmTypeException("Unknown RelationshipCategory " + tcsRelationshipType);
		}
	}
	
	
	/** Creates an cdm-NomenclaturalCode by the tcs NomenclaturalCode
	 */
	public static NomenclaturalStatusType nomStatusString2NomStatus (String nomStatus) throws UnknownCdmTypeException{
	
		if (nomStatus == null){ return null;
		}else if ("Valid".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.VALID();
		
		}else if ("Alternative".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ALTERNATIVE();
		}else if ("nom. altern.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ALTERNATIVE();
		
		}else if ("Ambiguous".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.AMBIGUOUS();
		
		}else if ("Doubtful".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.DOUBTFUL();
		
		}else if ("Confusum".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONFUSUM();
		
		}else if ("Illegitimate".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ILLEGITIMATE();
		}else if ("nom. illeg.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ILLEGITIMATE();
		
		}else if ("Superfluous".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.SUPERFLUOUS();
		}else if ("nom. superfl.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.SUPERFLUOUS();
		
		}else if ("Rejected".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.REJECTED();
		}else if ("nom. rej.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.REJECTED();
		
		}else if ("Utique Rejected".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.UTIQUE_REJECTED();
		
		}else if ("Conserved Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONSERVED_PROP();
		
		}else if ("Orthography Conserved Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP();
		
		}else if ("Legitimate".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.LEGITIMATE();
		
		}else if ("Novum".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NOVUM();
		}else if ("nom. nov.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NOVUM();
		
		}else if ("Utique Rejected Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.UTIQUE_REJECTED_PROP();
		
		}else if ("Orthography Conserved".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED();
		
		}else if ("Rejected Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.REJECTED_PROP();
		
		}else if ("Conserved".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONSERVED();
		}else if ("nom. cons.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONSERVED();
		
		}else if ("Sanctioned".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.SANCTIONED();
		
		}else if ("Invalid".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.INVALID();
		}else if ("nom. inval.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.INVALID();
		
		}else if ("Nudum".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NUDUM();
		}else if ("nom. nud.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NUDUM();
		
		}else if ("Combination Invalid".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.COMBINATION_INVALID();
		
		}else if ("Provisional".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.PROVISIONAL();
		}else if ("nom. provis.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.PROVISIONAL();
		}
		
		else {
			throw new UnknownCdmTypeException("Unknown Nomenclatural status type " + nomStatus);
		}
	}
	
	
}
