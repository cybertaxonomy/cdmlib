/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.faunaEuropaea;

import static eu.etaxonomy.cdm.io.pesi.faunaEuropaea.FaunaEuropaeaTransformer.P_PARENTHESIS;
import static eu.etaxonomy.cdm.io.pesi.faunaEuropaea.FaunaEuropaeaTransformer.R_GENUS;
import static eu.etaxonomy.cdm.io.pesi.faunaEuropaea.FaunaEuropaeaTransformer.R_SPECIES;
import static eu.etaxonomy.cdm.io.pesi.faunaEuropaea.FaunaEuropaeaTransformer.R_SUBGENUS;
import static eu.etaxonomy.cdm.io.pesi.faunaEuropaea.FaunaEuropaeaTransformer.R_SUBSPECIES;
import static eu.etaxonomy.cdm.io.pesi.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_ACCEPTED;
import static eu.etaxonomy.cdm.io.pesi.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_NOT_ACCEPTED;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;




import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportState;
import eu.etaxonomy.cdm.io.pesi.out.PesiTransformer;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaTaxonNameImport extends FaunaEuropaeaImportBase  {
	
	public static final String OS_NAMESPACE_TAXON = "Taxon";
	
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaTaxonNameImport.class);

	/* Max number of taxa to retrieve (for test purposes) */
	private int maxTaxa = 0;

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(FaunaEuropaeaImportState state) {
		boolean result = true;
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		logger.warn("Checking for Taxa not yet fully implemented");
//		result &= checkTaxonStatus(fauEuConfig);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(FaunaEuropaeaImportState state) {
		return ! state.getConfig().isDoTaxa();
	}

	/** 
	 * Import taxa from FauEU DB
	 */
	protected void doInvoke(FaunaEuropaeaImportState state) {				
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }
		
		processTaxa(state);
		logger.info("End making taxa...");
		return;
	}


	/**
	 * Returns an empty string in case of a null string.
	 * This avoids having the string "null" when using StringBuilder.append(null);
	 * @param string
	 * @return
	 */
	private String NullToEmpty(String string) {
		if (string == null) {
			return "";
		} else {
			return string;
		}
	}

	/** Retrieve taxa from FauEu DB, process in blocks */
	private void processTaxa(FaunaEuropaeaImportState state) {

		int limit = state.getConfig().getLimitSave();

		TransactionStatus txStatus = null;

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		
		Map<Integer, TaxonBase<?>> taxonMap = null;
		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = null;
		/* Store for heterotypic synonyms to be save separately */
		Set<Synonym> synonymSet = null;

		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Reference<?> sourceRef = fauEuConfig.getSourceReference();

		Source source = fauEuConfig.getSource();
		int i = 0;
		
		String selectCount = 
			" SELECT count(*) ";

		String selectColumns = 
			" SELECT Parent.TAX_NAME AS P2Name, Parent.TAX_RNK_ID AS P2RankId, " +
			" GrandParent.TAX_ID AS GP3Id, GrandParent.TAX_NAME AS GP3Name, GrandParent.TAX_RNK_ID AS GP3RankId, " +
			" GreatGrandParent.TAX_ID AS GGP4Id, GreatGrandParent.TAX_NAME AS GGP4Name, GreatGrandParent.TAX_RNK_ID AS GGP4RankId, " +
			" GreatGreatGrandParent.TAX_ID AS GGGP5Id, GreatGreatGrandParent.TAX_NAME AS GGGP5Name, GreatGreatGrandParent.TAX_RNK_ID AS GGGP5RankId, " +
			" OriginalGenusTaxon.TAX_NAME AS OGenusName, " +
			" GreatGreatGreatGrandParent.TAX_ID AS GGGGP6Id, GreatGreatGreatGrandParent.TAX_NAME AS GGGGP6Name, GreatGreatGreatGrandParent.TAX_RNK_ID AS GGGGP6RankId," +
			" expertUsers.usr_id AS expertUserId, expertUsers.usr_title AS ExpertUsrTitle, expertUsers.usr_firstname AS ExpertUsrFirstname, expertUsers.usr_lastname AS ExpertUsrLastname," +
			" speciesExpertUsers.usr_id AS speciesExpertUserId, speciesExpertUsers.usr_title AS SpeciesUsrTitle, speciesExpertUsers.usr_firstname AS SpeciesUsrFirstname, speciesExpertUsers.usr_lastname AS SpeciesUsrLastname," +
			" Taxon.*, rank.*, author.* ";
		
		String fromClause = 
			" FROM Taxon LEFT OUTER JOIN " +
			" Taxon AS Parent ON Taxon.TAX_TAX_IDPARENT = Parent.TAX_ID LEFT OUTER JOIN " +
			" Taxon AS GrandParent ON Parent.TAX_TAX_IDPARENT = GrandParent.TAX_ID LEFT OUTER JOIN " +
			" Taxon AS GreatGrandParent ON GrandParent.TAX_TAX_IDPARENT = GreatGrandParent.TAX_ID LEFT OUTER JOIN " +
			" Taxon AS GreatGreatGrandParent ON GreatGrandParent.TAX_TAX_IDPARENT = GreatGreatGrandParent.TAX_ID LEFT OUTER JOIN " +
			" Taxon AS GreatGreatGreatGrandParent ON GreatGreatGrandParent.TAX_TAX_IDPARENT = GreatGreatGreatGrandParent.TAX_ID LEFT OUTER JOIN " +
			" Taxon AS OriginalGenusTaxon ON Taxon.TAX_TAX_IDGENUS = OriginalGenusTaxon.TAX_ID LEFT OUTER JOIN " +
			" author ON Taxon.TAX_AUT_ID = author.aut_id LEFT OUTER JOIN " +
			" users AS expertUsers ON Taxon.TAX_USR_IDGC = expertUsers.usr_id LEFT OUTER JOIN " +
			" users AS speciesExpertUsers ON Taxon.TAX_USR_IDSP = speciesExpertUsers.usr_id LEFT OUTER JOIN " +
			" rank ON Taxon.TAX_RNK_ID = rank.rnk_id ";

		String countQuery = 
			selectCount + fromClause;

		String selectQuery = 
			selectColumns + fromClause;
		

        try {

			ResultSet rs = source.getResultSet(countQuery);
			rs.next();
			int count = rs.getInt(1);
			
			rs = source.getResultSet(selectQuery);

	        if (logger.isInfoEnabled()) {
				logger.info("Number of rows: " + count);
				logger.info("Count Query: " + countQuery);
				logger.info("Select Query: " + selectQuery);
			}
	        
			while (rs.next()) {

				if ((i++ % limit) == 0) {
					
					txStatus = startTransaction();
					taxonMap = new HashMap<Integer, TaxonBase<?>>(limit);
					fauEuTaxonMap = new HashMap<Integer, FaunaEuropaeaTaxon>(limit);
					synonymSet = new HashSet<Synonym>();
					
					if(logger.isInfoEnabled()) {
						logger.info("i = " + i + " - Transaction started"); 
					}
				}

				String localName = rs.getString("TAX_NAME");
				String parentName = rs.getString("P2Name");
				int grandParentId = rs.getInt("GP3Id");
				String grandParentName = rs.getString("GP3Name");
				int greatGrandParentId = rs.getInt("GGP4Id");
				String greatGrandParentName = rs.getString("GGP4Name");
				int greatGreatGrandParentId = rs.getInt("GGGP5Id");
				String greatGreatGrandParentName = rs.getString("GGGP5Name");
				String greatGreatGreatGrandParentName = rs.getString("GGGGP6Name");
				String originalGenusName = rs.getString("OGenusName");
				String autName = rs.getString("aut_name");
				int taxonId = rs.getInt("TAX_ID");
				int rankId = rs.getInt("TAX_RNK_ID");
				int parentId = rs.getInt("TAX_TAX_IDPARENT");
				int parentRankId = rs.getInt("P2RankId");
				int grandParentRankId = rs.getInt("GP3RankId");
				int greatGrandParentRankId = rs.getInt("GGP4RankId");
				int greatGreatGrandParentRankId = rs.getInt("GGGP5RankId");
				int greatGreatGreatGrandParentRankId = rs.getInt("GGGGP6RankId");
				int originalGenusId = rs.getInt("TAX_TAX_IDGENUS");
				int autId = rs.getInt("TAX_AUT_ID");
				int status = rs.getInt("TAX_VALID");
				

				// user related
				String expertUsrTitle = rs.getString("ExpertUsrTitle");
				String expertUsrFirstname = rs.getString("ExpertUsrFirstname");
				String expertUsrLastname = rs.getString("ExpertUsrLastname");
				String speciesUsrTitle = rs.getString("SpeciesUsrTitle");
				String speciesUsrFirstname = rs.getString("SpeciesUsrFirstname");
				String speciesUsrLastname = rs.getString("SpeciesUsrLastname");
				String expertUserId = "" + rs.getInt("expertUserId");
				String speciesExpertUserId = "" + rs.getInt("speciesExpertUserId");

				String expertName = "";
				if (expertUsrTitle != null) {
					expertName = expertUsrTitle;
					if (! expertUsrTitle.endsWith(".")) {
						expertName += ".";
					}
				}
				expertName += expertUsrTitle == null ? NullToEmpty(expertUsrFirstname) : " " + NullToEmpty(expertUsrFirstname);
				if ((expertUsrTitle != null || expertUsrFirstname != null) && expertUsrLastname != null) {
					expertName += " " + expertUsrLastname;
				}
				
				String speciesExpertName = speciesUsrTitle == null ? "" : speciesUsrTitle + ".";
				if (speciesUsrTitle != null) {
					speciesExpertName = speciesUsrTitle;
					if (! speciesUsrTitle.endsWith(".")) {
						speciesExpertName += ".";
					}
				}
				speciesExpertName += speciesUsrTitle == null ? NullToEmpty(speciesUsrFirstname) : " " + NullToEmpty(speciesUsrFirstname);
				if ((speciesUsrTitle != null || speciesUsrFirstname != null) && speciesUsrLastname != null) {
					speciesExpertName += " " + speciesUsrLastname;
				}
				
				// date related
				Timestamp createdTimeStamp = rs.getTimestamp("TAX_CREATEDAT");
				Timestamp modifiedTimeStamp = rs.getTimestamp("TAX_MODIFIEDAT");
				boolean isCreated = createdTimeStamp.equals(modifiedTimeStamp);
				String lastAction = isCreated ? "created" : "modified";
				DateTime created = new DateTime(createdTimeStamp);
				DateTime modified = isCreated ? null : new DateTime(modifiedTimeStamp);
				DateTime lastActionDate = isCreated ?  created : modified;
				String lastActionDateStr = isCreated ? createdTimeStamp.toString() : modifiedTimeStamp.toString();
				
				
				// note related
				String taxComment = rs.getString("TAX_TAXCOMMENT");
				String fauComment = rs.getString("TAX_FAUCOMMENT");
				String fauExtraCodes = rs.getString("TAX_FAUEXTRACODES");

				// Avoid publication year 0 for NULL values in database.
				Integer year = rs.getInt("TAX_YEAR");
				if (year != null && year.intValue() == 0) {
					year = null;
				}
				
				//int familyId = rs.getInt("TAX_TAX_IDFAMILY");

				Rank rank = null;
				int parenthesis = rs.getInt("TAX_PARENTHESIS");
				UUID taxonBaseUuid = null;
				if (resultSetHasColumn(rs,"UUID")){
					String uuid = rs.getString("UUID");
					taxonBaseUuid = UUID.fromString(uuid);
				} else {
					taxonBaseUuid = UUID.randomUUID();
				}

				FaunaEuropaeaTaxon fauEuTaxon = new FaunaEuropaeaTaxon();
				fauEuTaxon.setUuid(taxonBaseUuid);
				fauEuTaxon.setId(taxonId);
				fauEuTaxon.setRankId(rankId);
				fauEuTaxon.setLocalName(localName);
				
				fauEuTaxon.setParentId(parentId);
				fauEuTaxon.setParentRankId(parentRankId);
				fauEuTaxon.setParentName(parentName);
				
				fauEuTaxon.setGrandParentId(grandParentId);
				fauEuTaxon.setGrandParentRankId(grandParentRankId);
				fauEuTaxon.setGrandParentName(grandParentName);
				
				fauEuTaxon.setGreatGrandParentId(greatGrandParentId);
				fauEuTaxon.setGreatGrandParentRankId(greatGrandParentRankId);
				fauEuTaxon.setGreatGrandParentName(greatGrandParentName);
				
				fauEuTaxon.setGreatGreatGrandParentId(greatGreatGrandParentId);
				fauEuTaxon.setGreatGreatGrandParentRankId(greatGreatGrandParentRankId);
				fauEuTaxon.setGreatGreatGrandParentName(greatGreatGrandParentName);
				
				fauEuTaxon.setGreatGreatGreatGrandParentRankId(greatGreatGreatGrandParentRankId);
				fauEuTaxon.setGreatGreatGreatGrandParentName(greatGreatGreatGrandParentName);
				
				fauEuTaxon.setOriginalGenusId(originalGenusId);
				fauEuTaxon.setYear(year);
				fauEuTaxon.setOriginalGenusName(originalGenusName);
				fauEuTaxon.setAuthorName(autName);
				if (parenthesis == P_PARENTHESIS) {
					fauEuTaxon.setParenthesis(true);
				} else {
					fauEuTaxon.setParenthesis(false);
				}
				if (status == T_STATUS_ACCEPTED) {
					fauEuTaxon.setValid(true);
				} else {
					fauEuTaxon.setValid(false);
				}

//				fauEuTaxon.setAuthorId(autId);

				try {
					rank = FaunaEuropaeaTransformer.rankId2Rank(rs, false);
				} catch (UnknownCdmTypeException e) {
					logger.warn("Taxon (" + taxonId + ") has unknown rank (" + rankId + ") and could not be saved.");
					continue;
				} catch (NullPointerException e) {
					logger.warn("Taxon (" + taxonId + ") has rank null and can not be saved.");
					continue;
				}

				Reference<?> sourceReference = fauEuConfig.getSourceReference();
				Reference<?> auctReference = fauEuConfig.getAuctReference();

				ZoologicalName zooName = ZoologicalName.NewInstance(rank);
				TeamOrPersonBase<?> author = authorStore.get(autId);
				
				zooName.setCombinationAuthorTeam(author);
				zooName.setPublicationYear(year);
				
				
				// Add Date extensions to this zooName
				Extension.NewInstance(zooName, lastAction, getExtensionType(state, PesiTransformer.lastActionUuid, "LastAction", "LastAction", "LA"));
//				Extension.NewInstance(zooName, lastActionDateStr, getExtensionType(state, PesiTransformer.lastActionDateUuid, "LastActionDate", "LastActionDate", "LAD"));
				zooName.setCreated(created);
				zooName.setUpdated(modified);
				
				/* Add Note extensions to this zooName
				Extension.NewInstance(zooName, taxComment, getExtensionType(PesiTransformer.taxCommentUuid, "TaxComment", "TaxComment", "TC"));
				Extension.NewInstance(zooName, fauComment, getExtensionType(PesiTransformer.fauCommentUuid, "FauComment", "FauComment", "FC"));
				Extension.NewInstance(zooName, fauExtraCodes, getExtensionType(PesiTransformer.fauExtraCodesUuid, "FauExtraCodes", "FauExtraCodes", "FEC"));
				*/
				TaxonBase<?> taxonBase;

				Synonym synonym = null;
				Taxon taxon;
				try {
					// check for occurrence of the auct string in auctName
					String auctRegEx = "\\bauct\\b\\.?"; // The word "auct" with or without "."
					
					String auctWithNecRegEx = "\\bauct\\b\\.?.*\\bnec\\b\\.?.*";
					String necAuctRegEx = ".*\\bnec\\b\\.?.*\\bauct\\b\\.?";
					
					boolean auctNecFound = expressionMatches(auctWithNecRegEx, autName);
					boolean necAuctFound = expressionMatches(necAuctRegEx, autName);
					boolean auctWordFound = expressionMatches(auctRegEx, autName);
					
						
					
					if (status == T_STATUS_ACCEPTED ) {
						
							taxon = Taxon.NewInstance(zooName, sourceReference);
							if (logger.isDebugEnabled()) {
								logger.debug("Taxon created (" + taxonId + ")");
							}
							
						
						taxonBase = taxon;
					} else if ((status == T_STATUS_NOT_ACCEPTED) && ! auctWordFound) { // synonym
						synonym = Synonym.NewInstance(zooName, sourceReference);
						//logger.info("Synonym created: " + synonym.getTitleCache() + " taxonName: " + zooName.getTitleCache());
						if (logger.isDebugEnabled()) {
							logger.debug("Synonym created (" + taxonId + ")");
						}
						taxonBase = synonym;
					} else if (status == T_STATUS_NOT_ACCEPTED && (necAuctFound || auctNecFound)){
						synonym = Synonym.NewInstance(zooName, sourceReference);
						NomenclaturalStatus tempNameNomStatus = NomenclaturalStatus.NewInstance(FaunaEuropaeaTransformer.getNomStatusTempNamed(getTermService()));
						zooName.addStatus(tempNameNomStatus);
						taxonBase = synonym;
						logger.info("temporary named name created ("+ taxonId + ") " + zooName.getTitleCache()+ zooName.getStatus().toString());
					}else if (status == T_STATUS_NOT_ACCEPTED && auctWordFound){
							// misapplied name
								zooName.setCombinationAuthorTeam(null);
								zooName.setPublicationYear(null);
								
								taxon = Taxon.NewInstance(zooName, auctReference);
								taxonBase = taxon;
								logger.info("Misapplied name created ("+ taxonId + ") " + autName);
								if (logger.isDebugEnabled()) {
									logger.debug("Misapplied name created (" + taxonId + ")");
									}
					}else{
						
						logger.warn("Unknown taxon status " + status + ". Taxon (" + taxonId + ") ignored.");
						continue;
					}

					taxonBase.setUuid(taxonBaseUuid);
					taxonBase.setLsid(new LSID( "urn:lsid:faunaeur.org:taxname:"+taxonId));
					taxonBase.setCreated(created);
					taxonBase.setUpdated(modified);
					
					// Add Note extensions to this taxon
					
					if (!StringUtils.isBlank(taxComment)){
						Extension.NewInstance(taxonBase, taxComment, getExtensionType(state, PesiTransformer.taxCommentUuid, "TaxComment", "TaxComment", "TC"));
					}
					if (!StringUtils.isBlank(fauComment)){
						Extension.NewInstance(taxonBase, fauComment, getExtensionType(state, PesiTransformer.fauCommentUuid, "FauComment", "FauComment", "FC"));
					}
					if (!StringUtils.isBlank(fauExtraCodes)){
						Extension.NewInstance(taxonBase, fauExtraCodes, getExtensionType(state, PesiTransformer.fauExtraCodesUuid, "FauExtraCodes", "FauExtraCodes", "FEC"));
					}
					// Add UserId extensions to this zooName
					//Extension.NewInstance(zooName, expertUserId, getExtensionType(state, PesiTransformer.expertUserIdUuid, "expertUserId", "expertUserId", "EUID"));
					//Extension.NewInstance(zooName, speciesExpertUserId, getExtensionType(state, PesiTransformer.speciesExpertUserIdUuid, "speciesExpertUserId", "speciesExpertUserId", "SEUID"));
					
					// Add Expert extensions to this zooName
					Extension.NewInstance(taxonBase, expertName, getExtensionType(state, PesiTransformer.expertNameUuid, "ExpertName", "ExpertName", "EN"));
					Extension.NewInstance(taxonBase, speciesExpertName, getExtensionType(state, PesiTransformer.speciesExpertNameUuid, "SpeciesExpertName", "SpeciesExpertName", "SEN"));

					
					ImportHelper.setOriginalSource(taxonBase, fauEuConfig.getSourceReference(), taxonId, OS_NAMESPACE_TAXON);
					ImportHelper.setOriginalSource(zooName, fauEuConfig.getSourceReference(), taxonId, "TaxonName");

					if (!taxonMap.containsKey(taxonId)) {
						
						taxonMap.put(taxonId, taxonBase);
						fauEuTaxonMap.put(taxonId, fauEuTaxon);
						
					} else {
						logger.warn("Not imported taxon base with duplicated TAX_ID (" + taxonId + 
								") " + localName);
					}
				} catch (Exception e) {
					logger.warn("An exception occurred when creating taxon base with id " + taxonId + 
					". Taxon base could not be saved.");
					e.printStackTrace();
				}

				if (((i % limit) == 0 && i != 1 )) { 

					commitTaxa(state, txStatus, taxonMap, fauEuTaxonMap,
							synonymSet);
					
					taxonMap = null;
					synonymSet = null;
					fauEuTaxonMap = null;
					
					
					if(logger.isInfoEnabled()) {
						logger.info("i = " + i + " - Transaction committed"); 
					}
				}

			}
			if (taxonMap != null){
				commitTaxa(state, txStatus, taxonMap, fauEuTaxonMap,
						synonymSet);
				taxonMap = null;
				synonymSet = null;
				fauEuTaxonMap = null;
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			state.setUnsuccessfull();
		}

		return;
	}

	private void commitTaxa(FaunaEuropaeaImportState state,
			TransactionStatus txStatus, Map<Integer, TaxonBase<?>> taxonMap,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap,
			Set<Synonym> synonymSet) {
		processTaxaSecondPass(state, taxonMap, fauEuTaxonMap, synonymSet);
		if(logger.isDebugEnabled()) { logger.debug("Saving taxa ... " + taxonMap.size()); }
		getTaxonService().save((Collection)taxonMap.values());
		
		commitTransaction(txStatus);
	}

	
	
	/**
	 * Processes taxa from complete taxon store
	 */
	private void processTaxaSecondPass(FaunaEuropaeaImportState state, Map<Integer, TaxonBase<?>> taxonMap,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, Set<Synonym> synonymSet) {
		if(logger.isDebugEnabled()) { logger.debug("Processing taxa second pass..."); }

		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		
		for (int id : taxonMap.keySet())
		{
			if (logger.isDebugEnabled()) { logger.debug("Taxon # " + id); }
			
			TaxonBase<?> taxonBase = taxonMap.get(id);
			TaxonNameBase<?,?> taxonName = taxonBase.getName();
			FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(id);
			boolean useOriginalGenus = false;
			//if (taxonBase instanceof Synonym){
			if (!fauEuTaxon.isValid() ){
				useOriginalGenus = true;
			}
			
			String nameString = 
				buildTaxonName(fauEuTaxon, taxonBase, taxonName, useOriginalGenus, fauEuConfig);
			
			if (taxonBase instanceof Synonym){
				logger.debug("Name of Synonym: " + nameString);
			}
			
			if (fauEuConfig.isDoBasionyms() 
					&& fauEuTaxon.getRankId() > R_SUBGENUS
					&& (fauEuTaxon.getOriginalGenusId() != 0)) {
				
				Integer originalGenusId = fauEuTaxon.getOriginalGenusId();
				Integer actualGenusId = getActualGenusId(fauEuTaxon);
				
				if (logger.isDebugEnabled()) {
					logger.debug("actual genus id = " + actualGenusId + ", original genus id = " + originalGenusId);
				}
				
				if (actualGenusId.intValue() != originalGenusId.intValue() && taxonBase.isInstanceOf(Taxon.class)) {
					createBasionym(fauEuTaxon, taxonBase, taxonName, fauEuConfig, synonymSet, state);
				} else if (fauEuTaxon.isParenthesis()) {
					//the authorteam should be set in parenthesis because there should be a basionym, but we do not know it?
					ZoologicalName zooName = taxonName.deproxy(taxonName, ZoologicalName.class);
					zooName.setBasionymAuthorTeam(zooName.getCombinationAuthorTeam());
					zooName.setCombinationAuthorTeam(null);
					zooName.setOriginalPublicationYear(zooName.getPublicationYear());
					zooName.setPublicationYear(null);
				}
				
			}
		}
		return;	
	}

	
	private void createBasionym(FaunaEuropaeaTaxon fauEuTaxon, TaxonBase<?> taxonBase, 
			TaxonNameBase<?,?>taxonName, FaunaEuropaeaImportConfigurator fauEuConfig,
			Set<Synonym> synonymSet, FaunaEuropaeaImportState state) {

		try {
			ZoologicalName zooName = taxonName.deproxy(taxonName, ZoologicalName.class);
			
			// create basionym
			ZoologicalName basionym = ZoologicalName.NewInstance(taxonName.getRank());
			basionym.setCombinationAuthorTeam(zooName.getCombinationAuthorTeam());
			basionym.setPublicationYear(zooName.getPublicationYear());

			
			zooName.addBasionym(basionym, fauEuConfig.getSourceReference(), null, null);
			//TODO:this is a workaround for fauna europaea, this should be fixed in cdm model. this should be not a basionym but an orthographic variant (original spelling)
			if (fauEuTaxon.isParenthesis()){
				zooName.setOriginalPublicationYear(zooName.getPublicationYear());
				zooName.setBasionymAuthorTeam(zooName.getCombinationAuthorTeam());
				zooName.setCombinationAuthorTeam(null);
			} else{
				zooName.setBasionymAuthorTeam(null);
				zooName.setAuthorshipCache(zooName.getAuthorshipCache(), true);
				zooName.setCombinationAuthorTeam(null);
			}
			zooName.setPublicationYear(null);
			zooName.setTitleCache(null); // This should (re)generate the titleCache automatically
			if (logger.isDebugEnabled()) {
				logger.debug("Basionym created (" + fauEuTaxon.getId() + ")");
			}

			// create synonym
			Synonym synonym = Synonym.NewInstance(basionym, fauEuConfig.getSourceReference());
			MarkerType markerType =getMarkerType(state, PesiTransformer.uuidMarkerGuidIsMissing, "Uuid is missing", "Uuid is missing", null);
			synonym.addMarker(Marker.NewInstance(markerType, true));
			
			if (fauEuTaxon.isValid()) { // Taxon

				// homotypic synonym
				Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);
				taxon.addHomotypicSynonym(synonym, fauEuConfig.getSourceReference(), null);
				
				if (logger.isDebugEnabled()) {
					logger.debug("Homotypic synonym created (" + fauEuTaxon.getId() + ")");
				}

			} else { // Synonym
				
				// heterotypic synonym
				// synonym relationship to the accepted taxon is created later
				synonymSet.add(synonym);
				
				if (logger.isDebugEnabled()) {
					logger.debug("Heterotypic synonym stored (" + fauEuTaxon.getId() + ")");
				}
			}
			
			
			buildTaxonName(fauEuTaxon, synonym, basionym, true, fauEuConfig);
			
			//originalSources zufügen
			
			ImportHelper.setOriginalSource(synonym, fauEuConfig.getSourceReference(), fauEuTaxon.getId(), PesiTransformer.STR_NAMESPACE_NOMINAL_TAXON);
			ImportHelper.setOriginalSource(synonym.getName(), fauEuConfig.getSourceReference(), fauEuTaxon.getId(), PesiTransformer.STR_NAMESPACE_NOMINAL_TAXON);

			// add originalGenusId as source
//			String originalGenusIdString = String.valueOf(fauEuTaxon.getId());
//			IdentifiableSource basionymSource = IdentifiableSource.NewInstance(originalGenusIdString, "originalGenusId");
//			basionym.addSource(basionymSource);
//			
//			// add original database reference
//			ImportHelper.setOriginalSource(basionym, fauEuConfig.getSourceReference(), fauEuTaxon.getId(), "TaxonName");
			
			
		} catch (Exception e) {
			logger.warn("Exception occurred when creating basionym for " + fauEuTaxon.getId());
			e.printStackTrace();
		}
		
		
		return;
	}
	
	
	/* Build name title cache */
	private String buildNameTitleCache(String nameString, boolean useOriginalGenus, FaunaEuropaeaTaxon fauEuTaxon) {
		
		StringBuilder titleCacheStringBuilder = new StringBuilder(nameString);
		Integer year = fauEuTaxon.getYear();
		if (year != null) { // TODO: Ignore authors like xp, xf, etc?
			titleCacheStringBuilder.append(" ");
			if ((fauEuTaxon.isParenthesis() == true) && !useOriginalGenus) {
				titleCacheStringBuilder.append("(");
			}
			titleCacheStringBuilder.append(fauEuTaxon.getAuthor());
			titleCacheStringBuilder.append(" ");
			titleCacheStringBuilder.append(year);
			if ((fauEuTaxon.isParenthesis() == true) && !useOriginalGenus) {
				titleCacheStringBuilder.append(")");
			}
		}
		return titleCacheStringBuilder.toString();
	}


	/* Build taxon title cache */
	private String buildTaxonTitleCache(String nameCache, Reference<?> reference) {
		
		StringBuilder titleCacheStringBuilder = new StringBuilder(nameCache);
		titleCacheStringBuilder.append(" sec. ");
		titleCacheStringBuilder.append(reference.getTitleCache());
		return titleCacheStringBuilder.toString();
	}

	
	/* Build name full title cache */
	private String buildNameFullTitleCache(String titleCache, FaunaEuropaeaImportConfigurator fauEuConfig) {
		
		StringBuilder fullTitleCacheStringBuilder = new StringBuilder(titleCache);
		fullTitleCacheStringBuilder.append(" ");
		fullTitleCacheStringBuilder.append(fauEuConfig.getSourceReferenceTitle());
		return fullTitleCacheStringBuilder.toString();
	}
	
	
	private String genusPart(StringBuilder originalGenusName, boolean useOriginalGenus, 
			StringBuilder genusOrUninomial) {

		StringBuilder stringBuilder = new StringBuilder();
		
		if(useOriginalGenus) {
			stringBuilder.append(originalGenusName);
			genusOrUninomial.delete(0, genusOrUninomial.length());
			genusOrUninomial.append(originalGenusName);
		} else {
			stringBuilder.append(genusOrUninomial);
		}
		stringBuilder.append(" ");

		return stringBuilder.toString();
	}

	
	private String genusSubGenusPart(StringBuilder originalGenusName, boolean useOriginalGenus, 
			StringBuilder genusOrUninomial,
			StringBuilder infraGenericEpithet,
			FaunaEuropaeaTaxon fauEuTaxon) {

		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append(genusPart(originalGenusName, useOriginalGenus, genusOrUninomial));

		// The infraGenericEpithet is set to empty only if the original genus should be used and
		// the actualGenusId is not the originalGenusId.
		// This differentiation is relevant for synonyms and for basionyms.
		// InfraGenericEpithets of accepted taxa are not touched at all.
		Integer originalGenusId = fauEuTaxon.getOriginalGenusId();
		Integer actualGenusId = getActualGenusId(fauEuTaxon);
		if (useOriginalGenus && 
				originalGenusId.intValue() != actualGenusId.intValue() && 
				originalGenusId.intValue() > 0 &&
				actualGenusId.intValue() > 0) {
			infraGenericEpithet.delete(0, infraGenericEpithet.length());
			stringBuilder.append(" ");
			return stringBuilder.toString();
		}

		stringBuilder.append("(");
		stringBuilder.append(infraGenericEpithet);
		stringBuilder.append(")");
		stringBuilder.append(" ");
		
		return stringBuilder.toString();
	}

	/** Get actual genus id **/
	private Integer getActualGenusId(FaunaEuropaeaTaxon fauEuTaxon) {
		Integer actualGenusId = null;
		HashMap<Integer, Integer> ranks = new HashMap<Integer, Integer>();
		ranks.put(fauEuTaxon.getParentRankId(), fauEuTaxon.getParentId());
		ranks.put(fauEuTaxon.getGrandParentRankId(), fauEuTaxon.getGrandParentId());
		ranks.put(fauEuTaxon.getGreatGrandParentRankId(), fauEuTaxon.getGreatGrandParentId());
		ranks.put(fauEuTaxon.getGreatGreatGrandParentRankId(), fauEuTaxon.getGreatGreatGrandParentId());
		ranks.put(fauEuTaxon.getGreatGreatGreatGrandParentRankId(), fauEuTaxon.getGreatGreatGreatGrandParentId());
		
		actualGenusId = ranks.get(R_GENUS);

		return actualGenusId;
	}
	
	
	/** Build species and subspecies names */
	private String buildLowerTaxonName(StringBuilder originalGenus, boolean useOriginalGenus, 
			StringBuilder genusOrUninomial, StringBuilder infraGenericEpithet, 
			StringBuilder specificEpithet, StringBuilder infraSpecificEpithet,
			FaunaEuropaeaTaxon fauEuTaxon) {
		
		// species or subspecies name
		String localName = fauEuTaxon.getLocalName();
		int taxonId = fauEuTaxon.getId();
		int parentId = fauEuTaxon.getParentId();
		StringBuilder nameCacheStringBuilder = new StringBuilder();

//		FaunaEuropaeaTaxon parent = fauEuTaxonMap.get(parentId);
		if (parentId == 0) {
			nameCacheStringBuilder.append(localName);
			if (logger.isInfoEnabled()) {
				logger.info("Parent of (" + taxonId + ") is null");
			}
			return nameCacheStringBuilder.toString();
		}
		
		String parentName = fauEuTaxon.getParentName();
		String grandParentName = fauEuTaxon.getGrandParentName();
		String greatGrandParentName = fauEuTaxon.getGreatGrandParentName();
		int rank = fauEuTaxon.getRankId();
		int parentRankId = fauEuTaxon.getParentRankId();
		int grandParentRankId = fauEuTaxon.getGrandParentRankId();
		int greatGrandParentRankId = fauEuTaxon.getGreatGrandParentRankId();
//		int grandParentId = fauEuTaxon.getGrandParentId();
//		int greatGrandParentId = grandParent.getParentId();

		
		if (fauEuTaxon.isValid()) { // Taxon
			
			if (rank == R_SPECIES) {

				if(parentRankId == R_SUBGENUS) {
					//differ between isParanthesis= true and false
					String genusSubGenusPart = genusSubGenusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(grandParentName), 
							infraGenericEpithet.append(parentName),
							fauEuTaxon);
						nameCacheStringBuilder.append(genusSubGenusPart);
					

				} else if(parentRankId == R_GENUS) {

					String genusPart = genusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(parentName));
					nameCacheStringBuilder.append(genusPart);
				}
				nameCacheStringBuilder.append(localName);
				specificEpithet.append(localName);

			} else if (rank == R_SUBSPECIES) {

				if(grandParentRankId == R_SUBGENUS) {

					String genusSubGenusPart = genusSubGenusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(greatGrandParentName), 
							infraGenericEpithet.append(grandParentName),
							fauEuTaxon);
					nameCacheStringBuilder.append(genusSubGenusPart);

				} else if (grandParentRankId == R_GENUS) {

					String genusPart = genusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(grandParentName));
					nameCacheStringBuilder.append(genusPart);

				}
				nameCacheStringBuilder.append(parentName);
				nameCacheStringBuilder.append(" ");
				nameCacheStringBuilder.append(localName);
				specificEpithet.append(parentName);
				infraSpecificEpithet.append(localName);
			}
		} else { // Synonym or misapplied name
			
			if (rank == R_SPECIES) {

				if(grandParentRankId == R_SUBGENUS) {
					
					String genusSubGenusPart = genusSubGenusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(greatGrandParentName), 
							infraGenericEpithet.append(grandParentName),
							fauEuTaxon);
					nameCacheStringBuilder.append(genusSubGenusPart);

				} else if (grandParentRankId == R_GENUS) {
					
					String genusPart = genusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(grandParentName));
					nameCacheStringBuilder.append(genusPart);

				}
				nameCacheStringBuilder.append(localName);
				specificEpithet.append(localName);

			} else if (rank == R_SUBSPECIES) {
				
				String greatGreatGrandParentName = fauEuTaxon.getGreatGreatGrandParentName();
				
				if(greatGrandParentRankId == R_SUBGENUS) {
					
					String genusSubGenusPart = genusSubGenusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(greatGreatGrandParentName), 
							infraGenericEpithet.append(greatGrandParentName),
							fauEuTaxon);
					nameCacheStringBuilder.append(genusSubGenusPart);
					
				} else if (greatGrandParentRankId == R_GENUS) {
					
					String genusPart = genusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(greatGreatGrandParentName));
					nameCacheStringBuilder.append(genusPart);
				}
				
				nameCacheStringBuilder.append(grandParentName);
				nameCacheStringBuilder.append(" ");
				specificEpithet.append(grandParentName);
				nameCacheStringBuilder.append(localName);
				infraSpecificEpithet.append(localName);
			}
			
			
			
		}
		
		return nameCacheStringBuilder.toString();
	}
	
	
	/** Build taxon's name parts and caches */
	private String buildTaxonName(FaunaEuropaeaTaxon fauEuTaxon, TaxonBase<?> taxonBase, TaxonNameBase<?,?>taxonName,
			boolean useOriginalGenus, FaunaEuropaeaImportConfigurator fauEuConfig) {

		/* Local taxon name string */
		String localString = "";
		/* Concatenated taxon name string */
		String completeString = "";

		StringBuilder acceptedGenus = new StringBuilder("");
		
		StringBuilder genusOrUninomial = new StringBuilder();
		StringBuilder infraGenericEpithet = new StringBuilder(); 
		StringBuilder specificEpithet = new StringBuilder();
		StringBuilder infraSpecificEpithet = new StringBuilder();

		localString = fauEuTaxon.getLocalName();
		int rank = fauEuTaxon.getRankId();
		
		// determine genus: this also works for cases of synonyms since the accepted taxon is its parent
		String acceptedGenusString = null;
		String tempAcceptedGenusString = determineAcceptedGenus(fauEuTaxon);
		if (useOriginalGenus && ! "".equals(fauEuTaxon.getOriginalGenusName()) && !fauEuTaxon.getOriginalGenusName().equals(tempAcceptedGenusString) ) {
			acceptedGenusString  = fauEuTaxon.getOriginalGenusName();
		} else {
			acceptedGenusString = tempAcceptedGenusString;
		}

		if (acceptedGenusString != null) {
			acceptedGenus = new StringBuilder(acceptedGenusString);
		}

		if(logger.isDebugEnabled()) { 
			logger.debug("Local taxon name (rank = " + rank + "): " + localString); 
		}

		if (rank < R_SPECIES) {
			// subgenus or above

			completeString = localString;
			if (rank == R_SUBGENUS) {
				// subgenus part
				infraGenericEpithet.append(localString);
				
				// genus part
				genusOrUninomial.append(acceptedGenus);
				
				completeString = acceptedGenus + " ("+ localString + ")";
			} else {
				// genus or above
				genusOrUninomial.append(localString);
			}
			
		} else {
			// species or below

			taxonBase = taxonBase.deproxy(taxonBase, TaxonBase.class);

			completeString = 
				buildLowerTaxonName(acceptedGenus, useOriginalGenus, 
						genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet,
						fauEuTaxon);
			
			completeString = (String) CdmUtils.removeDuplicateWhitespace(completeString.trim());

		}
		return setCompleteTaxonName(completeString, useOriginalGenus,
				genusOrUninomial.toString(), infraGenericEpithet.toString(), 
				specificEpithet.toString(), infraSpecificEpithet.toString(),
				fauEuTaxon, taxonBase, fauEuConfig);
		 
	}
	
	
	/**
	 * Determines the original genus name by searching the taxon with rank Genus.
	 * @param fauEuTaxon
	 * @return
	 */
	private String determineAcceptedGenus(FaunaEuropaeaTaxon fauEuTaxon) {
		String originalGenus = null;

		HashMap<Integer, String> ranks = new HashMap<Integer, String>();
		ranks.put(fauEuTaxon.getParentRankId(), fauEuTaxon.getParentName());
		ranks.put(fauEuTaxon.getGrandParentRankId(), fauEuTaxon.getGrandParentName());
		ranks.put(fauEuTaxon.getGreatGrandParentRankId(), fauEuTaxon.getGreatGrandParentName());
		ranks.put(fauEuTaxon.getGreatGreatGrandParentRankId(), fauEuTaxon.getGreatGreatGrandParentName());
		ranks.put(fauEuTaxon.getGreatGreatGreatGrandParentRankId(), fauEuTaxon.getGreatGreatGreatGrandParentName());
		
		originalGenus = ranks.get(R_GENUS);

		return originalGenus;
	}

	/** Sets name parts and caches */
	private String setCompleteTaxonName(String concatString, boolean useOriginalGenus,
			String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, 
			FaunaEuropaeaTaxon fauEuTaxon, TaxonBase<?> taxonBase, FaunaEuropaeaImportConfigurator fauEuConfig) {

		boolean success = true;
		
		TaxonNameBase<?,?> taxonName = taxonBase.getName();
		ZoologicalName zooName = (ZoologicalName)taxonName;
		
		if (!genusOrUninomial.equals("")) {
			zooName.setGenusOrUninomial(emptyToNull(genusOrUninomial));
			if (logger.isDebugEnabled()) { 
				logger.debug("genusOrUninomial: " + genusOrUninomial); 
			}
		}
		
		//if ((!infraGenericEpithet.equals("") && fauEuTaxon.isParenthesis()) || (!infraGenericEpithet.equals("") && fauEuTaxon.)) {
		if (fauEuTaxon.getParentRankId() == R_SUBGENUS || fauEuTaxon.getRankId() == R_SUBGENUS ||
				fauEuTaxon.getGrandParentRankId() == R_SUBGENUS || fauEuTaxon.getGreatGrandParentRankId() == R_SUBGENUS) {
			zooName.setInfraGenericEpithet(emptyToNull(infraGenericEpithet));
			if (logger.isDebugEnabled()) { 
				logger.debug("infraGenericEpithet: " + infraGenericEpithet); 
			}
		}
		if ((fauEuTaxon.getRankId() == R_SPECIES || fauEuTaxon.getRankId() == R_SUBSPECIES)) {
			zooName.setSpecificEpithet(emptyToNull(specificEpithet));
			if (logger.isDebugEnabled()) { 
				logger.debug("specificEpithet: " + specificEpithet); 
			}
		}
		if (fauEuTaxon.getRankId() == R_SUBSPECIES) {
			zooName.setInfraSpecificEpithet(emptyToNull(infraSpecificEpithet));
			if (logger.isDebugEnabled()) { 
				logger.debug("infraSpecificEpithet: " + infraSpecificEpithet); 
			}
		}
		//TODO: use generate NameCache
		//zooName.setNameCache(concatString);
		String result = zooName.getNameCache();
//		zooName.generateTitle();
		//String titleCache = buildNameTitleCache(concatString, useOriginalGenus, fauEuTaxon);
		//zooName.setTitleCache(titleCache);
		//titleCache = buildNameFullTitleCache(concatString, fauEuConfig);
//		zooName.generateFullTitle();
		//zooName.setFullTitleCache(titleCache); // TODO: Add reference, NC status
		
//		ImportHelper.setOriginalSource(taxonName, fauEuConfig.getSourceReference(), 
//				fauEuTaxon.getId(), "TaxonName");
//		taxonBase.setSec(fauEuConfig.getSourceReference());
//		taxonBase.generateTitle();
		//titleCache = buildTaxonTitleCache(concatString, fauEuConfig.getSourceReference());
		//taxonBase.setTitleCache(titleCache);
		
		if (logger.isDebugEnabled()) { 
			logger.debug("Name stored: " + result); 
		}
		return result;
	}

	/**
	 * Ensures that empty strings are translated to null.
	 * @param genusOrUninomial
	 * @return
	 */
	private String emptyToNull(String text) {
		if (CdmUtils.isEmpty(text)) {
			return null;
		} else {
			return text;
		}
	}
	
	

}
