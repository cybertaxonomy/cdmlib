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
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.AgentBase;
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

	public FaunaEuropaeaAuthorImport(){
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("No checking for Authors not implemented");
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){ 

		MapWrapper<AgentBase> teamMap = (MapWrapper<AgentBase>)stores.get(ICdmIO.AUTHOR_STORE);
		
		FaunaEuropaeaImportConfigurator fauEuConfig = (FaunaEuropaeaImportConfigurator)config;
		Source source = fauEuConfig.getSource();
		String dbAttrName;
		String cdmAttrName;

		logger.info("Start making authors ...");
		boolean success = true ;
		
		//get data from database
		String strQuery = 
				" SELECT *  " +
                " FROM author " ;
		ResultSet rs = source.getResultSet(strQuery) ;
		String namespace = "AuthorTeam";
		
		int i = 0;
		try{
			while (rs.next()){
				
				if ((i++ % modCount ) == 0 && i!= 1 ){ logger.info("Authors handled: " + (i-1));}
				
				//create Agent element
				int teamId = rs.getInt("aut_id");
				
				TeamOrPersonBase<Team> team = new Team();
				
				dbAttrName = "aut_name";
				cdmAttrName = "nomenclaturalTitle";
				success &= ImportHelper.addStringValue(rs, team, dbAttrName, cdmAttrName);

				dbAttrName = "aut_name";
				cdmAttrName = "titleCache";
				success &= ImportHelper.addStringValue(rs, team, dbAttrName, cdmAttrName);

				//TODO
				//title cache or nomenclaturalTitle?

				teamMap.put(teamId, team);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

		logger.info(i + " authors handled");
		getAgentService().saveAgentAll(teamMap.objects());

		logger.info("End making authors ...");
		return success;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return !config.isDoAuthors();
	}

}
