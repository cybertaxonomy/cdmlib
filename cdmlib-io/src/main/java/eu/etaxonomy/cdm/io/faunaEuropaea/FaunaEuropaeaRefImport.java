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
import java.util.HashSet;
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
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
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
//		ResultSet rs = source.getResultSet(sqlStr);
		return result;
//		} catch (SQLException e) {
//		e.printStackTrace();
//		return false;
//		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				

//		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
//		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
//		MapWrapper<ReferenceBase> refStore = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		TransactionStatus txStatus = null;
		List<TaxonBase> taxonList = null;
		Set<UUID> taxonUuids = null;
		Set<ReferenceBase> references = null;
		Set<TeamOrPersonBase> authors = null;
		Map<UUID, FaunaEuropaeaReferenceTaxon> fauEuTaxonMap = null;
		int limit = state.getConfig().getLimitSave();

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

				if ((i++ % limit) == 0) {

					txStatus = startTransaction();
					taxonUuids = new HashSet<UUID>(limit);
					references = new HashSet<ReferenceBase>(limit);
					authors = new HashSet<TeamOrPersonBase>(limit);
					fauEuTaxonMap = new HashMap<UUID, FaunaEuropaeaReferenceTaxon>(limit);

					if(logger.isInfoEnabled()) {
						logger.info("i = " + i + " - Reference import transaction started"); 
					}
				}

				int taxonId = rs.getInt("trf_tax_id");
				int refId = rs.getInt("ref_id");
				String refAuthor = rs.getString("ref_author");
				String year = rs.getString("ref_year");
				String title = rs.getString("ref_title");
				String refSource = rs.getString("ref_source");
				String page = rs.getString("trf_page");
				UUID currentTaxonUuid = null;
				if (resultSetHasColumn(rs, "UUID")){
					currentTaxonUuid = UUID.fromString(rs.getString("UUID"));
				} else {
					logger.error("Taxon (" + taxonId + ") without UUID ignored");
					continue;
				}

				FaunaEuropaeaReference fauEuReference = new FaunaEuropaeaReference();
				fauEuReference.setTaxonUuid(currentTaxonUuid);
				fauEuReference.setReferenceId(refId);
				fauEuReference.setReferenceAuthor(refAuthor);
				fauEuReference.setReferenceYear(year);
				fauEuReference.setReferenceTitle(title);
				fauEuReference.setReferenceSource(refSource);
				fauEuReference.setPage(page);

				if (!taxonUuids.contains(currentTaxonUuid)) {
					taxonUuids.add(currentTaxonUuid);
					FaunaEuropaeaReferenceTaxon fauEuReferenceTaxon = 
						new FaunaEuropaeaReferenceTaxon(currentTaxonUuid);
					fauEuTaxonMap.put(currentTaxonUuid, fauEuReferenceTaxon);
				} else {
					if (logger.isTraceEnabled()) { 
						logger.trace("Taxon (" + currentTaxonUuid + ") already stored.");
						continue;
					}
				}

				StrictReferenceBase<?> reference = null;
				TeamOrPersonBase<Team> author = null;

				reference = Generic.NewInstance();
				reference.setTitleCache(title);
				reference.setDatePublished(ImportHelper.getDatePublished(year));
				author = Team.NewInstance();
				author.setTitleCache(refAuthor);

				ImportHelper.setOriginalSource(reference, fauEuConfig.getSourceReference(), refId, namespace);
				ImportHelper.setOriginalSource(author, fauEuConfig.getSourceReference(), refId, namespace);

				// Store reference

				if (!references.contains(refId)) {
					if (reference == null) {
						logger.warn("Reference is null");
					}
					references.add(reference);
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

				// Store author

				if (!authors.contains(refId)) {
					if (refAuthor == null) {
						logger.warn("Reference author is null");
					}
					authors.add(author);
					if (logger.isDebugEnabled()) { 
						logger.debug("Stored author (" + refId + ") " + refAuthor); 
					}

					// TODO: 
//					List<TeamOrPersonBase<Team>> matches = getCommonService().findMatching(author, null);
//					if (matches.size() == 0) {
//					authorStore.put(refId, author);
//					if (logger.isDebugEnabled()) { 
//					logger.debug("Stored author (" + refId + ") " + refAuthor); 
//					}
//					} else {
//					if (logger.isDebugEnabled()) { 
//					logger.debug("Matching authors found. Not stored author (" + refId + ") " + refAuthor); 
//					}
//					}
				} else {
					if (logger.isDebugEnabled()) { 
						logger.debug("Not imported author with duplicated aut_id (" + refId + 
								") " + refAuthor);
					}
				}

				fauEuTaxonMap.get(currentTaxonUuid).addReference(fauEuReference);

				if ((i % limit) == 0 && i != 1 ) {

					try {

						taxonList = getTaxonService().findByUuid(taxonUuids);

						for (TaxonBase taxonBase : taxonList) {

							// Create descriptions

							Taxon taxon = null;
							if (taxonBase == null) { 
								if (logger.isDebugEnabled()) { 
									logger.debug("TaxonBase is null (" + currentTaxonUuid + ")");
								}
								continue; 
							}
							boolean isSynonym = taxonBase.isInstanceOf(Synonym.class);
							if (isSynonym) {
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
								Set<TaxonDescription> descriptions = taxon.getDescriptions();
								if (descriptions.size() > 0) {
									taxonDescription = descriptions.iterator().next(); 
								} else {
									taxonDescription = TaxonDescription.NewInstance();
									taxon.addDescription(taxonDescription);
								}


								UUID taxonUuid = taxonBase.getUuid();
								FaunaEuropaeaReferenceTaxon fauEuHelperTaxon = fauEuTaxonMap.get(taxonUuid);

								for (FaunaEuropaeaReference storedReference : fauEuHelperTaxon.getReferences()) {

									TextData textData = TextData.NewInstance(Feature.CITATION());
									if (isSynonym){
										Synonym syn = CdmBase.deproxy(taxonBase, Synonym.class);
										textData.setNameUsedInReference(syn.getName());
									}
									textData.setCitation(storedReference.getCdmReference());
									textData.setCitationMicroReference(storedReference.getPage());
									taxonDescription.addElement(textData);
								}
							}
						}
						if(logger.isInfoEnabled()) { logger.info("Saving references ..."); }

						// save taxa, references, and authors
						getTaxonService().saveTaxonAll(taxonList);
						getReferenceService().saveReferenceAll(references);
						getAgentService().saveAgentAll(authors);

						taxonUuids = null;
						references = null;
						authors = null;
						taxonList = null;
						fauEuTaxonMap = null;
						commitTransaction(txStatus);

					} catch (Exception e) {
						logger.warn("An exception occurred when creating reference with id " + refId + 
						". Reference could not be saved.");
					}
				}
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			success = false;
		}
		if(logger.isInfoEnabled()) { logger.info("End making references ..."); }

		return success;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(FaunaEuropaeaImportState state){
		return (state.getConfig().getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}

}
