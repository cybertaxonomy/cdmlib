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

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;

/**
 * @see http://rs.tdwg.org/ontology/voc/TaxonName#NomenclaturalCodeTerm
 * @author a.mueller
 * @created 19.05.2008
 * @version 1.0
 */
@Entity
public class NomenclaturalCode extends DefinedTermBase {
	private static final Logger logger = Logger.getLogger(NomenclaturalCode.class);

	private static final UUID uuidBacteriological = UUID.fromString("ff4b0979-7abf-4b40-95c0-8b8b1e8a4d5e");
	private static final UUID uuidIcbn = UUID.fromString("540fc02a-8a8e-4813-89d2-581dad4dd482");
	private static final UUID uuidIcncp = UUID.fromString("65a432b5-92b1-4c9a-8090-2a185e423d2e");
	private static final UUID uuidIczn = UUID.fromString("b584c2f8-dbe5-4454-acad-2b45e63ec11b");
	private static final UUID uuidViral = UUID.fromString("e9d6d6b4-ccb7-4f28-b828-0b1501f8c75a");

	/**
	 * Constructor
	 */
	public NomenclaturalCode() {
		super();
	}
	
	/**
	 * Constructor
	 * @param term
	 * @param label
	 */
	public NomenclaturalCode(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}


	public static final NomenclaturalCode getByUuid(UUID uuid){
		return (NomenclaturalCode) findByUuid(uuid);
	}
	
	/**
	 * @return
	 */
	public static final NomenclaturalCode BACTERIOLOGICAL(){
		return getByUuid(uuidBacteriological);
	}
	
	/**
	 * International Code of Botanical Nomenclature
	 * @return
	 */
	public static final NomenclaturalCode ICBN(){
		return getByUuid(uuidIcbn);
	}
	/**
	 * International Code of Cultivated Plants
	 * @return
	 */
	public static final NomenclaturalCode ICNCP(){
		return getByUuid(uuidIcncp);
	}

	/**
	 * International Code of Zoological Nomenclature
	 * @return
	 */
	public static final NomenclaturalCode ICZN(){
		return getByUuid(uuidIczn);
	}


	/**
	 * Rules that govern the names of viral species
	 * @return
	 */
	public static final NomenclaturalCode VIRAL(){
		return getByUuid(uuidViral);
	}
	
	@Transient
	public TaxonNameBase getNewTaxonNameInstance(Rank rank){
		TaxonNameBase result;
		if (this.equals(NomenclaturalCode.ICBN())){
			result = BotanicalName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.ICZN())){
			result = ZoologicalName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.ICNCP())){
			result = CultivarPlantName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.BACTERIOLOGICAL())){
			result = BacterialName.NewInstance(rank);
		}else if (this.equals(NomenclaturalCode.VIRAL())){
			result = ViralName.NewInstance(rank);
		}else {
			logger.warn("Unknown nomenclatural code: " + this.getUuid());
			result = null;
		}
		return result;
	}
	
	
}
