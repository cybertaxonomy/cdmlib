/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IPartitionedIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public abstract class CentralAfricaFernsImportBase<CDM_BASE extends CdmBase> extends CdmImportBase<CentralAfricaFernsImportConfigurator, CentralAfricaFernsImportState> implements ICdmIO<CentralAfricaFernsImportState>, IPartitionedIO<CentralAfricaFernsImportState> {
	private static final Logger logger = Logger.getLogger(CentralAfricaFernsImportBase.class);
	
	public static final UUID ID_IN_SOURCE_EXT_UUID = UUID.fromString("23dac094-e793-40a4-bad9-649fc4fcfd44");
	
	protected static final String TAXON_NAMESPACE = "African_pteridophytes_Taxon";
	protected static final String NAME_NAMESPACE = "African_pteridophytes_Name";
	

	private String pluralString;
	private String dbTableName;
	//TODO needed?
	private Class cdmTargetClass;
	

	
	
	/**
	 * @param dbTableName
	 * @param dbTableName2 
	 */
	public CentralAfricaFernsImportBase(String pluralString, String dbTableName, Class cdmTargetClass) {
		this.pluralString = pluralString;
		this.dbTableName = dbTableName;
		this.cdmTargetClass = cdmTargetClass;
	}

	protected boolean doInvoke(CentralAfricaFernsImportState state){
		logger.info("start make " + getPluralString() + " ...");
		boolean success = true ;
		CentralAfricaFernsImportConfigurator config = state.getConfig();
		Source source = config.getSource();
			
		String strIdQuery = getIdQuery();
		String strRecordQuery = getRecordQuery(config);

		int recordsPerTransaction = config.getRecordsPerTransaction();
		try{
			ResultSetPartitioner partitioner = ResultSetPartitioner.NewInstance(source, strIdQuery, strRecordQuery, recordsPerTransaction);
			while (partitioner.nextPartition()){
				partitioner.doPartition(this, state);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
		
		logger.info("end make " + getPluralString() + " ... " + getSuccessString(success));
		return success;
	}
	
	public boolean doPartition(ResultSetPartitioner partitioner, CentralAfricaFernsImportState state) {
		boolean success = true ;
		Set objectsToSave = new HashSet();
		
 		DbImportMapping<?, ?> mapping = getMapping();
		mapping.initialize(state, cdmTargetClass);
		
		ResultSet rs = partitioner.getResultSet();
		try{
			while (rs.next()){
				success &= mapping.invoke(rs,objectsToSave);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	
		partitioner.startDoSave();
		getCommonService().save(objectsToSave);
		return success;
	}


	
	/**
	 * @return
	 */
	protected abstract DbImportMapping<?, ?> getMapping();
	
	/**
	 * @return
	 */
	protected abstract String getRecordQuery(CentralAfricaFernsImportConfigurator config);

	/**
	 * @return
	 */
	protected String getIdQuery(){
		String result = " SELECT id FROM " + getTableName();
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getPluralString()
	 */
	public String getPluralString(){
		return pluralString;
	}

	/**
	 * @return
	 */
	protected String getTableName(){
		return this.dbTableName;
	}
	
	protected boolean doIdCreatedUpdatedNotes(CentralAfricaFernsImportState state, IdentifiableEntity identifiableEntity, ResultSet rs, long id, String namespace)
			throws SQLException{
		boolean success = true;
		//id
		success &= ImportHelper.setOriginalSource(identifiableEntity, state.getConfig().getSourceReference(), id, namespace);
		//createdUpdateNotes
		success &= doCreatedUpdatedNotes(state, identifiableEntity, rs, namespace);
		return success;
	}
	
	
	protected boolean doCreatedUpdatedNotes(CentralAfricaFernsImportState state, AnnotatableEntity annotatableEntity, ResultSet rs, String namespace)
			throws SQLException{

		CentralAfricaFernsImportConfigurator config = state.getConfig();
		Object createdWhen = rs.getObject("Created_When");
		String createdWho = rs.getString("Created_Who");
		Object updatedWhen = null;
		String updatedWho = null;
		try {
			updatedWhen = rs.getObject("Updated_When");
			updatedWho = rs.getString("Updated_who");
		} catch (SQLException e) {
			//Table "Name" has no updated when/who
		}
		String notes = rs.getString("notes");
		
		boolean success  = true;
		
		//Created When, Who, Updated When Who
		if (config.getEditor() == null || config.getEditor().equals(EDITOR.NO_EDITORS)){
			//do nothing
		}else if (config.getEditor().equals(EDITOR.EDITOR_AS_ANNOTATION)){
			String createdAnnotationString = "Berlin Model record was created By: " + String.valueOf(createdWho) + " (" + String.valueOf(createdWhen) + ") ";
			if (updatedWhen != null && updatedWho != null){
				createdAnnotationString += " and updated By: " + String.valueOf(updatedWho) + " (" + String.valueOf(updatedWhen) + ")";
			}
			Annotation annotation = Annotation.NewInstance(createdAnnotationString, Language.DEFAULT());
			annotation.setCommentator(config.getCommentator());
			annotation.setAnnotationType(AnnotationType.TECHNICAL());
			annotatableEntity.addAnnotation(annotation);
		}else if (config.getEditor().equals(EDITOR.EDITOR_AS_EDITOR)){
			User creator = getUser(createdWho, state);
			User updator = getUser(updatedWho, state);
			DateTime created = getDateTime(createdWhen);
			DateTime updated = getDateTime(updatedWhen);
			annotatableEntity.setCreatedBy(creator);
			annotatableEntity.setUpdatedBy(updator);
			annotatableEntity.setCreated(created);
			annotatableEntity.setUpdated(updated);
		}else {
			logger.warn("Editor type not yet implemented: " + config.getEditor());
		}
		
		
		//notes
		if (CdmUtils.isNotEmpty(notes)){
			String notesString = String.valueOf(notes);
			if (notesString.length() > 65530 ){
				notesString = notesString.substring(0, 65530) + "...";
				logger.warn("Notes string is longer than 65530 and was truncated: " + annotatableEntity);
			}
			Annotation notesAnnotation = Annotation.NewInstance(notesString, null);
			//notesAnnotation.setAnnotationType(AnnotationType.EDITORIAL());
			//notes.setCommentator(bmiConfig.getCommentator());
			annotatableEntity.addAnnotation(notesAnnotation);
		}
		return success;
	}
	
	private User getUser(String userString, CentralAfricaFernsImportState state){
		if (CdmUtils.isEmpty(userString)){
			return null;
		}
		userString = userString.trim();
		
		User user = state.getUser(userString);
		if (user == null){
			user = getTransformedUser(userString,state);
		}
		if (user == null){
			user = makeNewUser(userString, state);
		}
		if (user == null){
			logger.warn("User is null");
		}
		return user;
	}
	
	private User getTransformedUser(String userString, CentralAfricaFernsImportState state){
		Method method = state.getConfig().getUserTransformationMethod();
		if (method == null){
			return null;
		}
		try {
			userString = (String)state.getConfig().getUserTransformationMethod().invoke(null, userString);
		} catch (Exception e) {
			logger.warn("Error when trying to transform userString " +  userString + ". No transformation done.");
		}
		User user = state.getUser(userString);
		return user;
	}

	private User makeNewUser(String userString, CentralAfricaFernsImportState state){
		String pwd = getPassword(); 
		User user = User.NewInstance(userString, pwd);
		state.putUser(userString, user);
		getUserService().save(user);
		logger.info("Added new user: " + userString);
		return user;
	}
	
	private String getPassword(){
		String result = UUID.randomUUID().toString();
		return result;
	}
	
	private DateTime getDateTime(Object timeString){
		if (timeString == null){
			return null;
		}
		DateTime dateTime = null;
		if (timeString instanceof Timestamp){
			Timestamp timestamp = (Timestamp)timeString;
			dateTime = new DateTime(timestamp);
		}else{
			logger.warn("time ("+timeString+") is not a timestamp. Datetime set to current date. ");
			dateTime = new DateTime();
		}
		return dateTime;
	}
	
	protected boolean resultSetHasColumn(ResultSet rs, String columnName){
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			for (int i = 0; i < metaData.getColumnCount(); i++){
				if (metaData.getColumnName(i + 1).equalsIgnoreCase(columnName)){
					return true;
				}
			}
			return false;
		} catch (SQLException e) {
            logger.warn("Exception in resultSetHasColumn");
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
	
	/**
	 * Returns a map that holds all values of a ResultSet. This is needed if a value needs to
	 * be accessed twice
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected Map<String, Object> getValueMap(ResultSet rs) throws SQLException{
		try{
			Map<String, Object> valueMap = new HashMap<String, Object>();
			int colCount = rs.getMetaData().getColumnCount();
			for (int c = 0; c < colCount ; c++){
				Object value = rs.getObject(c+1);
				String label = rs.getMetaData().getColumnLabel(c+1).toLowerCase();
				if (value != null && ! CdmUtils.Nz(value.toString()).trim().equals("")){
					valueMap.put(label, value);
				}
			}
			return valueMap;
		}catch(SQLException e){
			throw e;
		}
	}
	
	protected ExtensionType getExtensionType(UUID uuid, String label, String text, String labelAbbrev){
		ExtensionType extensionType = (ExtensionType)getTermService().find(uuid);
		if (extensionType == null){
			extensionType = ExtensionType.NewInstance(text, label, labelAbbrev);
			extensionType.setUuid(uuid);
			getTermService().save(extensionType);
		}
		return extensionType;
	}
	
	protected MarkerType getMarkerType(UUID uuid, String label, String text, String labelAbbrev){
		MarkerType markerType = (MarkerType)getTermService().find(uuid);
		if (markerType == null){
			markerType = MarkerType.NewInstance(label, text, labelAbbrev);
			markerType.setUuid(uuid);
			getTermService().save(markerType);
		}
		return markerType;
	}
	

	/**
	 * Reads a foreign key field from the result set and adds its value to the idSet.
	 * @param rs
	 * @param teamIdSet
	 * @throws SQLException
	 */
	protected void handleForeignKey(ResultSet rs, Set<String> idSet, String attributeName)
			throws SQLException {
		Object idObj = rs.getObject(attributeName);
		if (idObj != null){
			String id  = String.valueOf(idObj);
			idSet.add(id);
		}
	}
	
	/**
	 * Returns true if i is a multiple of recordsPerTransaction
	 * @param i
	 * @param recordsPerTransaction
	 * @return
	 */
	protected boolean loopNeedsHandling(int i, int recordsPerLoop) {
		startTransaction();
		return (i % recordsPerLoop) == 0;
	}
	
	protected void doLogPerLoop(int count, int recordsPerLog, String pluralString){
		if ((count % recordsPerLog ) == 0 && count!= 0 ){ logger.info(pluralString + " handled: " + (count));}
	}
	


	
}
