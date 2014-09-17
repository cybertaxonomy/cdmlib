/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.globis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
 * @author a.mueller
 * @created 25.02.2014
 */
@Component
public class GlobisAuthorImport  extends GlobisImportBase<TeamOrPersonBase<?>> implements IMappingImport<TeamOrPersonBase<?>, GlobisImportState>{
	private static final Logger logger = Logger.getLogger(GlobisAuthorImport.class);
	
	private static final String pluralString = "references";
	private static final String dbTableName = "Literatur";
	private static final Class<?> cdmTargetClass = Reference.class;

	public GlobisAuthorImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}

	
	

	@Override
	protected void doInvoke(GlobisImportState state) {
		
		//create reference authors
		GlobisReferenceImport globisReferenceImport = (GlobisReferenceImport)getBean("globisReferenceImport");
		if (globisReferenceImport == null){
			logger.error("globisReferenceImport could not be found");
		}else{
			String query = globisReferenceImport.getIdQuery();
			query = query.replace("SELECT refID", "SELECT refID, RefAuthor");
			ResultSet rs = state.getConfig().getSource().getResultSet(query);
			try {
				TransactionStatus tx = startTransaction();
				while (rs.next()){
					String refAuthor = rs.getString("RefAuthor");
					TeamOrPersonBase<?> agent = makeAuthor(refAuthor, state, getAgentService());
//					getAgentService().saveOrUpdate(agent);
				}
				commitTransaction(tx);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}



	@Override
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition( ResultSet rs, GlobisImportState state) {
		//not required here
		return null;
	}

	@Override
	public TeamOrPersonBase<?> createObject(ResultSet rs, GlobisImportState state) throws SQLException {
		//not required here
		return null;
	}

	@Override
	protected String getRecordQuery(GlobisImportConfigurator config) {
		//not required here
		return null;
	}
	
	
	
	protected static TeamOrPersonBase<?> makeAuthor(String refAuthor, GlobisImportState state, IAgentService service) {
		
		//create string list for single authors
		List<String> singleAuthorStrings = makeAuthorStringList(refAuthor);

		TeamOrPersonBase<?> result; 
		if (singleAuthorStrings.size() > 1){
			Team newTeam = Team.NewInstance();
			for (String str : singleAuthorStrings){
				Person person = makePerson(str, state, service);
				newTeam.addTeamMember(person);
			}
			String teamKey = makeTeamKey(newTeam, state, service);
			Team team = state.getTeam(teamKey);
			if (team == null){
				team = newTeam;
				service.save(team);
				state.putTeam(teamKey, newTeam);
			}
			result = team;
		}else{
			result = makePerson(singleAuthorStrings.get(0), state, service);
		}
		
		return result;
	}
	
	private static List<String> makeAuthorStringList(String refAuthor) {
		List<String> singleAuthorStrings = new ArrayList<String>();
		String[] split = refAuthor.split(";");
		for (String single : split){
			single = single.trim();
			if (single.startsWith("&")){
				single = single.substring(1).trim();
			}
			String[] split2 = single.split("&");
			for (String single2 : split2){
				singleAuthorStrings.add(single2.trim());
			}
		}
		return singleAuthorStrings;
	}




	protected static String makeTeamKey(Team team, GlobisImportState state, IAgentService service){
		String uuidList = "";
		for (Person person : team.getTeamMembers()){
			String str = person.getTitleCache();
			Person existingPerson = makePerson(str, state, service);
			uuidList += "@" + existingPerson.getUuid();
		}
		return uuidList;
	}

	private static Person makePerson(String string, GlobisImportState state, IAgentService service) {
		if (string == null){
			return null;
		}
		string = string.trim();
		Person person = state.getPerson(string);
		if (person == null){
			person = Person.NewTitledInstance(string);
			service.save(person);
			state.putPerson(string, person);
		}
		return person;
	}

	
	@Override
	protected boolean doCheck(GlobisImportState state){
		logger.info("Do check not implemented for Author import");
		return true;
	}
	
	@Override
	protected boolean isIgnore(GlobisImportState state){
		return !state.getConfig().isDoAuthors();
	}


}
