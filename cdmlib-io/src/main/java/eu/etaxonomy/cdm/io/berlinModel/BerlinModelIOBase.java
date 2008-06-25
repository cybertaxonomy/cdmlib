package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;


public class BerlinModelIOBase {
	private static final Logger logger = Logger.getLogger(BerlinModelIOBase.class);

	
	protected static boolean doIdCreatedUpdatedNotes(BerlinModelImportConfigurator bmiConfig, IdentifiableEntity identifiableEntity, ResultSet rs, long id)
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
		ImportHelper.setOriginalSource(identifiableEntity, bmiConfig.getSourceReference(), id);

		
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
	
}
