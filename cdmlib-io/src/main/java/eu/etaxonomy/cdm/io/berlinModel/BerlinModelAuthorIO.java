/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 *
 */
public class BerlinModelAuthorIO extends BerlinModelIOBase {
	private static final Logger logger = Logger.getLogger(BerlinModelAuthorIO.class);

	private static int modCount = 1000;

	private static final String ioNameLocal = "BerlinModelAuthorIO";
	
	public BerlinModelAuthorIO(boolean ignore){
		super(ioNameLocal, ignore);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
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
	protected boolean doInvoke(IImportConfigurator config, 
			CdmApplicationController cdmApp, 
			Map<String, MapWrapper<? extends CdmBase>> stores){ 

		MapWrapper<TeamOrPersonBase> teamMap = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.AUTHOR_STORE);
		
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;

		logger.info("start makeAuthors ...");
		boolean success = true ;
		
		IAgentService agentService = cdmApp.getAgentService();
		
		//get data from database
		String strQuery = 
				" SELECT *  " +
                " FROM AuthorTeam " ;
		ResultSet rs = source.getResultSet(strQuery) ;
		
		int i = 0;
		//for each reference
		try{
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Authors handled: " + (i-1));}
				
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
				doIdCreatedUpdatedNotes(config, team, rs, teamId);

				teamMap.put(teamId, team);
			} //while rs.hasNext()
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

			
		logger.info(i + " authors handled");
		agentService.saveAgentAll(teamMap.objects());

		logger.info("end makeTaxonNames ...");
		return success;
	}
}
