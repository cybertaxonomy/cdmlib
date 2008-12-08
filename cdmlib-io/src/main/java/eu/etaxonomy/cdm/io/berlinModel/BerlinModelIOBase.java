package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;


public abstract class BerlinModelIOBase extends CdmIoBase<IImportConfigurator> implements ICdmIO<IImportConfigurator> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BerlinModelIOBase.class);
	
	public BerlinModelIOBase() {
		super();
	}
	
	
	protected boolean doIdCreatedUpdatedNotes(IImportConfigurator bmiConfig, IdentifiableEntity identifiableEntity, ResultSet rs, long id, String namespace)
			throws SQLException{
		
		Object createdWhen = rs.getObject("Created_When");
		Object createdWho = rs.getObject("Created_Who");
		Object updatedWhen = null;
		Object updatedWho = null;
		try {
			updatedWhen = rs.getObject("Updated_When");
			updatedWho = rs.getObject("Updated_who");
		} catch (SQLException e) {
			//Table "Name" has no updated when/who
		}
		Object notes = rs.getObject("notes");

		boolean success  = true;
		
		//id
		ImportHelper.setOriginalSource(identifiableEntity, bmiConfig.getSourceReference(), id, namespace);

		
		//Created When, Who, Updated When Who
		String createdAnnotationString = "Berlin Model record was created By: " + String.valueOf(createdWho) + " (" + String.valueOf(createdWhen) + ") ";
		if (updatedWhen != null && updatedWho != null){
			createdAnnotationString += " and updated By: " + String.valueOf(updatedWho) + " (" + String.valueOf(updatedWhen) + ")";
		}
		Annotation annotation = Annotation.NewInstance(createdAnnotationString, Language.ENGLISH());
		annotation.setCommentator(bmiConfig.getCommentator());
		identifiableEntity.addAnnotation(annotation);
		
		//notes
		if (notes != null){
			String notesString = String.valueOf(notes);
			if (notesString.length() > 254 ){
				notesString = notesString.substring(0, 250) + "...";
			}
			Annotation notesAnnotation = Annotation.NewInstance(notesString, null);
			//notes.setCommentator(bmiConfig.getCommentator());
			identifiableEntity.addAnnotation(notesAnnotation);
		}
		return success;
	}
	
	
	protected boolean resultSetHasColumn(ResultSet rs, String columnName){
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			for (int i = 0; i < metaData.getColumnCount(); i++){
				if (metaData.getColumnName(i).equalsIgnoreCase(columnName)){
					return true;
				}
			}
			return false;
		} catch (SQLException e) {
			return false;
		}
	}
	
	protected boolean checkSqlServerColumnExists(Source source, String tableName, String columnName){
		String strQuery = "SELECT  Count(t.id) as n " +
				" FROM sysobjects AS t " +
				" INNER JOIN syscolumns AS c ON t.id = c.id " +
				" WHERE (t.xtype = 'U') AND " + 
				" (t.name = '" + tableName + "') AND " + 
				" (c.name = '" + columnName + "')";
		ResultSet rs = source.getResultSet(strQuery) ;		
		int n;
		try {
			rs.next();
			n = rs.getInt("n");
			return n>0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
}
