/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_ACCEPTED;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_NOT_ACCEPTED;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.CdmOneToManyMapper;
import eu.etaxonomy.cdm.io.berlinModel.CdmStringMapper;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.PublicationBase;
import eu.etaxonomy.cdm.model.reference.Publisher;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
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
public class FaunaEuropaeaRefImport extends FaunaEuropaeaImportBase {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaRefImport.class);

	/* Interval for progress info message when retrieving taxa */
	private int modCount = 10000;
	
		
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(FaunaEuropaeaImportState state) {
		boolean result = true;
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		logger.warn("Checking for References not yet fully implemented");
		result &= checkReferenceStatus(fauEuConfig);
		
		return result;
	}
	
	private boolean checkReferenceStatus(FaunaEuropaeaImportConfigurator fauEuConfig) {
		boolean result = true;
//		try {
			Source source = fauEuConfig.getSource();
			String sqlStr = "";
			ResultSet rs = source.getResultSet(sqlStr);
			return result;
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return false;
//		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				
		
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		MapWrapper<ReferenceBase> refStore = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		TransactionStatus txStatus = null;
				
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();
		
		String namespace = "Reference";
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making References..."); }
		
		try {
			String strQuery = 
				" SELECT MAX(TAX_ID) AS TAX_ID FROM dbo.Taxon ";
			
			ResultSet rs = source.getResultSet(strQuery);
			while (rs.next()) {
				int maxTaxonId = rs.getInt("TAX_ID");
//				highestTaxonIndex = maxTaxonId;
			}

			strQuery = 
				" SELECT Reference.*, TaxRefs.* " + 
                " FROM Reference INNER JOIN TaxRefs ON Reference.ref_id = TaxRefs.trf_ref_id " +
                " WHERE (1=1)" + 
                " ORDER BY TaxRefs.trf_tax_id";
			
			rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			while (rs.next()) {
				
				if ((i++ % modCount) == 0 && i!= 1 ) { 
					if(logger.isInfoEnabled()) {
						logger.info("References handled: " + (i-1)); 
					}
				}
				
				int taxonId = rs.getInt("trf_tax_id");
				int refId = rs.getInt("ref_id");
				String refAuthor = rs.getString("ref_author");
				String year = rs.getString("ref_year");
				String title = rs.getString("ref_title");
				String refSource = rs.getString("ref_source");
				String page = rs.getString("trf_page");
				
				StrictReferenceBase<?> reference = null;
				TeamOrPersonBase<Team> author = null;
				
				try {
					reference = Generic.NewInstance();
					reference.setTitleCache(title);
					reference.setDatePublished(ImportHelper.getDatePublished(year));
					author = Team.NewInstance();
					author.setTitleCache(refAuthor);
					
					ImportHelper.setOriginalSource(reference, fauEuConfig.getSourceReference(), refId, namespace);
					ImportHelper.setOriginalSource(author, fauEuConfig.getSourceReference(), refId, namespace);
					
					// Create references
					
					if (!refStore.containsId(refId)) {
						if (reference == null) {
							logger.warn("Reference is null");
						}
						refStore.put(refId, reference);
						if (logger.isDebugEnabled()) { 
							logger.debug("Stored reference (" + refId + ") " + refAuthor); 
						}
					} else {
						if (logger.isDebugEnabled()) { 
							logger.debug("Not imported reference with duplicated ref_id (" + refId + 
									") " + refAuthor);
						}
						continue;
					}
					
					// Create authors
					
					if (!authorStore.containsId(refId)) { // TODO: Don't insert identical author names
						if (refAuthor == null) {
							logger.warn("Reference author is null");
						}
						authorStore.put(refId, author);
						if (logger.isDebugEnabled()) { 
							logger.debug("Stored author (" + refId + ") " + refAuthor); 
						}
					} else {
						if (logger.isDebugEnabled()) { 
							logger.debug("Not imported author with duplicated aut_id (" + refId + 
									") " + refAuthor);
						}
					}
					
				} catch (Exception e) {
					logger.warn("An exception occurred when creating reference with id " + refId + 
					". Reference could not be saved.");
				}
				
				try {
					// Create descriptions

					Taxon taxon;
					TaxonBase taxonBase = taxonStore.get(taxonId);
					if (taxonBase == null) { continue; }
					boolean isSynonym = taxonBase.isInstanceOf(Synonym.class);
					if (isSynonym){
						Synonym syn = CdmBase.deproxy(taxonBase, Synonym.class);
						taxon = syn.getAcceptedTaxa().iterator().next();
					}else{
						taxon = CdmBase.deproxy(taxonBase, Taxon.class);
					}

					TaxonDescription taxonDescription = null;
					Set<TaxonDescription> descriptions = taxon.getDescriptions();
					if (descriptions.size() > 0) {
						taxonDescription = descriptions.iterator().next(); 
					} else {
						taxonDescription = TaxonDescription.NewInstance();
						taxon.addDescription(taxonDescription);
					}

					TextData textData = TextData.NewInstance(Feature.CITATION());
					if (isSynonym){
						Synonym syn = CdmBase.deproxy(taxonBase, Synonym.class);
						textData.setNameUsedInReference(syn.getName());
					}
					textData.setCitation(reference);
					textData.setCitationMicroReference(page);
					taxonDescription.addElement(textData);

				} catch (Exception e) {
					logger.warn("An exception occurred when creating description for reference " + refId + 
					". Taxon description could not be saved.");
					e.printStackTrace();
				}
				
			}
			
			if(logger.isInfoEnabled()) { logger.info("Saving references ..."); }
			
			if (state.getConfig().isUseTransactions()) {
				txStatus = startTransaction();
			}

			// save taxa, references, and authors
			success = saveTaxa(state, state.getHighestTaxonIndex(), state.getConfig().getLimitSave());
			getReferenceService().saveReferenceAll(refStore.objects());
			getAgentService().saveAgentAll(authorStore.objects());
			
			if (state.getConfig().isUseTransactions()) {
				commitTransaction(txStatus);
			}

			if(logger.isInfoEnabled()) { logger.info("End making references ..."); }
			
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			success = false;
		}
		return success;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(FaunaEuropaeaImportState state){
		return (state.getConfig().getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}

}
