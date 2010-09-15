// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class SingleTermUpdater extends SchemaUpdaterStepBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SingleTermUpdater.class);
	
	public static final SingleTermUpdater NewInstance(String stepName, UUID uuidTerm, String description,  String label, String abbrev, String dtype, Integer orderIndex, UUID uuidVocabulary){
		return new SingleTermUpdater(stepName, uuidTerm, description, label, abbrev, dtype, orderIndex, uuidVocabulary);
		
	}
	
//	private ICdmDataSource datasource;
//	private IProgressMonitor monitor;
	private UUID uuidTerm ;
	private String description;
	private String label;
	private String abbrev;
	private String dtype;
	private UUID uuidVocabulary;
	private Integer orderIndex;
	
	

	private SingleTermUpdater(String stepName, UUID uuidTerm, String description, String label, String abbrev, String dtype, Integer orderIndex, UUID uuidVocabulary) {
		super(stepName);
		this.abbrev = abbrev;
//		this.datasource = datasource;
//		this.monitor = monitor;
		this.description = description;
		this.dtype = dtype;
		this.label = label;
		this.orderIndex = orderIndex;
		this.uuidTerm = uuidTerm;
		this.uuidVocabulary = uuidVocabulary;
	}



	public boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException{
 		String sqlCheckTermExists = " SELECT count(*) as n FROM DefinedTermBase WHERE uuid = '" + uuidTerm + "'";
		Long n = (Long)datasource.getSingleValue(sqlCheckTermExists);
		if (n != 0){
			monitor.warning("Term already exists: " + label + "(" + uuidTerm + ")");
			return true;
		}
		
		//vocabulary id
		int vocId;
		String sqlVocId = " SELECT id FROM TermVocabulary WHERE uuid = '" + uuidVocabulary + "'";
		ResultSet rs = datasource.executeQuery(sqlVocId);
		if (rs.next()){
			vocId = rs.getInt("id");
		}else{
			String warning = "Vocabulary ( "+ uuidVocabulary +" ) for term does not exist!";
			monitor.warning(warning);
			return false;
		}
		
		int termId;
		String sqlMaxId = " SELECT max(id)+1 as maxId FROM DefinedTermBase";
		rs = datasource.executeQuery(sqlMaxId);
		if (rs.next()){
			termId = rs.getInt("maxId");
		}else{
			String warning = "No defined terms do exist yet. Can't update terms!";
			monitor.warning(warning);
			return false;
		}
		
		String id = Integer.toString(termId);
		String created = "2010-09-01 10:15:00";
		String defaultColor = "null";
		String protectedTitleCache = "b'0'";
		String titleCache = label != null ? label : (abbrev != null ? abbrev : description );
		String sqlInsertTerm = " INSERT INTO DefinedTermBase (DTYPE, id, uuid, created, protectedtitlecache, titleCache, orderindex, defaultcolor, vocabulary_id)" +
				"VALUES ('" + dtype + "', " + id + ", '" + uuidTerm + "', '" + created + "', " + protectedTitleCache + ", '" + titleCache + "', " + orderIndex + ", " + defaultColor + ", " + vocId + ")"; 
		datasource.executeUpdate(sqlInsertTerm);
		
//
//		INSERT INTO DefinedTermBase (DTYPE, id, uuid, created, protectedtitlecache, titleCache, orderindex, defaultcolor, vocabulary_id) 
//		SELECT 'ReferenceSystem' ,  (@defTermId := max(id)+1)  as maxId , '1bb67042-2814-4b09-9e76-c8c1e68aa281', '2010-06-01 10:15:00', b'0', 'Google Earth', null, null, @refSysVocId
//		FROM DefinedTermBase ;
//

		//language id
		int langId;
		String sqlLangId = " SELECT id FROM DefinedTermBase WHERE uuid = 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'";
		rs = datasource.executeQuery(sqlLangId);
		if (rs.next()){
			langId = rs.getInt("id");
		}else{
			String warning = "Term for default language (English) not  does not exist!";
			monitor.warning(warning);
			return false;
		}
		
		//representation
		int repId;
		sqlMaxId = " SELECT max(id)+1 as maxId FROM Representation";
		rs = datasource.executeQuery(sqlMaxId);
		if (rs.next()){
			repId = rs.getInt("maxId");
		}else{
			String warning = "No representations do exist yet. Can't update terms!";
			monitor.warning(warning);
			return false;
		}
		
		UUID uuidRepresentation = UUID.randomUUID();
		String sqlInsertRepresentation = " INSERT INTO Representation (id, created, uuid, text, abbreviatedlabel, label, language_id) " +
				"VALUES (" + repId + ", '" + created + "', '" + uuidRepresentation + "', '" + description +  "', '" + label +  "',  '" + abbrev +  "', " + langId + ")"; 
		
		datasource.executeUpdate(sqlInsertRepresentation);
		
		
//		-- representation
//		INSERT INTO Representation (id, created, uuid, text, abbreviatedlabel, label, language_id) 
//		SELECT  ( @repId := max(id)+1 ) AS maxId ,'2010-06-01 18:49:07','fadb1730-9936-44e7-8911-884a84662b08', 'Google Earth','Google','Google Earth', @langId
//		FROM Representation;
//		;
		
		//
//		-- representation
//		INSERT INTO Representation (id, created, uuid, text, abbreviatedlabel, label, language_id) 
//		SELECT  ( @repId := max(id)+1 ) AS maxId ,'2010-06-01 18:49:07','fadb1730-9936-44e7-8911-884a84662b08', 'Google Earth','Google','Google Earth', @langId
//		FROM Representation;
//		;

		String sqlInsertMN = "INSERT INTO DefinedTermBase_Representation (DefinedTermBase_id, representations_id) " + 
				" VALUES ("+ termId +"," +repId+ " )";		
		
		datasource.executeUpdate(sqlInsertMN);
		
//		 -- defTerm <-> representation
//		INSERT INTO DefinedTermBase_Representation (DefinedTermBase_id, representations_id) 
//		VALUES (@defTermId,@repId);
//	
		return true;
	}
	
}
