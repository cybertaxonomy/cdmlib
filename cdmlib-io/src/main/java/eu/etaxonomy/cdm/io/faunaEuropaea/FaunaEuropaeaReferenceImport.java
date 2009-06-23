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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

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
public class FaunaEuropaeaReferenceImport extends FaunaEuropaeaImportBase {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaReferenceImport.class);

	private int modCount = 10000;
	/* Max number of references to be saved with one service call */
	private int limit = 20000; // TODO: Make configurable
	
		
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(FaunaEuropeaImportState state) {
		boolean result = true;
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		logger.warn("Checking for Taxa not yet fully implemented");
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
	protected boolean doInvoke(FaunaEuropeaImportState state) {				
		
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		authorStore.makeEmpty();
		MapWrapper<ReferenceBase> refStore = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
				
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();
		
		String namespace = "Reference";
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making References..."); }
		
		try {
			String strQuery = 
				" SELECT Reference.*, TaxRefs.* " + 
                " FROM Reference INNER JOIN TaxRefs ON Reference.ref_id = TaxRefs.trf_ref_id " +
                " WHERE (1=1)";
            					
//			String strQuery = 
//				" SELECT Reference.*, TaxRefs.*, author.aut_id " + 
//                " FROM Reference INNER JOIN TaxRefs ON Reference.ref_id = TaxRefs.trf_ref_id " +
//                " INNER JOIN author ON Reference.ref_author = author.aut_name" +
//                " WHERE (1=1)";
			
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			while (rs.next()) {
				
				if ((i++ % modCount) == 0 && i!= 1 ) { 
					if(logger.isInfoEnabled()) {
						logger.info("References handled: " + (i-1)); 
					}
				}
				
				int refId = rs.getInt("ref_id");
				String refAuthor = rs.getString("ref_author");
				String year = rs.getString("ref_year");
				String title = rs.getString("ref_title");
				String refSource = rs.getString("ref_source");
				
				StrictReferenceBase<?> reference = null;
				TeamOrPersonBase<Team> author = null;
				
				try {
					reference = Generic.NewInstance();
					reference.setTitleCache(title);
					reference.setDatePublished(ImportHelper.getDatePublished(year));
					author = Team.NewInstance();
					author.setTitleCache(refAuthor);
					
					// FIXME: author.aut_name and Reference.ref_author don't match
//					if (authorStore != null) {
//						TeamOrPersonBase<?> author = authorStore.get(authorId);
//						if (author != null) {
//							reference.setAuthorTeam(author);
//						}
//					}
										
					ImportHelper.setOriginalSource(reference, fauEuConfig.getSourceReference(), refId, namespace);
					ImportHelper.setOriginalSource(author, fauEuConfig.getSourceReference(), refId, namespace);
					
					// Create reference
					
					if (!refStore.containsId(refId)) {
						if (reference == null) {
							logger.warn("Reference is null");
						}
						refStore.put(refId, reference);
						if (logger.isDebugEnabled()) { 
							logger.debug("Stored reference (" + refId + ") " + refAuthor); 
						}
					} else {
						logger.warn("Not imported reference with duplicated ref_id (" + refId + 
								") " + refAuthor);
					}
					
					// Create authors
					
					if (!authorStore.containsId(refId)) {
						if (refAuthor == null) {
							logger.warn("Reference author is null");
						}
						authorStore.put(refId, author);
						if (logger.isDebugEnabled()) { 
							logger.debug("Stored author (" + refId + ") " + refAuthor); 
						}
					} else {
						logger.warn("Not imported author with duplicated aut_id (" + refId + 
								") " + refAuthor);
					}
					
					
				} catch (Exception e) {
					logger.warn("An exception occurred when creating reference with id " + refId + 
					". Reference could not be saved.");
				}
			}
			
			if(logger.isInfoEnabled()) { logger.info("Saving references ..."); }
			
			// save authors and references
			getReferenceService().saveReferenceAll(refStore.objects());
			getAgentService().saveAgentAll(authorStore.objects());
			
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
	protected boolean isIgnore(FaunaEuropeaImportState state){
		return (state.getConfig().getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}

}
