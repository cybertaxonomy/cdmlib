/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

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
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class NomenclaturalCode extends DefinedTermBase {
	private static final Logger logger = Logger.getLogger(NomenclaturalCode.class);

	private static final UUID uuidIcnb = UUID.fromString("ff4b0979-7abf-4b40-95c0-8b8b1e8a4d5e");
	private static final UUID uuidIcbn = UUID.fromString("540fc02a-8a8e-4813-89d2-581dad4dd482");
	private static final UUID uuidIcncp = UUID.fromString("65a432b5-92b1-4c9a-8090-2a185e423d2e");
	private static final UUID uuidIczn = UUID.fromString("b584c2f8-dbe5-4454-acad-2b45e63ec11b");
	private static final UUID uuidIcvcn = UUID.fromString("e9d6d6b4-ccb7-4f28-b828-0b1501f8c75a");

	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty nomenclature code instance.
	 * 
	 * @see 	#NomenclaturalCode(String, String, String)
	 */
	public NomenclaturalCode() {
		super();
	}
	
	/** 
	 * Class constructor: creates an additional nomenclature code instance with
	 * a description (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}), a label and
	 * a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new nomenclature code to be created 
	 * @param	label  		 the string identifying the new nomenclature code
	 * 						 to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new nomenclature code to be created
	 * @see 	#NomenclaturalCode()
	 */
	public NomenclaturalCode(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}


	//********* METHODS **************************************/

	/**
	 * Returns the nomenclature code identified through its immutable universally
	 * unique identifier (UUID).
	 * 
	 * @param	uuid	the universally unique identifier
	 * @return  		the nomenclature code corresponding to the given
	 * 					universally unique identifier
	 */
	public static final NomenclaturalCode getByUuid(UUID uuid){
		return (NomenclaturalCode) findByUuid(uuid);
	}
	
	/**
	 * Returns the International Code of Nomenclature of Bacteria
	 */
	public static final NomenclaturalCode ICNB(){
		return getByUuid(uuidIcnb);
	}
	
	/**
	 * Returns the International Code of Botanical Nomenclature
	 */
	public static final NomenclaturalCode ICBN(){
		return getByUuid(uuidIcbn);
	}
	/**
	 * Returns the International Code of Cultivated Plants
	 */
	public static final NomenclaturalCode ICNCP(){
		return getByUuid(uuidIcncp);
	}

	/**
	 * Returns the International Code of Zoological Nomenclature
	 */
	public static final NomenclaturalCode ICZN(){
		return getByUuid(uuidIczn);
	}


	/**
	 * Returns the International Code for Virus Classification and Nomenclature
	 */
	public static final NomenclaturalCode ICVCN(){
		return getByUuid(uuidIcvcn);
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
	public TaxonNameBase getNewTaxonNameInstance(Rank rank){
		TaxonNameBase result;
		if (this.equals(NomenclaturalCode.ICBN())){
			result = BotanicalName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.ICZN())){
			result = ZoologicalName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.ICNCP())){
			result = CultivarPlantName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.ICNB())){
			result = BacterialName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.ICVCN())){
			result = ViralName.NewInstance(rank);
		}else {
			logger.warn("Unknown nomenclatural code: " + this.getUuid());
			result = null;
		}
		return result;
	}
	
	
}
