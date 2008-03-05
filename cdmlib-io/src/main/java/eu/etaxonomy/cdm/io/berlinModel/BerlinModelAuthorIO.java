/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.agent.Agent;

/**
 * @author a.mueller
 *
 */
public class BerlinModelAuthorIO {
	private static final Logger logger = Logger.getLogger(BerlinModelAuthorIO.class);

	private static int modCount = 1000;

	public static boolean invoke(ReferenceBase berlinModelRef, Source source, CdmApplicationController cdmApp, boolean deleteAll, 
			MapWrapper<Agent> agentMap){
		
		String dbAttrName;
		String cdmAttrName;

		logger.info("start makeAuthors ...");
		logger.warn("Authors not yet implemented !!");

//		IAgentService agentService = cdmApp.getAgentService();
		boolean delete = deleteAll;
		
		//		if (delete){
		//			List<Agent> listAllAgents =  agentService.getAllAgents(0, 1000);
		//			while(listAllAgents.size() > 0 ){
		//				for (Agent name : listAllAgents ){
		//					//FIXME
		//					//nameService.remove(name);
		//				}
		//				listAllAgents =  agentService.getAllAgents(0, 1000);
		//			}			
		//		}
		//		try {
			//get data from database
			String strQuery = 
					" SELECT *  " +
                    " FROM AuthorTeam " ;
			ResultSet rs = source.getResultSet(strQuery) ;
			
			
			
			logger.info("end makeAuthors ...");
			return true;
		//		} catch (SQLException e) {
		//			logger.error("SQLException:" +  e);
		//			return false;
		//		}

	}
}
