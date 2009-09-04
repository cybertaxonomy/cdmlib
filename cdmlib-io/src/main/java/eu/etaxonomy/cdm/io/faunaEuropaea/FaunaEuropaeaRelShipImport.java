/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.A_AUCT;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.P_PARENTHESIS;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.Q_NO_RESTRICTION;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.R_GENUS;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.R_SUBGENUS;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.R_SPECIES;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.R_SUBSPECIES;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_ACCEPTED;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_NOT_ACCEPTED;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.CdmOneToManyMapper;
import eu.etaxonomy.cdm.io.berlinModel.CdmStringMapper;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.CdmAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.tcsxml.in.TcsXmlImportState;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.PublicationBase;
import eu.etaxonomy.cdm.model.reference.Publisher;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaRelShipImport extends FaunaEuropaeaImportBase  {
	
	public static final String OS_NAMESPACE_TAXON = "Taxon";
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaRelShipImport.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(FaunaEuropaeaImportState state) {
		boolean result = true;
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		logger.warn("Checking for Taxa not yet fully implemented");
		result &= checkTaxonStatus(fauEuConfig);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(FaunaEuropaeaImportState state) {
		return ! state.getConfig().isDoTaxa();
	}

	private boolean checkTaxonStatus(FaunaEuropaeaImportConfigurator fauEuConfig) {
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

	
	/* 
	 * Import with complete taxon store
	 */
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				
		
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		authorStore.makeEmpty();
		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = state.getFauEuTaxonMap();
		TransactionStatus txStatus = null;
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }
		
		txStatus = startTransaction();

		success = processTaxa(state, fauEuTaxonMap);
		success = saveTaxa(state, state.getHighestTaxonIndex(), state.getConfig().getLimitSave());
		
		commitTransaction(txStatus);

		logger.info("End making taxa...");
		return success;
	}

	
	/*
	 * Processes taxa from complete taxon store
	 */
	private boolean processTaxa(FaunaEuropaeaImportState state, 
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		if(logger.isInfoEnabled()) { logger.info("Processing taxa second pass..."); }

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();

		boolean success = true;

		for (int id : fauEuTaxonMap.keySet())
		//for (int id : taxonStore.keySet())
		{
			TaxonBase<?> taxonBase = taxonStore.get(id);
			TaxonNameBase<?,?> taxonName = taxonBase.getName();
			FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(id);
			
			if (logger.isDebugEnabled()) { logger.debug("Taxon # " + id); }
			createRelationships(fauEuTaxon, taxonBase, taxonName, fauEuTaxonMap, state);
		}
		return success;	
	}
	
	
	/** Creates relationships for existing taxon store in memory */
	private boolean createRelationships(FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, TaxonNameBase<?,?> taxonName,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, FaunaEuropaeaImportState state) {

		int parentId = fauEuTaxon.getParentId();
		int taxonId = fauEuTaxon.getId();
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		ReferenceBase<?> sourceRef = state.getConfig().getSourceReference();
		TaxonBase<?> parentTaxonBase = taxonStore.get(parentId);
		Taxon parentTaxon = parentTaxonBase.deproxy(parentTaxonBase, Taxon.class);
//		FaunaEuropaeaTaxon parent = fauEuTaxonMap.get(parentId);
		boolean success = true;

		if (!fauEuTaxon.isValid()) { // FauEu Synonym

			if (fauEuTaxon.getAuthor() != null && fauEuTaxon.getAuthor().equals("A_AUCT_NAME")) {
				try {
					// add misapplied name relationship from this taxon to parent
					Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);
					taxon.addMisappliedName(parentTaxon, sourceRef, null);
					if (logger.isInfoEnabled()) {
						logger.info("Misapplied name created " + taxon.getUuid());
					}

				} catch (Exception e) {
					logger.error("Error creating misapplied name relationship for taxon (" + 
							parentId + ")");
				}
			}

			else if((fauEuTaxon.getAuthor() == null) 
					|| (fauEuTaxon.getAuthor() != null && !fauEuTaxon.getAuthor().equals("A_AUCT_NAME"))) {
				try {
					// add this synonym as heterotypic synonym to parent
					Synonym synonym = taxonBase.deproxy(taxonBase, Synonym.class);
					parentTaxon.addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
					if (logger.isDebugEnabled()) {
						logger.debug("Heterotypic synonym created " + synonym.getUuid());
					}

				} catch (Exception e) {
					logger.error("Error creating heterotypic synonym for taxon (" + parentId + ")");
//					e.printStackTrace();
				}
			}

		} else if (fauEuTaxon.isValid()) { // FauEu Taxon

			Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);

			try {
				// add this taxon as child to parent
				if (parentTaxon != null) {
					makeTaxonomicallyIncluded(state, parentTaxon, taxon, sourceRef, null);
					if (logger.isDebugEnabled()) {
						logger.debug("Parent-child (" + parentId + "-" + taxonId + 
						") relationship created");
					}
				}

			} catch (Exception e) {
				logger.error("Error creating taxonomically included relationship Parent-child (" + 
						parentId + "-" + taxonId + ")");
			}
		}

		return success;
	}
	

	private boolean makeTaxonomicallyIncluded(FaunaEuropaeaImportState state, Taxon toTaxon, Taxon fromTaxon, ReferenceBase citation, String microCitation){
		boolean success = true;
		ReferenceBase sec = toTaxon.getSec();
		TaxonomicTree tree = state.getTree(sec);
		if (tree == null){
			tree = makeTree(state, sec);
		}
		success = tree.addParentChild(toTaxon, fromTaxon, citation, microCitation);
		return success;
	}
	
}
