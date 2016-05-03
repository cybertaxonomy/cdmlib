/**
 * 
 */
package eu.etaxonomy.cdm.database.update.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * 
 * Class for testing foreign key constraints. Will move to another package in future.
 * UNDER CONSTRUCTION
 
 * @author a.mueller
 *
 */
public class ValidateForeignKeys {

	private DataSource source;
	
	
	
	public class FkTestResult {
		public String table;
		public String field;
		public String foreignTable;
		public Integer id;
		
		public FkTestResult(String table, String field,
				String foreignTable, Integer id) {
			super();
			this.table = table;
			this.field = field;
			this.foreignTable = foreignTable;
			this.id = id;
		}
	}
	
	public ValidateForeignKeys(DataSource source) {
		this.source = source;
	}


	private void resultSet(String thisTable, String thisField, String foreignTable){
		List<FkTestResult> failingRecords = new ArrayList<FkTestResult>();
		try {
			String sql = sql(thisTable, thisField, foreignTable);
			Statement a = source.getConnection().createStatement();
		
			ResultSet rs = a.executeQuery(sql);
			while (rs.next()){
				Integer id = rs.getInt("id");
				FkTestResult res = new FkTestResult(thisTable, thisField, foreignTable, id);
				failingRecords.add(res);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private String sql(String thisTable, String thisField, String foreignTable){
		String sql = "SELECT * FROM %s WHERE %s.%s_id NOT IN (SELECT id FROM %s)";
		sql = String.format(sql, thisTable, thisTable, thisField, foreignTable);
		return sql;
	}
	
	
	public List<FkTestResult> invoke(){
		List<FkTestResult> failingRecords = new ArrayList<FkTestResult>();
		resultSet("SynonymRelationship","relatedFrom","TaxonBase");
		resultSet("SynonymRelationship","relatedTo","TaxonBase");
		
		return failingRecords;
	}
	


}
