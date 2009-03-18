/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelAuthorExport extends BerlinModelExportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelAuthorExport.class);

	private static int modCount = 1000;

	public BerlinModelAuthorExport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IExportConfigurator config){
		boolean result = true;
		logger.warn("Checking for Authors not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IExportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){ 

		MapWrapper<AgentBase> teamMap = (MapWrapper<AgentBase>)stores.get(ICdmIO.AUTHOR_STORE);
		
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;

		logger.info("start makeAuthors ...");
		boolean success = true ;
		
		
		
		//get data from database
		String strQuery = 
				" SELECT *  " +
                " FROM AuthorTeam " ;
		ResultSet rs = source.getResultSet(strQuery) ;
		String namespace = "AuthorTeam";
		
		int i = 0;
		//for each reference
		try{
			while (rs.next()){
				
				if ((i++ % modCount ) == 0 && i!= 1 ){ logger.info("Authors handled: " + (i-1));}
				
				//create Agent element
				int teamId = rs.getInt("AuthorTeamId");
				
				TeamOrPersonBase team = new Team();
				
				dbAttrName = "AuthorTeamCache";
				cdmAttrName = "nomenclaturalTitle";
				success &= ImportHelper.addStringValue(rs, team, dbAttrName, cdmAttrName);

				dbAttrName = "AuthorTeamCache";
				cdmAttrName = "titleCache";
				success &= ImportHelper.addStringValue(rs, team, dbAttrName, cdmAttrName);

				//TODO
				//FullAuthorTeamCache
				//preliminaryFlag
				//title cache or nomenclaturalTitle?

				//created, notes
	//			doIdCreatedUpdatedNotes(config, team, rs, teamId, namespace);

				teamMap.put(teamId, team);
			} //while rs.hasNext()
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

			
		logger.info(i + " authors handled");
		getAgentService().saveAgentAll(teamMap.objects());

		logger.info("end make authors ...");
		return success;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IExportConfigurator config){
		return ! ((BerlinModelExportConfigurator)config).isDoAuthors();
	}

}
