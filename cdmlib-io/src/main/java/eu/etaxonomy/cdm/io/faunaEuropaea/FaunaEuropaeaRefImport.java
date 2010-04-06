/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


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
		int i = 0;

		String selectCount = 
			" SELECT count(*) ";

		String selectColumns = 
			" SELECT Reference.*, TaxRefs.*, Taxon.UUID  ";
			
		String fromClause = 
			" FROM TaxRefs " +
			" INNER JOIN Reference ON Reference.ref_id = TaxRefs.trf_ref_id " +
			" INNER JOIN Taxon ON TaxRefs.trf_tax_id = Taxon.TAX_ID ";
		
		String orderClause = 
			" ORDER BY TaxRefs.trf_tax_id";
			
		String countQuery = 
			selectCount + fromClause;

		String selectQuery = 
			selectColumns + fromClause + orderClause;

		if(logger.isInfoEnabled()) { logger.info("Start making References..."); }

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

				ReferenceBase<?> reference = null;
				TeamOrPersonBase<Team> author = null;
				ReferenceFactory refFactory = ReferenceFactory.newInstance();
				reference = refFactory.newGeneric();
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
					if (logger.isTraceEnabled()) { 
						logger.trace("Stored reference (" + refAuthor + ")"); 
					}
				} else {
					if (logger.isDebugEnabled()) { 
						logger.debug("Duplicated reference (" + refId + ", " + refAuthor + ")");
					}
					continue;
				}

				// Store author

				boolean store = true;
				if (!authors.contains(refId)) {
					if (refAuthor == null) {
						logger.warn("Reference author is null");
					}
					String storedAuthorTitleCache = null;
					for (TeamOrPersonBase<?> storedAuthor : authors) {
						storedAuthorTitleCache = storedAuthor.getTitleCache();
						if (storedAuthorTitleCache.equals(refAuthor)) {
							store = false;
							if (logger.isDebugEnabled()) {
								logger.debug("Duplicated author (" + refId + ", " + refAuthor + ")");
							}
							break;
						}
					}
					if (store == true) { 
						authors.add(author); 
						if (logger.isTraceEnabled()) { 
							logger.trace("Stored author (" + refAuthor + ")");
						}
					}

				} else {
					if (logger.isDebugEnabled()) { 
						logger.debug("Not imported author with duplicated aut_id (" + refId + 
								") " + refAuthor);
					}
				}

				fauEuTaxonMap.get(currentTaxonUuid).addReference(fauEuReference);

				Taxon taxon = null;
				if (((i % limit) == 0 && i != 1 ) || i == count) { 

					try {

						taxonList = getTaxonService().find(taxonUuids);

						for (TaxonBase taxonBase : taxonList) {

							// Create descriptions

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
//									if (logger.isDebugEnabled()) { 
										logger.warn("Synonym (" + taxonBase.getUuid() + ") does not have accepted taxa");
//									}
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
									
									ReferenceBase citation = storedReference.getCdmReference();
									String microCitation = storedReference.getPage();
									DescriptionElementSource originalSource = DescriptionElementSource.NewInstance(null, null, citation, microCitation, null, null);
									if (isSynonym){
										Synonym syn = CdmBase.deproxy(taxonBase, Synonym.class);
										originalSource.setNameUsedInSource(syn.getName());
									}
									textData.addSource(originalSource);
									taxonDescription.addElement(textData);
								}
							}
						}
						if(logger.isInfoEnabled()) { 
							logger.info("i = " + i + " - Transaction committed"); 
						}

						// save taxa, references, and authors
						getTaxonService().save(taxonList);
						getReferenceService().save(references);
						getAgentService().save((Collection)authors);

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
