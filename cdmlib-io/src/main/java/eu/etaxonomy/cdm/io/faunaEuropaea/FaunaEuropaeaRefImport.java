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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
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
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.match.IMatchable;


/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaRefImport extends FaunaEuropaeaImportBase {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaRefImport.class);

//	private static final List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String []{
//			"descriptions.*"
//			});
//
//	private static final List<String> SYNONYM_INIT_STRATEGY = Arrays.asList(new String []{
//			"synonymRelations"
//			});
	
	/* Interval for progress info message when retrieving taxa */
	private int modCount = 10000;
	
	@Autowired
	private BeanInitializer defaultBeanInitializer;
		
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
//			ResultSet rs = source.getResultSet(sqlStr);
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
//		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		MapWrapper<ReferenceBase> refStore = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		TransactionStatus txStatus = null;
				
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();
		
		String namespace = "Reference";
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making References..."); }
		
		try {
			String strQuery = 
				" SELECT Reference.*, TaxRefs.*, Taxon.UUID " + 
                " FROM TaxRefs " +
				" INNER JOIN Reference ON Reference.ref_id = TaxRefs.trf_ref_id " +
                " INNER JOIN Taxon ON TaxRefs.trf_tax_id = Taxon.TAX_ID " +
                " ORDER BY TaxRefs.trf_tax_id";
			
			if (logger.isInfoEnabled()) {
				logger.info("Query: " + strQuery);
			}
			ResultSet rs = source.getResultSet(strQuery) ;
			
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
				UUID taxonBaseUuid = null;
				if (resultSetHasColumn(rs,"UUID")){
					taxonBaseUuid = UUID.fromString(rs.getString("UUID"));
				} else {
					taxonBaseUuid = UUID.randomUUID();
				}
				
				txStatus = startTransaction();

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
					
					if (!authorStore.containsId(refId)) {
						if (refAuthor == null) {
							logger.warn("Reference author is null");
						}
						
						List<TeamOrPersonBase<Team>> matches = getCommonService().findMatching(author, null);
						if (matches.size() == 0) {
							authorStore.put(refId, author);
							if (logger.isDebugEnabled()) { 
								logger.debug("Stored author (" + refId + ") " + refAuthor); 
							}
						} else {
							if (logger.isDebugEnabled()) { 
								logger.debug("Matching authors found. Not stored author (" + refId + ") " + refAuthor); 
							}
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

					Taxon taxon = null;
					TaxonBase taxonBase = getTaxonService().findByUuid(taxonBaseUuid);
//					TaxonBase taxonBase = getTaxonService().load(taxonBaseUuid, TAXON_INIT_STRATEGY);
//					TaxonBase taxonBase = taxonStore.get(taxonId);
					if (taxonBase == null) { 
						if (logger.isDebugEnabled()) { 
							logger.debug("TaxonBase is null (" + taxonBaseUuid);
						}
						continue; 
					}
					boolean isSynonym = taxonBase.isInstanceOf(Synonym.class);
					if (isSynonym) {
//						taxonBase = getTaxonService().load(taxonBase.getUuid(), SYNONYM_INIT_STRATEGY);
						Synonym syn = CdmBase.deproxy(taxonBase, Synonym.class);
						Set<Taxon> acceptedTaxa = syn.getAcceptedTaxa();
						if (acceptedTaxa.size() > 0) {
							taxon = syn.getAcceptedTaxa().iterator().next();
						} else {
							if (logger.isDebugEnabled()) { 
								logger.debug("Synonym (" + taxonBase.getUuid() + ") does not have accepted taxa");
							}
						}
					} else {
						taxon = CdmBase.deproxy(taxonBase, Taxon.class);
					}

					if (taxon != null) {
						TaxonDescription taxonDescription = null;
//						Hibernate.initialize(taxon);
//						defaultBeanInitializer.initialize(taxon, TAXON_INIT_DESCRIPTIONS_STRATEGY);
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
					}

				} catch (Exception e) {
					logger.warn("An exception occurred when creating description for reference " + refId + 
					". Taxon description could not be saved.");
					e.printStackTrace();
				}
				
			}
			
			if(logger.isInfoEnabled()) { logger.info("Saving references ..."); }
			
			// save taxa, references, and authors
			success = saveTaxa(state, state.getHighestTaxonIndex(), state.getConfig().getLimitSave());
			getReferenceService().saveReferenceAll(refStore.objects());
			getAgentService().saveAgentAll(authorStore.objects());
			
			commitTransaction(txStatus);

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
