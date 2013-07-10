/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.faunaEuropaea;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaAuthorImport extends FaunaEuropaeaImportBase {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaAuthorImport.class);

	private static int modCount = 1000;

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(FaunaEuropaeaImportState state){
		boolean result = true;
		logger.warn("No checking for Authors not implemented");
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected void doInvoke(FaunaEuropaeaImportState state){ 
		/*
		logger.warn("Start author doInvoke");
		ProfilerController.memorySnapshot();
		*/
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		TransactionStatus txStatus = null;
		
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();
		
		String namespace = "AuthorTeam";
		
		if(logger.isInfoEnabled()) { logger.info("Start making Authors..."); }
		
		try {

			String strQuery = 
				" SELECT *  " +
				" FROM author " ;
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			while (rs.next()) {

				if ((i++ % modCount) == 0 && i!= 1 ) { 
					if(logger.isDebugEnabled()) {
						logger.debug("Authors retrieved: " + (i-1)); 
					}
				}

				int authorId = rs.getInt("aut_id");
				String authorName = rs.getString("aut_name");

				String auctWithNecRegEx = "\\bauct\\b\\.?.*\\bnec\\b\\.?.*";
				String necAuctRegEx = "\\bnec\\b\\.?.*\\bauct\\b\\.?.*";
				
				boolean auctWithNecFound = expressionMatches(auctWithNecRegEx, authorName);
				boolean necAuctFound = expressionMatches(necAuctRegEx, authorName);
				if (auctWithNecFound){
					logger.debug("authorName before auct nec string is removed" + authorName);
					authorName = authorName.substring(expressionEnd("nec\\.?", authorName)+1, authorName.length());
					logger.debug("authorName after auct nec string is removed" + authorName);
				}
				
				if (necAuctFound){
					logger.debug("authorName before nec auct string is removed" + authorName);
					authorName = authorName.substring(0, authorName.indexOf("nec")-1);
					logger.debug("authorName before nec auct string is removed" + authorName);
				}
				TeamOrPersonBase<Team> author = null;

				try {
					author = Team.NewInstance();
					author.setTitleCache(authorName, true);

					ImportHelper.setOriginalSource(author, fauEuConfig.getSourceReference(), authorId, namespace);

					if (!authorStore.containsId(authorId)) {
						authorStore.put(authorId, author);
						if (logger.isDebugEnabled()) { logger.debug("Stored author (" + authorId + ") " + authorName); }
					} else {
						logger.warn("Not imported author with duplicated aut_id (" + authorId + ") " + authorName);
					}
				} catch (Exception e) {
					logger.warn("An exception occurred when creating author with id " + authorId + ". Author could not be saved.");
				}
			}
			
			if(logger.isInfoEnabled()) { logger.info("Saving authors ..."); }

			txStatus = startTransaction();

			// save authors
			getAgentService().save((Collection)authorStore.objects());

			commitTransaction(txStatus);
			
			if(logger.isInfoEnabled()) { logger.info("End making authors ..."); }

			return;

		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			state.setUnsuccessfull();
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(FaunaEuropaeaImportState state){
		return ! state.getConfig().isDoAuthors();
	}

}
