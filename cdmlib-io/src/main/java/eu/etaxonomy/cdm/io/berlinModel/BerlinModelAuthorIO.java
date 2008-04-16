/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.agent.Team;


/**
 * @author a.mueller
 *
 */
public class BerlinModelAuthorIO {
	private static final Logger logger = Logger.getLogger(BerlinModelAuthorIO.class);

	private static int modCount = 1000;

	public static boolean invoke(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<Team> teamMap){
		
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
				
				//create TaxonName element
				int teamId = rs.getInt("AuthorTeamId");
				//int rankId = rs.getInt("rankFk");
				//Object nomRefFk = rs.getObject("NomRefFk");
				
				Team team = new Team();
				
				dbAttrName = "AuthorTeamCache";
				cdmAttrName = "titleCache";
				success &= ImportHelper.addStringValue(rs, team, dbAttrName, cdmAttrName);
	
	
				//TODO
				//FullAuthorTeamCache
				//preliminaryFlag
				//created
				//notes
				
				//authorTeamId
				ImportHelper.setOriginalSource(team, bmiConfig.getSourceReference(), teamId);
				
				teamMap.put(teamId, team);
			} //while rs.hasNext()
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

			
		logger.info(i + " authors handled");
		agentService.saveAgentAll(teamMap.objects());
		
//			makeNameSpecificData(nameMap);

		logger.info("end makeTaxonNames ...");
		return success;
	}
}
