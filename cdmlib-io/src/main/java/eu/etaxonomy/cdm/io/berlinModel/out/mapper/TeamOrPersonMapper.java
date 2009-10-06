/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel.out.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportMapping;
import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportState;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 *
 */
public class TeamOrPersonMapper extends DbObjectMapper {

	BerlinModelExportMapping teamMapping;
	PreparedStatement stmtInsertTeam;
	PreparedStatement stmtInsertSequence;
	PreparedStatement stmtMaxId;
	
	public static TeamOrPersonMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		TeamOrPersonMapper result = new TeamOrPersonMapper(cdmAttributeString, dbAttributeString, null);
		return result;
	}
	
	
	protected TeamOrPersonMapper(String cdmAttributeString, String dbAttributeString, Object defaultValue) {
		super(cdmAttributeString, dbAttributeString, defaultValue);
		
	}
	
	Integer lastTeamId;
	
	protected boolean doInvoke(CdmBase cdmBase) throws SQLException {
		CdmBase agent = (CdmBase)ImportHelper.getValue(cdmBase, this.getSourceAttribute(), false, obligatory);  
		if (agent == null || agent.isInstanceOf(Team.class)){
			//if Team, do normal
			if (agent == null){
				lastTeamId = null;
			}else{
				lastTeamId = agent.getId();
			}
			return super.doInvoke(cdmBase);
		}else if (agent.isInstanceOf(Person.class)){
			lastTeamId = makePersonToTeam(CdmBase.deproxy(agent, Person.class));
			return super.doInvoke(cdmBase);
		}else{
			throw new IllegalStateException("Invoke argument for TeamOrPersonMapper must be either a Team or a Person but was " + agent.getClass().getName());
		}
	}
	

	private int makePersonToTeam(Person agent) {		
		int teamFk = 0;
		try {
			ResultSet result = stmtMaxId.executeQuery();
			
			while (result.next()){
				teamFk = result.getInt(1) +1;
				System.out.println(teamFk);
				stmtInsertTeam.setInt(1, teamFk);
				stmtInsertTeam.setString(2, agent.getTitleCache());
				stmtInsertTeam.setString(3, agent.getNomenclaturalTitle());
				stmtInsertTeam.executeUpdate();
				
				stmtInsertSequence.setInt(1, teamFk);
				stmtInsertSequence.setInt(2, agent.getId());
				stmtInsertSequence.executeUpdate();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalStateException("SQL Exception occurred in makePersonToTeam()");
		}
		
		return teamFk;
	}


	@Override
	protected Object getValue(CdmBase cdmBase) {
		return lastTeamId;
	}


	@Override
	public void initialize(PreparedStatement stmt, IndexCounter index,
			DbExportStateBase state, String tableName) {
		super.initialize(stmt, index, state, tableName);
		Source db = ((BerlinModelExportState)this.getState()).getConfig().getDestination();
		try {
			String insertAuthorTeam = "INSERT INTO authorTeam (AuthorTeamId, FullAuthorTeamCache,AuthorTeamCache, PreliminaryFlag ) " +
							" Values (?, ?,?,0)";
			stmtInsertTeam = db.getConnection().prepareStatement(insertAuthorTeam);
			
			String insertSequence = "INSERT INTO authorTeamSequence (authorTeamFk, authorFk, Sequence) " +
			" Values (?,?,1)";
			stmtInsertSequence = db.getConnection().prepareStatement(insertSequence);
			
			String getMaxId = "SELECT max(AuthorTeamId) as max FROM AuthorTeam";
			stmtMaxId = db.getConnection().prepareStatement(getMaxId);
			
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new IllegalStateException("An SQLException occurred when trying to prepare insert team statements");
		}
	}


	

}
