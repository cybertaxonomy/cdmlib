/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR;
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
public abstract class BerlinModelImportBase extends CdmImportBase<BerlinModelImportConfigurator, BerlinModelImportState> implements ICdmIO<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelImportBase.class);
	
	public static final UUID ID_IN_SOURCE_EXT_UUID = UUID.fromString("23dac094-e793-40a4-bad9-649fc4fcfd44");
	
	public BerlinModelImportBase() {
		super();
	}
	
	protected abstract boolean doInvoke(BerlinModelImportState state);

	
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
//	 */
//	@Override
//	protected boolean doInvoke(IImportConfigurator config, 
//			Map<String, MapWrapper<? extends CdmBase>> stores){ 
//		BerlinModelImportState state = ((BerlinModelImportConfigurator)config).getState();
//		state.setConfig((BerlinModelImportConfigurator)config);
//		return doInvoke(state);
//	}
	
	protected boolean doIdCreatedUpdatedNotes(BerlinModelImportState state, IdentifiableEntity identifiableEntity, ResultSet rs, long id, String namespace)
			throws SQLException{
		boolean success = true;
		//id
		success &= ImportHelper.setOriginalSource(identifiableEntity, state.getConfig().getSourceReference(), id, namespace);
		//createdUpdateNotes
		success &= doCreatedUpdatedNotes(state, identifiableEntity, rs, namespace);
		return success;
	}
	
	
	protected boolean doCreatedUpdatedNotes(BerlinModelImportState state, AnnotatableEntity annotatableEntity, ResultSet rs, String namespace)
			throws SQLException{

		BerlinModelImportConfigurator config = state.getConfig();
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
		Object notes = rs.getObject("notes");
		
		boolean success  = true;
		
		//Created When, Who, Updated When Who
		if (config.getEditor() == null || config.getEditor().equals(EDITOR.NO_EDITORS)){
			//do nothing
		}else if (config.getEditor().equals(EDITOR.EDITOR_AS_ANNOTATION)){
			String createdAnnotationString = "Berlin Model record was created By: " + String.valueOf(createdWho) + " (" + String.valueOf(createdWhen) + ") ";
			if (updatedWhen != null && updatedWho != null){
				createdAnnotationString += " and updated By: " + String.valueOf(updatedWho) + " (" + String.valueOf(updatedWhen) + ")";
			}
			Annotation annotation = Annotation.NewInstance(createdAnnotationString, Language.ENGLISH());
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
		if (notes != null){
			String notesString = String.valueOf(notes);
			if (notesString.length() > 3999 ){
				notesString = notesString.substring(0, 3996) + "...";
				logger.warn("Notes string is longer than 3999 and was truncated: " + annotatableEntity);
			}
			Annotation notesAnnotation = Annotation.NewInstance(notesString, null);
			//notesAnnotation.setAnnotationType(AnnotationType.EDITORIAL());
			//notes.setCommentator(bmiConfig.getCommentator());
			annotatableEntity.addAnnotation(notesAnnotation);
		}
		return success;
	}
	
	private User getUser(String userString, BerlinModelImportState state){
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
	
	private User getTransformedUser(String userString, BerlinModelImportState state){
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

	private User makeNewUser(String userString, BerlinModelImportState state){
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
		ExtensionType extensionType = (ExtensionType)getTermService().getTermByUuid(uuid);
		if (extensionType == null){
			extensionType = new ExtensionType(label, text, labelAbbrev);
			extensionType.setUuid(uuid);
			getTermService().save(extensionType);
		}
		return extensionType;
	}
	
	protected MarkerType getMarkerType(UUID uuid, String label, String text, String labelAbbrev){
		MarkerType markerType = (MarkerType)getTermService().getTermByUuid(uuid);
		if (markerType == null){
			markerType = MarkerType.NewInstance(label, text, labelAbbrev);
			markerType.setUuid(uuid);
			getTermService().save(markerType);
		}
		return markerType;
	}
	
}
