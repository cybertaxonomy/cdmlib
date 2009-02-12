/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IDefinedTerm;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.media.Media;

/**
 * The class for the five nomenclature codes (ICNB, ICBN, ICNCP, ICZN and ICVCN)
 * ruling {@link TaxonNameBase taxon names}.
 * <P>
 * The standard set of nomenclature code instances will be automatically created
 * as the project starts. But this class allows to extend this standard set by
 * creating new instances of additional nomenclature codes if unlikely needed. 
 * <P>
 * This class corresponds to: <ul>
 * <li> NomenclaturalCodeTerm according to the TDWG ontology
 * <li> NomenclaturalCodesEnum according to the TCS
 * <li> CodeOfNomenclatureEnum according to the ABCD schema
 * </ul>
 * 
 * @author a.mueller
 * @created 19.05.2008
 * @version 2.0
 */

public enum NomenclaturalCode implements IDefinedTerm<NomenclaturalCode> {
	//International Code of Nomenclature of Bacteria
	ICNB(UUID.fromString("ff4b0979-7abf-4b40-95c0-8b8b1e8a4d5e"), 1 ), 
	//International Code of Botanical Nomenclature
	ICBN(UUID.fromString("540fc02a-8a8e-4813-89d2-581dad4dd482"), 2 ), 
	//International Code of Cultivated Plants
	ICNCP(UUID.fromString("65a432b5-92b1-4c9a-8090-2a185e423d2e"), 3 ), 
	//International Code of Zoological Nomenclature
	ICZN(UUID.fromString("b584c2f8-dbe5-4454-acad-2b45e63ec11b"), 4 ), 
	//International Code for Virus Classification and Nomenclature
	ICVCN(UUID.fromString("e9d6d6b4-ccb7-4f28-b828-0b1501f8c75a"), 5 );
	

	private static final Logger logger = Logger.getLogger(NomenclaturalCode.class);
	
	private UUID uuid;
	private Integer id;
	
	private NomenclaturalCode(UUID uuid, int id){
		this.uuid = uuid;
		this.id = id;
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getUuid()
	 */
	public UUID getUuid(){
		return this.uuid;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return "NomenclaturalCode" + "<" + uuid + "> " + this.name();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getByUuid(java.util.UUID)
	 */
	public NomenclaturalCode getByUuid(UUID uuid) {
		for (NomenclaturalCode nomCode : NomenclaturalCode.values()){
			if (nomCode.getUuid().equals(uuid)){
				return nomCode;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getGeneralizationOf()
	 */
	public Set<NomenclaturalCode> getGeneralizationOf() {
		return new HashSet<NomenclaturalCode>();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getIncludes()
	 */
	public Set<NomenclaturalCode> getIncludes() {
		return new HashSet<NomenclaturalCode>();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getKindOf()
	 */
	public NomenclaturalCode getKindOf() {
		return null;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getPartOf()
	 */
	public NomenclaturalCode getPartOf() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getMedia()
	 */
	public Set<Media> getMedia() {
		// TODO add links to codes
		return new HashSet<Media>();
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ILoadableTerm#readCsvLine(java.lang.Class, java.util.List, java.util.Map)
	 */
	public NomenclaturalCode readCsvLine(Class<NomenclaturalCode> termClass,
			List<String> csvLine, Map<UUID, DefinedTermBase> terms) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ILoadableTerm#writeCsvLine(au.com.bytecode.opencsv.CSVWriter, eu.etaxonomy.cdm.model.common.IDefinedTerm)
	 */
	public void writeCsvLine(CSVWriter writer, NomenclaturalCode term) {
		// TODO Auto-generated method stub
		logger.warn("write csvLine not yet implemented");
	}

	/**
	 * Creates a new particular {@link TaxonNameBase taxon name} (botanical, zoological,
	 * cultivar plant, bacterial or viral name) instance depending on <i>this</i>
	 * nomenclature code only containing the given {@link Rank rank}.
	 * 
	 * @param	rank	the rank of the new taxon name instance
	 * @see 			BotanicalName#NewInstance(Rank)
	 * @see 			ZoologicalName#NewInstance(Rank)
	 * @see 			CultivarPlantName#NewInstance(Rank)
	 * @see 			BacterialName#NewInstance(Rank)
	 * @see 			ViralName#NewInstance(Rank)
	 */
	@Transient
	public TaxonNameBase<?,?> getNewTaxonNameInstance(Rank rank){
		TaxonNameBase<?,?> result;
		NomenclaturalCode nomCode = this;
		if (this.equals(NomenclaturalCode.ICBN)){
			result = ZoologicalName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.ICZN)){
			result = ZoologicalName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.ICNCP)){
			result = CultivarPlantName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.ICNB)){
			result = BacterialName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.ICVCN)){
			result = ViralName.NewInstance(rank);
		}else {
			logger.warn("Unknown nomenclatural code: " + this.getUuid());
			result = null;
		}
		return result;
	}
	
	/**
	 * Creates a new particular {@link TaxonNameBase taxon name} (botanical, zoological,
	 * cultivar plant, bacterial or viral name) instance depending on <i>this</i>
	 * nomenclature code only containing the given {@link Rank rank}.
	 * 
	 * @param	rank	the rank of the new taxon name instance
	 * @see 			BotanicalName#NewInstance(Rank)
	 * @see 			ZoologicalName#NewInstance(Rank)
	 * @see 			CultivarPlantName#NewInstance(Rank)
	 * @see 			BacterialName#NewInstance(Rank)
	 * @see 			ViralName#NewInstance(Rank)
	 */
	@Transient
	public <T extends TaxonNameBase> Class<? extends T> getCdmClass(){
		Class<? extends T> result;
		if (this.equals(NomenclaturalCode.ICBN)){
			result = (Class<T>)BotanicalName.class;
		}else if (this.equals(NomenclaturalCode.ICZN)){
			result = (Class<T>)ZoologicalName.class;
		}else if (this.equals(NomenclaturalCode.ICNCP)){
			result = (Class<T>)CultivarPlantName.class;
		}else if (this.equals(NomenclaturalCode.ICNB)){
			result = (Class<T>)BacterialName.class;
		}else if (this.equals(NomenclaturalCode.ICVCN)){
			result = (Class<T>)ViralName.class;
		}else {
			logger.warn("Unknown nomenclatural code: " + this.getUuid());
			result = null;
		}
		return result;
	}

	
}

//@XmlAccessorType(XmlAccessType.FIELD)
//@Entity
////@Audited
//public class NomenclaturalCode extends DefinedTermBase<NomenclaturalCode> {
//	/**
//	 * SerialVersionUID
//	 */
//	private static final long serialVersionUID = -1011240079962589681L;
//	private static final Logger logger = Logger.getLogger(NomenclaturalCode.class);
//
//	private static final UUID uuidIcnb = UUID.fromString("ff4b0979-7abf-4b40-95c0-8b8b1e8a4d5e");
//	private static final UUID uuidIcbn = UUID.fromString("540fc02a-8a8e-4813-89d2-581dad4dd482");
//	private static final UUID uuidIcncp = UUID.fromString("65a432b5-92b1-4c9a-8090-2a185e423d2e");
//	private static final UUID uuidIczn = UUID.fromString("b584c2f8-dbe5-4454-acad-2b45e63ec11b");
//	private static final UUID uuidIcvcn = UUID.fromString("e9d6d6b4-ccb7-4f28-b828-0b1501f8c75a");
//
//	private static NomenclaturalCode ICZN;
//
//	private static NomenclaturalCode ICVCN;
//
//	private static NomenclaturalCode ICNCP;
//
//	private static NomenclaturalCode ICBN;
//
//	private static NomenclaturalCode ICNB;
//
//	
//	protected static Map<UUID, NomenclaturalCode> termMap = null;		
//
//	protected static NomenclaturalCode getTermByUuid(UUID uuid){
//		if (termMap == null){
//			DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
//			vocabularyStore.initialize();
//		}
//		return (NomenclaturalCode)termMap.get(uuid);
//	}
//	
//	
//	// ************* CONSTRUCTORS *************/	
//	/** 
//	 * Class constructor: creates a new empty nomenclature code instance.
//	 * 
//	 * @see 	#NomenclaturalCode(String, String, String)
//	 */
//	public NomenclaturalCode() {
//		super();
//	}
//	
//	/** 
//	 * Class constructor: creates an additional nomenclature code instance with
//	 * a description (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}), a label and
//	 * a label abbreviation.
//	 * 
//	 * @param	term  		 the string (in the default language) describing the
//	 * 						 new nomenclature code to be created 
//	 * @param	label  		 the string identifying the new nomenclature code
//	 * 						 to be created
//	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
//	 * 						 new nomenclature code to be created
//	 * @see 				 #NomenclaturalCode()
//	 */
//	public NomenclaturalCode(String term, String label, String labelAbbrev) {
//		super(term, label, labelAbbrev);
//	}
//
//
//	//********* METHODS **************************************/
//
//	/**
//	 * Returns the International Code of Nomenclature of Bacteria
//	 */
//	public static final NomenclaturalCode ICNB(){
//		return getTermByUuid(uuidIcnb);
////		return ICNB;
//	}
//	
//	/**
//	 * Returns the International Code of Botanical Nomenclature
//	 */
//	public static final NomenclaturalCode ICBN(){
//		return getTermByUuid(uuidIcbn);
////		return  ICBN;
//	}
//	/**
//	 * Returns the International Code of Cultivated Plants
//	 */
//	public static final NomenclaturalCode ICNCP(){
//		return getTermByUuid(uuidIcncp);
////		return ICNCP; 
//	}
//
//	/**
//	 * Returns the International Code of Zoological Nomenclature
//	 */
//	public static final NomenclaturalCode ICZN(){
//		return getTermByUuid(uuidIczn);
////		return ICZN; 
//	}
//
//
//	/**
//	 * Returns the International Code for Virus Classification and Nomenclature
//	 */
//	public static final NomenclaturalCode ICVCN(){
//		return getTermByUuid(uuidIcvcn);
////		return ICVCN; // FIXME(uuidIcvcn);
//	}
//	
//	/**
//	 * Creates a new particular {@link TaxonNameBase taxon name} (botanical, zoological,
//	 * cultivar plant, bacterial or viral name) instance depending on <i>this</i>
//	 * nomenclature code only containing the given {@link Rank rank}.
//	 * 
//	 * @param	rank	the rank of the new taxon name instance
//	 * @see 			BotanicalName#NewInstance(Rank)
//	 * @see 			ZoologicalName#NewInstance(Rank)
//	 * @see 			CultivarPlantName#NewInstance(Rank)
//	 * @see 			BacterialName#NewInstance(Rank)
//	 * @see 			ViralName#NewInstance(Rank)
//	 */
//	@Transient
//	public TaxonNameBase<?,?> getNewTaxonNameInstance(Rank rank){
//		TaxonNameBase<?,?> result;
//		if (this.equals(NomenclaturalCode.ICBN())){
//			result = BotanicalName.NewInstance(rank);
//		}else if (this.equals(NomenclaturalCode.ICZN())){
//			result = ZoologicalName.NewInstance(rank);
//		}else if (this.equals(NomenclaturalCode.ICNCP())){
//			result = CultivarPlantName.NewInstance(rank);
//		}else if (this.equals(NomenclaturalCode.ICNB())){
//			result = BacterialName.NewInstance(rank);
//		}else if (this.equals(NomenclaturalCode.ICVCN())){
//			result = ViralName.NewInstance(rank);
//		}else {
//			logger.warn("Unknown nomenclatural code: " + this.getUuid());
//			result = null;
//		}
//		return result;
//	}
//	
//	/**
//	 * Creates a new particular {@link TaxonNameBase taxon name} (botanical, zoological,
//	 * cultivar plant, bacterial or viral name) instance depending on <i>this</i>
//	 * nomenclature code only containing the given {@link Rank rank}.
//	 * 
//	 * @param	rank	the rank of the new taxon name instance
//	 * @see 			BotanicalName#NewInstance(Rank)
//	 * @see 			ZoologicalName#NewInstance(Rank)
//	 * @see 			CultivarPlantName#NewInstance(Rank)
//	 * @see 			BacterialName#NewInstance(Rank)
//	 * @see 			ViralName#NewInstance(Rank)
//	 */
//	@Transient
//	public <T extends TaxonNameBase> Class<? extends T> getCdmClass(){
//		Class<? extends T> result;
//		if (this.equals(NomenclaturalCode.ICBN())){
//			result = (Class<T>)BotanicalName.class;
//		}else if (this.equals(NomenclaturalCode.ICZN())){
//			result = (Class<T>)ZoologicalName.class;
//		}else if (this.equals(NomenclaturalCode.ICNCP())){
//			result = (Class<T>)CultivarPlantName.class;
//		}else if (this.equals(NomenclaturalCode.ICNB())){
//			result = (Class<T>)BacterialName.class;
//		}else if (this.equals(NomenclaturalCode.ICVCN())){
//			result = (Class<T>)ViralName.class;
//		}else {
//			logger.warn("Unknown nomenclatural code: " + this.getUuid());
//			result = null;
//		}
//		return result;
//	}
//
//	@Override
//	protected void setDefaultTerms(TermVocabulary<NomenclaturalCode> termVocabulary) {
//		termMap = new HashMap<UUID, NomenclaturalCode>();
//		for (NomenclaturalCode term : termVocabulary.getTerms()){
//			termMap.put(term.getUuid(), term);
//		}
//		
////		termMap.put(uuidIcbn, termVocabulary.findTermByUuid(NomenclaturalCode.uuidIcbn));
////		termMap.put(uuidIcnb, termVocabulary.findTermByUuid(NomenclaturalCode.uuidIcnb));
////		termMap.put(uuidIcncp, termVocabulary.findTermByUuid(NomenclaturalCode.uuidIcncp));
////		termMap.put(uuidIcvcn, termVocabulary.findTermByUuid(NomenclaturalCode.uuidIcvcn));
////		termMap.put(uuidIczn, termVocabulary.findTermByUuid(NomenclaturalCode.uuidIczn));
//		
////		NomenclaturalCode.ICBN = termVocabulary.findTermByUuid(NomenclaturalCode.uuidIcbn);
////		NomenclaturalCode.ICNB = termVocabulary.findTermByUuid(NomenclaturalCode.uuidIcnb);
////		NomenclaturalCode.ICNCP = termVocabulary.findTermByUuid(NomenclaturalCode.uuidIcncp);
////		NomenclaturalCode.ICVCN = termVocabulary.findTermByUuid(NomenclaturalCode.uuidIcvcn);
////		NomenclaturalCode.ICZN = termVocabulary.findTermByUuid(NomenclaturalCode.uuidIczn);
//	}
//	
//	
//}
