// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.erms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class ErmsImportState extends DbImportStateBase<ErmsImportConfigurator, ErmsImportState>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ErmsImportState.class);

	private Map<String, DefinedTermBase> dbCdmDefTermMap = new HashMap<String, DefinedTermBase>();
	
	private Map<String, User> usernameMap = new HashMap<String, User>();
	
	private Map<Integer, Map<Integer,Rank>> rankMap;
	
	private Set<Integer> acceptedTaxaKeys;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IoStateBase#initialize(eu.etaxonomy.cdm.io.common.IoConfiguratorBase)
	 */
	@Override
	public void initialize(ErmsImportConfigurator config) {
//		super(config);
		String tableName = "WebMarkerCategory_";
		//webMarkerCategory
		dbCdmDefTermMap.put(tableName + 1, MarkerType.COMPLETE());
	}

	public ErmsImportState(ErmsImportConfigurator config) {
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
	
	public User getUser(String username){
		return usernameMap.get(username);
	}

	public void putUser(String username, User user){
		usernameMap.put(username, user);
	}

	/**
	 * @param rankMap the rankMap to set
	 */
	public void setRankMap(Map<Integer, Map<Integer,Rank>> rankMap) {
		this.rankMap = rankMap;
	}

//	/**
//	 * @return the rankMap
//	 */
//	public Map<Integer, Map<Integer,Rank>> getRankMap() {
//		return rankMap;
//	}
	
 
	/**
	 * Returns the CDM rank depending on the ERMS rankId and the ERMS kingdomId. Returns <code>
	 * null</code> if the rank does not exist.
	 * Throws a RuntimeException if the rank map has not been initialized before.
	 * @param rankId
	 * @param kingdomId
	 * @return
	 * @throws RuntimeException
	 **/
	public Rank getRank (int rankId, int  kingdomId){
		Rank result = null;
		if (this.rankMap == null){
			throw new RuntimeException("rank map not initialized");
		}
		if (kingdomId == 147415 && rankId == 10){
			result = Rank.KINGDOM();
		}else{
			Map<Integer, Rank> kingdomMap = rankMap.get(rankId);
			if (kingdomMap != null){
				result = kingdomMap.get(kingdomId);
			}
		}
		return result;
	}

	public Set<Integer> getAcceptedTaxaKeys() {
		return acceptedTaxaKeys;
	}

	public void setAcceptedTaxaKeys(Set<Integer> acceptedTaxaKeys) {
		this.acceptedTaxaKeys = acceptedTaxaKeys;
	}

    
}
