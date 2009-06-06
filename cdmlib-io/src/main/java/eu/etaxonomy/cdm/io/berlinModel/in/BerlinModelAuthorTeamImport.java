/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel.in;

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
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelAuthorTeamImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelAuthorTeamImport.class);

	private static int modCount = 1000;
	private static final String pluralString = "AuthorTeams";
	 

	public BerlinModelAuthorTeamImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for "+pluralString+" not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){ 

		MapWrapper<AgentBase> teamMap = (MapWrapper<AgentBase>)stores.get(ICdmIO.TEAM_STORE);
		MapWrapper<AgentBase> personMap = (MapWrapper<AgentBase>)stores.get(ICdmIO.PERSON_STORE);
		
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;

		logger.info("start make "+pluralString+" ...");
		boolean success = true ;
		
		
		
		//get data from database
		String strQueryTeam = 
				" SELECT *  " +
                " FROM AuthorTeam " + 
                " ORDER By authorTeamId ";
		ResultSet rsTeam = source.getResultSet(strQueryTeam) ;
		String namespace = "AuthorTeam";

		String strQuerySequence = 
			" SELECT *  " +
            " FROM AuthorTeamSequence " + 
            " ORDER By authorTeamFk, Sequence ";
		ResultSet rsSequence = source.getResultSet(strQuerySequence) ;
		
		int i = 0;
		//for each reference
		try{
			while (rsTeam.next()){
				
				if ((i++ % modCount ) == 0 && i!= 1 ){ logger.info(""+pluralString+" handled: " + (i-1));}
				
				//create Agent element
				int teamId = rsTeam.getInt("AuthorTeamId");
				if (teamId == 0 && bmiConfig.isIgnore0AuthorTeam()){
					continue;
				}
				
				Team team = Team.NewInstance();
				
				dbAttrName = "AuthorTeamCache";
				cdmAttrName = "nomenclaturalTitle";
				success &= ImportHelper.addStringValue(rsTeam, team, dbAttrName, cdmAttrName);

				dbAttrName = "AuthorTeamCache";
				cdmAttrName = "titleCache";
				success &= ImportHelper.addStringValue(rsTeam, team, dbAttrName, cdmAttrName);

				//TODO
				//FullAuthorTeamCache
				//preliminaryFlag
				//title cache or nomenclaturalTitle?

				makeSequence(team, teamId, rsSequence, stores);
				
				//created, notes
				doIdCreatedUpdatedNotes(config, team, rsTeam, teamId, namespace);

				teamMap.put(teamId, team);
			} //while rs.hasNext()
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

			
		logger.info(i + " "+pluralString+" handled");
		getAgentService().saveAgentAll(teamMap.objects());

		logger.info("end make "+pluralString+" ...");
		personMap.makeEmpty();
		return success;
	}
	
	private boolean makeSequence(Team team, int teamId, ResultSet rsSequence, Map<String, MapWrapper<? extends CdmBase>> stores){
		MapWrapper<Person> personMap = (MapWrapper<Person>)stores.get(ICdmIO.PERSON_STORE);
		try {
			if (rsSequence.isBeforeFirst()){
				rsSequence.next();
			}
			if (rsSequence.isAfterLast()){
				return true;
			}
			int sequenceTeamFk = rsSequence.getInt("AuthorTeamFk");
			while (sequenceTeamFk < teamId){
				rsSequence.next();
				sequenceTeamFk = rsSequence.getInt("AuthorTeamFk");
			}
			while (sequenceTeamFk == teamId){
				int authorFk = rsSequence.getInt("AuthorFk");
				Person author = personMap.get(authorFk);
				team.addTeamMember(author);
				if (rsSequence.next()){
					sequenceTeamFk = rsSequence.getInt("AuthorTeamFk");
				}else{
					break;
				}
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoAuthors();
	}

}
