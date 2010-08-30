// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.checklist;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class CentralAfricaChecklistImportState extends DbImportStateBase<CentralAfricaChecklistImportConfigurator, CentralAfricaChecklistImportState>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CentralAfricaChecklistImportState.class);

	private Map<String, DefinedTermBase> dbCdmDefTermMap = new HashMap<String, DefinedTermBase>();
	
	private String lastFamily;
	private String lastGenus;
	private Map<String, UUID> higherTaxonUuidMap = new HashMap<String, UUID>();

	private ReferenceBase genevaReference;


	public boolean containsHigherTaxon(String higherName) {
		return higherTaxonUuidMap.containsKey(higherName);
	}

	public UUID putHigherTaxon(String higherName, UUID uuid) {
		return higherTaxonUuidMap.put(higherName, uuid);
	}

	public UUID removeHigherTaxon(String higherName) {
		return higherTaxonUuidMap.remove(higherName);
	}

	public UUID getHigherTaxon(String higherName) {
		return higherTaxonUuidMap.get(higherName);
	}


	public CentralAfricaChecklistImportState(CentralAfricaChecklistImportConfigurator config) {
		super(config);
	}

	public Map<String, DefinedTermBase> getDbCdmDefinedTermMap(){
		return this.dbCdmDefTermMap;
	}
	
	public void putDefinedTermToMap(String tableName, String id, DefinedTermBase term){
		 this.dbCdmDefTermMap.put(tableName + "_" + id, term);
	}
	
	public void putDefinedTermToMap(String tableName, int id, DefinedTermBase term){
		putDefinedTermToMap(tableName, String.valueOf(id), term);
	}

	public void setLastFamily(String lastFamily) {
		this.lastFamily = lastFamily;
	}

	public String getLastFamily() {
		return lastFamily;
	}

	public void setLastGenus(String lastGenus) {
		this.lastGenus = lastGenus;
	}

	public String getLastGenus() {
		return lastGenus;
	}


	
	
	public ReferenceBase getGenevaReference() {
		return genevaReference;
	}
	public void setGenevaReference(ReferenceBase genevaReference) {
		this.genevaReference = genevaReference;
	}
	
    
}
