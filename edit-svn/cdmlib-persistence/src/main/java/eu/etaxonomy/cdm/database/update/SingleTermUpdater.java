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

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * Creates a new term if a term with the same given uuid does not exist yet
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class SingleTermUpdater extends SchemaUpdaterStepBase<SingleTermUpdater> implements ITermUpdaterStep{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SingleTermUpdater.class);
	
	public static final SingleTermUpdater NewInstance(String stepName, UUID uuidTerm, String description,  String label, String abbrev, String dtype, UUID uuidVocabulary, UUID uuidLanguage, boolean isOrdered, UUID uuidAfterTerm){
		return new SingleTermUpdater(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLanguage, isOrdered, uuidAfterTerm);	
	}
	
	private UUID uuidTerm ;
	private String description;
	private String label;
	private String abbrev;
	private String dtype;
	private UUID uuidVocabulary;
	private boolean isOrdered;
	private UUID uuidAfterTerm;
	private UUID uuidLanguage;
	private String reverseDescription;
	private String reverseLabel;
	private String reverseAbbrev;
	
	

	private SingleTermUpdater(String stepName, UUID uuidTerm, String description, String label, String abbrev, String dtype, UUID uuidVocabulary, UUID uuidLanguage, boolean isOrdered, UUID uuidAfterTerm) {
		super(stepName);
		this.abbrev = abbrev;
		this.description = description;
		this.dtype = dtype;
		this.label = label;
		this.isOrdered = isOrdered;
		this.uuidTerm = uuidTerm;
		this.uuidVocabulary = uuidVocabulary;
		this.uuidAfterTerm = uuidAfterTerm;
		this.uuidLanguage = uuidLanguage;
	}

	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.monitor.IProgressMonitor)
	 */
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException{
 		String sqlCheckTermExists = " SELECT count(*) as n FROM DefinedTermBase WHERE uuid = '" + uuidTerm + "'";
		Long n = (Long)datasource.getSingleValue(sqlCheckTermExists);
		if (n != 0){
			monitor.warning("Term already exists: " + label + "(" + uuidTerm + ")");
			return null;
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
			return null;
		}
		
		Integer termId;
		String sqlMaxId = " SELECT max(id)+1 as maxId FROM DefinedTermBase";
		rs = datasource.executeQuery(sqlMaxId);
		if (rs.next()){
			termId = rs.getInt("maxId");
		}else{
			String warning = "No defined terms do exist yet. Can't update terms!";
			monitor.warning(warning);
			return null;
		}
		
		String id = Integer.toString(termId);
		String created = "2010-09-16 10:15:00";
		String defaultColor = "null";
		String protectedTitleCache = getBoolean(false, datasource);
		String orderIndex;
		if (isOrdered){
			orderIndex = getOrderIndex(datasource, vocId, monitor);
		}else{
			orderIndex = "null";
		}
		String titleCache = label != null ? label : (abbrev != null ? abbrev : description );
		String sqlInsertTerm = " INSERT INTO DefinedTermBase (DTYPE, id, uuid, created, protectedtitlecache, titleCache, orderindex, defaultcolor, vocabulary_id)" +
				"VALUES ('" + dtype + "', " + id + ", '" + uuidTerm + "', '" + created + "', " + protectedTitleCache + ", '" + titleCache + "', " + orderIndex + ", " + defaultColor + ", " + vocId + ")"; 
		datasource.executeUpdate(sqlInsertTerm);
		
		updateFeatureTerms(termId, datasource, monitor);
		
//
//		INSERT INTO DefinedTermBase (DTYPE, id, uuid, created, protectedtitlecache, titleCache, orderindex, defaultcolor, vocabulary_id) 
//		SELECT 'ReferenceSystem' ,  (@defTermId := max(id)+1)  as maxId , '1bb67042-2814-4b09-9e76-c8c1e68aa281', '2010-06-01 10:15:00', b'0', 'Google Earth', null, null, @refSysVocId
//		FROM DefinedTermBase ;
//

		//language id
		Integer langId = getLanguageId(uuidLanguage, datasource, monitor);
		if (langId == null){
			return null;
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
			return null;
		}
		
		//standard representation
		UUID uuidRepresentation = UUID.randomUUID();
		String sqlInsertRepresentation = " INSERT INTO Representation (id, created, uuid, text, label, abbreviatedlabel, language_id) " +
				"VALUES (" + repId + ", '" + created + "', '" + uuidRepresentation + "', '" + description +  "', '" + label +  "',  '" + abbrev +  "', " + langId + ")"; 
		
		datasource.executeUpdate(sqlInsertRepresentation);
		
		String sqlInsertMN = "INSERT INTO DefinedTermBase_Representation (DefinedTermBase_id, representations_id) " + 
				" VALUES ("+ termId +"," +repId+ " )";		
		
		datasource.executeUpdate(sqlInsertMN);
		
		//reverse representation
		if (hasReverseRepresentation()){
			int reverseRepId = repId + 1;
			UUID uuidReverseRepresentation = UUID.randomUUID();
			String sqlInsertReverseRepresentation = " INSERT INTO Representation (id, created, uuid, text, label, abbreviatedlabel, language_id) " +
					"VALUES (" + reverseRepId + ", '" + created + "', '" + uuidReverseRepresentation + "', '" + reverseDescription +  "', '" + reverseLabel +  "',  '" + reverseAbbrev +  "', " + langId + ")"; 
			
			datasource.executeUpdate(sqlInsertReverseRepresentation);
			
			String sqlReverseInsertMN = "INSERT INTO RelationshipTermBase_inverseRepresentation (DefinedTermBase_id, inverserepresentations_id) " + 
					" VALUES ("+ termId +"," +reverseRepId+ " )";		
			
			datasource.executeUpdate(sqlReverseInsertMN);
		}
				
		
		return termId;
	}


	private void updateFeatureTerms(Integer termId, ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		if (dtype.equals(Feature.class.getSimpleName())){
			String sqlUpdate = "UPDATE DefinedTermBase SET " + 
				" supportscategoricaldata = " + getBoolean(false, datasource) + ", " + 
				" supportscommontaxonname = " + getBoolean(false, datasource) + ", " + 
				" supportsdistribution = " + getBoolean(false, datasource) + ", " +
				" supportsindividualassociation = " + getBoolean(false, datasource) + ", " +
				" supportsquantitativedata = " + getBoolean(false, datasource) + ", " +
				" supportstaxoninteraction = " + getBoolean(false, datasource) + ", " +
				" supportstextdata = " + getBoolean(true, datasource) +  " " +
				" WHERE id = " + termId;
			datasource.executeUpdate(sqlUpdate);
		}
	}




	/**
	 * @param datasource
	 * @param vocId
	 * @param monitor
	 * @return
	 * @throws SQLException
	 */
	private String getOrderIndex(ICdmDataSource datasource, int vocId, IProgressMonitor monitor) throws SQLException {
		ResultSet rs;
		Integer intOrderIndex = null;
		if (uuidAfterTerm == null){
			return "1";
		}
		String sqlOrderIndex = " SELECT orderindex FROM DefinedTermBase WHERE uuid = '"+uuidAfterTerm+"' AND vocabulary_id = "+vocId+"";
		rs = datasource.executeQuery(sqlOrderIndex);
		if (rs.next()){
			intOrderIndex = rs.getInt("orderindex") + 1;
			
			String sqlUpdateLowerTerms = "UPDATE DefinedTermBase SET orderindex = orderindex + 1 WHERE vocabulary_id = " + vocId+ " AND orderindex >= " + intOrderIndex;
			datasource.executeUpdate(sqlUpdateLowerTerms);
		}else{
			String warning = "The previous term has not been found in vocabulary. Put term to the end";
			monitor.warning(warning);
			return "null";
		}
		if (intOrderIndex == null){
			String sqlMaxOrderIndex = " SELECT max(orderindex) FROM DefinedTermBase WHERE vocabulary_id = " + vocId + "";
			intOrderIndex = (Integer)datasource.getSingleValue(sqlMaxOrderIndex);
			if (intOrderIndex != null){
				intOrderIndex++;
			}else{
				String warning = "No term was found in vocabulary or vocabulary does not exist. Use order index '0'.";
				monitor.warning(warning);
				return "0";
			}
		}
		
		return intOrderIndex.toString();
//			-- absence term max orderindex
//			SELECT (@maxAbsenceOrderIndex := max(orderindex)) AS b FROM DefinedTermBase WHERE DTYPE = 'AbsenceTerm';
//
//			-- native reported in error
//			SELECT (@presenceOrderIndex := orderindex) AS a FROM DefinedTermBase WHERE uuid = '4ba212ef-041e-418d-9d43-2ebb191b61d8';
//			UPDATE DefinedTermBase SET uuid = '61cee840-801e-41d8-bead-015ad866c2f1', DTYPE = 'AbsenceTerm', vocabulary_id = 18, orderindex = @maxAbsenceOrderIndex + 1 WHERE uuid = '4ba212ef-041e-418d-9d43-2ebb191b61d8';
//			UPDATE DefinedTermBase SET orderindex = orderindex -1 WHERE DTYPE = 'PresenceTerm' AND orderindex > @presenceOrderIndex ;
	}

	
	private boolean hasReverseRepresentation() {
		return  reverseLabel != null ||  reverseDescription != null ||  reverseAbbrev != null;
	}

	public SingleTermUpdater setReverseRepresentation(String reverseDescription, String reverseLabel, String reverseAbbrev) {
		this.reverseLabel = reverseLabel;
		this.reverseDescription = reverseDescription;
		this.reverseAbbrev = reverseAbbrev;
		return this;
	}


}
