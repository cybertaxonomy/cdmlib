/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
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
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
//import eu.etaxonomy.cdm.model.reference.WebPage;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 29.05.2008
 * @version 1.0
 */
public final class TcsRdfTransformer {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TcsRdfTransformer.class);
 

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
	public static Rank rankString2Rank (String strRank) throws UnknownCdmTypeException{
		String tcsRoot = "http://rs.tdwg.org/ontology/voc/TaxonRank#";
		String tcsFamily = tcsRoot + "Family";
		String tcsSubFamily = tcsRoot + "Subfamily";
		String tcsTribe = tcsRoot + "Tribe";
		String tcsSubtribe = tcsRoot + "Subtribe";
		String tcsGenus = tcsRoot + "Genus";
		String tcsSpecies = tcsRoot + "Species";
		String tcsSubSpecies = tcsRoot + "Subspecies";
		String tcsVariety = tcsRoot + "Variety";
		String tcsSubVariety = tcsRoot + "Sub-Variety";
		String tcsForm = tcsRoot + "Form";
		
		
		
		if (strRank == null){return null;
		}else if (tcsFamily.equals(strRank)){return Rank.FAMILY();
		}else if (tcsSubFamily.equals(strRank)){return Rank.SUBFAMILY();
		}else if (tcsTribe.equals(strRank)){return Rank.TRIBE();
		}else if (tcsSubtribe.equals(strRank)){return Rank.SUBTRIBE();
		}else if (tcsGenus.equals(strRank)){return Rank.GENUS();
		}else if (tcsSpecies.equals(strRank)){return Rank.SPECIES();
		}else if (tcsVariety.equals(strRank)){return Rank.VARIETY();
		}else if (tcsSubVariety.equals(strRank)){return Rank.SUBVARIETY();
		}else if (tcsSubSpecies.equals(strRank)){return Rank.SUBSPECIES();
		}else if (tcsForm.equals(strRank)){return Rank.FORM();
		}	
		else {
			throw new UnknownCdmTypeException("Unknown Rank " + strRank);
		}
	}
	
	/** Creates an cdm-NomenclaturalCode by the tcs NomenclaturalCode
	 */
	public static NomenclaturalCode nomCodeString2NomCode (String nomCode) throws UnknownCdmTypeException{
		
		String tcsRoot = "http://rs.tdwg.org/ontology/voc/TaxonName#";
		String tcsICBN = tcsRoot + "ICBN";
		String tcsICZN = tcsRoot + "ICZN";
		String tcsICNCP = tcsRoot + "ICNCP";
		String tcsBacteriological = tcsRoot + "BACTERIOLOGICAL";
		String tcsViral = tcsRoot + "VIRAL";
		
		if (nomCode == null){ return null;
		}else if (tcsICBN.equals(nomCode)){return NomenclaturalCode.ICBN;
		}else if (tcsICZN.equals(nomCode)){return NomenclaturalCode.ICZN;
		}else if (tcsICNCP.equals(nomCode)){return NomenclaturalCode.ICNCP;
		}else if (tcsBacteriological.equals(nomCode)){return NomenclaturalCode.ICNB;
		}else if (tcsViral.equals(nomCode)){return NomenclaturalCode.ICVCN;
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
	public static ReferenceBase pubTypeStr2PubType (String strPubType) throws UnknownCdmTypeException{
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

		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		if (strPubType == null){return null;
		}else if (tcsBookSection.equals(strPubType)){return refFactory.newBookSection();
		}else if (tcsBook.equals(strPubType)){return refFactory.newBook();
		}else if (tcsArticle.equals(strPubType)){return refFactory.newArticle();
		}else if (tcsJournal.equals(strPubType)){return refFactory.newJournal();
		}else if (tcsWebPage.equals(strPubType)){return refFactory.newWebPage();
		}else if (tcsCommunication.equals(strPubType)){return refFactory.newPersonalCommunication();
		}else if (tcsBookSeries.equals(strPubType)){return refFactory.newPrintSeries();
		}	
		else {
			throw new UnknownCdmTypeException("Unknown publication type " + strPubType);
		}
	}
	
	/** Creates an cdm-RelationshipTermBase by the tcsRelationshipCategory
	 */
	public static RelationshipTermBase tcsRelationshipCategory2Relationship (String tcsRelationshipCategory) throws UnknownCdmTypeException{
		String tcsRoot = "http://rs.tdwg.org/ontology/voc/TaxonConcept#";
		String doesNotInclude  = tcsRoot + "DoesNotInclude";
		String doesNotOverlap  = tcsRoot + "DoesNotOverlap";
		String excludes  = tcsRoot + "Excludes";
		String hasSynonym  = tcsRoot + "HasSynonym";
		String hasVernacular  = tcsRoot + "HasVernacular";
		String includes  = tcsRoot + "Includes";
		String isAmbiregnalOf  = tcsRoot + "IsAmbiregnalOf";
		String isAnamorphOf  = tcsRoot + "IsAnamorphOf";
		String isChildTaxonOf  = tcsRoot + "IsChildTaxonOf";
		String isCongruentTo  = tcsRoot + "IsCongruentTo";
		String isFemaleParentOf  = tcsRoot + "IsFemaleParentOf";
		String isFirstParentOf  = tcsRoot + "IsFirstParentOf";
		String isHybridChildOf  = tcsRoot + "IsHybridChildOf";
		String isHybridParentOf  = tcsRoot + "IsHybridParentOf";
		String isIncludedIn  = tcsRoot + "IsIncludedIn";
		String isMaleParentOf  = tcsRoot + "IsMaleParentOf";
		String isNotCongruentTo  = tcsRoot + "IsNotCongruentTo";
		String isNotIncludedIn  = tcsRoot + "IsNotIncludedIn";
		String isParentTaxonOf  = tcsRoot + "IsParentTaxonOf";
		String isSecondParentOf  = tcsRoot + "IsSecondParentOf";
		String isSynonymFor  = tcsRoot + "IsSynonymFor";
		String isTeleomorphOf  = tcsRoot + "IsTeleomorphOf";
		String isVernacularFor  = tcsRoot + "IsVernacularFor";
		String overlaps  = tcsRoot + "Overlaps";

		if (tcsRelationshipCategory == null){ return null;
		
		//Synonym relationships
		}else if (isSynonymFor.equals(tcsRelationshipCategory)){return SynonymRelationshipType.SYNONYM_OF(); 
		}else if (hasSynonym.equals(tcsRelationshipCategory)){/*isReverse = true; */ return SynonymRelationshipType.SYNONYM_OF(); 
		
		//Taxon relationships
		}else if (isChildTaxonOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(); 
		}else if (isParentTaxonOf.equals(tcsRelationshipCategory)){/*isReverse = true; */ return TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(); 
		
		//concept relationships
		}else if (doesNotOverlap.equals(tcsRelationshipCategory)){return TaxonRelationshipType.DOES_NOT_OVERLAP(); 
		}else if (excludes.equals(tcsRelationshipCategory)){return TaxonRelationshipType.EXCLUDES(); 
		}else if (includes.equals(tcsRelationshipCategory)){return TaxonRelationshipType.INCLUDES(); 
		}else if (isCongruentTo.equals(tcsRelationshipCategory)){return TaxonRelationshipType.CONGRUENT_TO(); 
		}else if (isNotCongruentTo.equals(tcsRelationshipCategory)){return TaxonRelationshipType.NOT_CONGRUENT_TO(); 
		}else if (isNotIncludedIn.equals(tcsRelationshipCategory)){return TaxonRelationshipType.NOT_INCLUDED_IN(); 
		}else if (overlaps.equals(tcsRelationshipCategory)){return TaxonRelationshipType.OVERLAPS(); 
		//reverse concept relationships
		}else if (isIncludedIn.equals(tcsRelationshipCategory)){/*isReverse = true; */ return TaxonRelationshipType.INCLUDES();
		}else if (doesNotInclude.equals(tcsRelationshipCategory)){/*isReverse = true; */ return TaxonRelationshipType.NOT_INCLUDED_IN(); 
		
	//TODO	
//		}else if (hasVernacular.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isAmbiregnalOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isAnamorphOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isFemaleParentOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isFirstParentOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isHybridChildOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isHybridParentOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isMaleParentOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isSecondParentOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isTeleomorphOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isVernacularFor.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
		
		}else {
			throw new UnknownCdmTypeException("Unknown RelationshipCategory " + tcsRelationshipCategory);
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
