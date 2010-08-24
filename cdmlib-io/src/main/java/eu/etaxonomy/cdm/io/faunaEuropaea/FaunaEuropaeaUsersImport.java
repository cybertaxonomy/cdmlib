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
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.babadshanjan
 * @created 23.08.2010
 * @version 1.0
 */
@Component
public class FaunaEuropaeaUsersImport extends FaunaEuropaeaImportBase {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaUsersImport.class);

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
		Map<Integer, ReferenceBase> references = null;
		Map<String,TeamOrPersonBase> authors = null;
		Map<Integer, UUID> referenceUuids = new HashMap<Integer, UUID>();
		int limit = state.getConfig().getLimitSave();

		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();

		String namespace = "Reference";
		boolean success = true;
		int i = 0;

		String selectCountUsers = 
			" SELECT count(*) FROM Users";

		String selectColumnsUsers = 
			" SELECT * FROM Users";

		int count;
		if(logger.isInfoEnabled()) { logger.info("Start making References (Users)..."); }

		try {
			ResultSet rsRefs = source.getResultSet(selectCountUsers);
			rsRefs.next();
			count = rsRefs.getInt(1);
			
			rsRefs = source.getResultSet(selectColumnsUsers);

	        if (logger.isInfoEnabled()) {
	        	logger.info("Get all References..."); 
				logger.info("Number of rows: " + count);
				logger.info("Count Query: " + selectCountUsers);
				logger.info("Select Query: " + selectColumnsUsers);
			}
	        
	        while (rsRefs.next()){
	        	int refId = rsRefs.getInt("usr_id");
				String refTitle = rsRefs.getString("usr_title");
				String refFirstname = rsRefs.getString("usr_firstname");
				String refLastname = rsRefs.getString("usr_lastname");
				String createdDate = rsRefs.getString("usr_createdat");

				// build author
				String refAuthor = "";
				if (refTitle != null) {
					refAuthor = refTitle;
					if (! refTitle.endsWith(".")) {
						refAuthor += ".";
					}
				}
				refAuthor += refTitle == null ? NullToEmpty(refFirstname) : " " + NullToEmpty(refFirstname);
				if ((refTitle != null || refFirstname != null) && refLastname != null) {
					refAuthor += " " + refLastname;
				}

				// build year
				String year = null;
				if (createdDate != null) {
					year = createdDate.substring(0, createdDate.indexOf("-"));
				}
				
				if ((i++ % limit) == 0) {

					txStatus = startTransaction();
					references = new HashMap<Integer,ReferenceBase>(limit);
					authors = new HashMap<String,TeamOrPersonBase>(limit);
					
					if(logger.isInfoEnabled()) {
						logger.info("i = " + i + " - Reference import transaction started"); 
					}
				}
				
				ReferenceBase<?> reference = null;
				TeamOrPersonBase<Team> author = null;
				reference = ReferenceFactory.newGeneric();

				reference.setTitle(refAuthor);
				reference.setDatePublished(ImportHelper.getDatePublished(year));
				
				if (!authors.containsKey(refAuthor)) {
					if (refAuthor == null) {
						logger.warn("Reference author is null");
					}
					author = Team.NewInstance();
					author.setTitleCache(refAuthor, true);
					authors.put(refAuthor,author); 
					if (logger.isTraceEnabled()) { 
						logger.trace("Stored author (" + refAuthor + ")");
					}
				//}

				} else {
					author = authors.get(refAuthor);
					if (logger.isDebugEnabled()) { 
						logger.debug("Not imported author with duplicated aut_id (" + refId + 
							") " + refAuthor);
					}
				}
				
				reference.setAuthorTeam(author);
				
				ImportHelper.setOriginalSource(reference, fauEuConfig.getSourceReference(), refId, namespace);
				ImportHelper.setOriginalSource(author, fauEuConfig.getSourceReference(), refId, namespace);

				
				// Store reference
				if (!references.containsKey(refId)) {

					if (reference == null) {
						logger.warn("Reference is null");
					}
					references.put(refId, reference);
					if (logger.isTraceEnabled()) { 
						logger.trace("Stored reference (" + refAuthor + ")"); 
					}
				} else {
					if (logger.isDebugEnabled()) { 
						logger.debug("Duplicated reference (" + refId + ", " + refAuthor + ")");
					}
					//continue;
				}
				
				if (((i % limit) == 0 && i > 1 ) || i == count) { 
					
					Map <UUID, ReferenceBase> referenceMap =getReferenceService().save(references.values());
					logger.info("i = " + i + " - references saved"); 

					Iterator<Entry<UUID, ReferenceBase>> it = referenceMap.entrySet().iterator();
					while (it.hasNext()){
						ReferenceBase ref = it.next().getValue();
						int refID = Integer.valueOf(((OriginalSourceBase)ref.getSources().iterator().next()).getIdInSource());
						UUID uuid = ref.getUuid();
						referenceUuids.put(refID, uuid);
					}
					references= null;
					getAgentService().save((Collection)authors.values());
					
					authors = null;
					commitTransaction(txStatus);
				}
	        	
	        }
		}catch(SQLException e) {
			logger.error("SQLException:" +  e);
			success = false;
		}

		if(logger.isInfoEnabled()) { logger.info("End making References (Users) ..."); }
		
		return success;
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(FaunaEuropaeaImportState state){
		return (state.getConfig().getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}

}
