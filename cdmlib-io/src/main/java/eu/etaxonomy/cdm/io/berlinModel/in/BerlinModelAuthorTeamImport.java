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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelAuthorTeamImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
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

	public static final String NAMESPACE = "AuthorTeam";
	
	private static int modCount = 1000;
	private static final String pluralString = "AuthorTeams";
	private static final String dbTableName = "AuthorTeam";
	 
	//TODO pass it in other way, not as a class variable
	private ResultSet rsSequence;
	private Source source;

	public BerlinModelAuthorTeamImport(){
		super();
	}

	
	protected void doInvoke(BerlinModelImportState state){
		BerlinModelImportConfigurator config = state.getConfig();
		source = config.getSource();

		logger.info("start make " + pluralString + " ...");
				
		//queryStrings
		String strIdQuery = getIdQuery(state);
		
		String strRecordQuery = getRecordQuery(config);
		String strWhere = " WHERE (1=1) ";
		if (state.getConfig().getAuthorTeamFilter() != null){
			strWhere += " AND " + state.getConfig().getAuthorTeamFilter();
			strWhere = strWhere.replaceFirst("authorTeamId", "authorTeamFk");
		}
		String strQuerySequence = 
			" SELECT *  " +
            " FROM AuthorTeamSequence " +
				strWhere + 	
            " ORDER By authorTeamFk, Sequence ";
		
		int recordsPerTransaction = config.getRecordsPerTransaction();
		try{
			ResultSetPartitioner partitioner = ResultSetPartitioner.NewInstance(source, strIdQuery, strRecordQuery, recordsPerTransaction);
			rsSequence = source.getResultSet(strQuerySequence) ;
			while (partitioner.nextPartition()){
				partitioner.doPartition(this, state);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			state.setUnsuccessfull();
			return;
		}
		
		
		logger.info("end make " + pluralString + " ... " + getSuccessString(true));
		return;
	}
	
	@Override
	protected String getIdQuery(BerlinModelImportState state){
		String strWhere = " WHERE (1=1) ";
		if (state.getConfig().getAuthorTeamFilter() != null){
			strWhere += " AND " + state.getConfig().getAuthorTeamFilter();
		}
		String idQuery = 
				" SELECT authorTeamId " +
                " FROM AuthorTeam " + 
                strWhere +
                " ORDER BY authorTeamId ";
		return idQuery;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String strRecordQuery = 
			" SELECT *  " +
            " FROM AuthorTeam " + 
            " WHERE authorTeamId IN ( " + ID_LIST_TOKEN + " )" + 
            " ORDER By authorTeamId ";
		return strRecordQuery;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true ;
		BerlinModelImportConfigurator config = state.getConfig();
		Set<Team> teamsToSave = new HashSet<Team>();
		Map<String, Person> personMap = (Map<String, Person>) partitioner.getObjectMap(BerlinModelAuthorImport.NAMESPACE);
		
		ResultSet rs = partitioner.getResultSet();
		//for each reference
		try{
			while (rs.next()){
				try{
					//if ((i++ % modCount ) == 0 && i!= 1 ){ logger.info(""+pluralString+" handled: " + (i-1));}
					
					//create Agent element
					int teamId = rs.getInt("AuthorTeamId");
					if (teamId == 0 && config.isIgnore0AuthorTeam()){
						continue;
					}
					
					Team team = Team.NewInstance();
					
					Boolean preliminaryFlag = rs.getBoolean("PreliminaryFlag");
					String authorTeamCache = rs.getString("AuthorTeamCache");
					String fullAuthorTeamCache = rs.getString("FullAuthorTeamCache");
					if (CdmUtils.isEmpty(fullAuthorTeamCache)){
						fullAuthorTeamCache = authorTeamCache;
					}
					team.setTitleCache(fullAuthorTeamCache, preliminaryFlag);
					team.setNomenclaturalTitle(authorTeamCache, preliminaryFlag);
	
					success &= makeSequence(team, teamId, rsSequence, personMap);
					if (team.getTeamMembers().size()== 0 && preliminaryFlag == false){
						team.setProtectedTitleCache(true);
						team.setProtectedNomenclaturalTitleCache(true);
					}
					
					//created, notes
					doIdCreatedUpdatedNotes(state, team, rs, teamId, NAMESPACE);
	
					teamsToSave.add(team);
				}catch(Exception ex){
					logger.error(ex.getMessage());
					ex.printStackTrace();
					success = false;
				}
			} //while rs.hasNext()
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
			
		//logger.info(i + " " + pluralString + " handled");
		getAgentService().save((Collection)teamsToSave);

		return success;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs)  {
		String nameSpace;
		Class cdmClass;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		//person map
		Set<String> idInSourceList = makeAuthorIdList(rs);
		nameSpace = BerlinModelAuthorImport.NAMESPACE;
		cdmClass = Person.class;
		Map<String, Person> personMap = (Map<String, Person>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idInSourceList, nameSpace);
		result.put(nameSpace, personMap);

		return result;
	}
		
	/**
	 * @param rs 
	 * @return
	 * @throws SQLException 
	 * @throws SQLException 
	 */
	private Set<String> makeAuthorIdList(ResultSet rs) {
		Set<String> result = new HashSet<String>();
		
		String authorTeamIdList = "";
		try {
			while (rs.next()){
				int id = rs.getInt("AuthorTeamId");
				authorTeamIdList = CdmUtils.concat(",", authorTeamIdList, String.valueOf(id));
			}
		
			String strQuerySequence = 
				" SELECT DISTINCT authorFk " +
	            " FROM AuthorTeamSequence " + 
	            " WHERE authorTeamFk IN (@) ";
			strQuerySequence = strQuerySequence.replace("@", authorTeamIdList);
			
			rs = source.getResultSet(strQuerySequence) ;
			while (rs.next()){
				int authorFk = rs.getInt("authorFk");
				result.add(String.valueOf(authorFk));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	private boolean makeSequence(Team team, int teamId, ResultSet rsSequence, Map<String, Person> personMap){
		try {
			if (rsSequence.isBeforeFirst()){
				rsSequence.next();
			}
			if (rsSequence.isAfterLast()){
				return true;
			}
			int sequenceTeamFk;
			try {
				sequenceTeamFk = rsSequence.getInt("AuthorTeamFk");
			} catch (SQLException e) {
				if (rsSequence.next() == false){
					return true;
				}else{
					throw e;
				}
			}
			while (sequenceTeamFk < teamId){
				logger.warn("Sequence team FK is smaller then team ID. Some teams for a sequence may not be available");
				rsSequence.next();
				sequenceTeamFk = rsSequence.getInt("AuthorTeamFk");
			}
			while (sequenceTeamFk == teamId){
				int authorFk = rsSequence.getInt("AuthorFk");
				Person author = personMap.get(String.valueOf(authorFk));
				if (author != null){
				team.addTeamMember(author);
				}else{
					logger.error("Author " + authorFk + " was not found for team " + teamId);
				}
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
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelAuthorTeamImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getTableName()
	 */
	@Override
	protected String getTableName() {
		return dbTableName;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getPluralString()
	 */
	@Override
	public String getPluralString() {
		return pluralString;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoAuthors();
	}



}
