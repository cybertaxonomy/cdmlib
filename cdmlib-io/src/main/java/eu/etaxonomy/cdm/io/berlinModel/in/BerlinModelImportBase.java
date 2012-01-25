/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR;
import eu.etaxonomy.cdm.io.common.IPartitionedIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public abstract class BerlinModelImportBase extends CdmImportBase<BerlinModelImportConfigurator, BerlinModelImportState> implements ICdmIO<BerlinModelImportState>, IPartitionedIO<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelImportBase.class);
	
	public static final UUID ID_IN_SOURCE_EXT_UUID = UUID.fromString("23dac094-e793-40a4-bad9-649fc4fcfd44");
	
	public BerlinModelImportBase() {
		super();
	}
	
	protected void doInvoke(BerlinModelImportState state){
			//	String strTeamStore = ICdmIO.TEAM_STORE;
			BerlinModelImportConfigurator config = state.getConfig();
			Source source = config.getSource();
			boolean success = true ;
			
			logger.info("start make " + getPluralString() + " ...");

			String strIdQuery = getIdQuery(state);
			String strRecordQuery = getRecordQuery(config);

			int recordsPerTransaction = config.getRecordsPerTransaction();
			try{
				ResultSetPartitioner partitioner = ResultSetPartitioner.NewInstance(source, strIdQuery, strRecordQuery, recordsPerTransaction);
				while (partitioner.nextPartition()){
					try {
						partitioner.doPartition(this, state);
					} catch (Exception e) {
						e.printStackTrace();
						success = false;
					}
				}
			} catch (SQLException e) {
				logger.error("SQLException:" +  e);
				state.setUnsuccessfull();
			}
	
			logger.info("end make " + getPluralString() + " ... " + getSuccessString(success));
			if (success == false){
				state.setUnsuccessfull();
			}
			return;
	}

	
	/**
	 * @return
	 */
	protected abstract String getRecordQuery(BerlinModelImportConfigurator config);

	/**
	 * @return
	 */
	protected String getIdQuery(BerlinModelImportState state){
		String result = " SELECT " + getTableName() + "id FROM " + getTableName();
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getPluralString()
	 */
	public abstract String getPluralString();

	/**
	 * @return
	 */
	protected abstract String getTableName();
	
	protected boolean doIdCreatedUpdatedNotes(BerlinModelImportState state, DescriptionElementBase descriptionElement, ResultSet rs, String id, String namespace) throws SQLException{
		boolean success = true;
		//id
		success &= ImportHelper.setOriginalSource(descriptionElement, state.getConfig().getSourceReference(), id, namespace);
		//createdUpdateNotes
		success &= doCreatedUpdatedNotes(state, descriptionElement, rs);
		return success;
	}
	
	protected boolean doIdCreatedUpdatedNotes(BerlinModelImportState state, IdentifiableEntity identifiableEntity, ResultSet rs, long id, String namespace, boolean excludeUpdated)	
				throws SQLException{
		boolean success = true;
		//id
		success &= ImportHelper.setOriginalSource(identifiableEntity, state.getConfig().getSourceReference(), id, namespace);
		//createdUpdateNotes
		success &= doCreatedUpdatedNotes(state, identifiableEntity, rs, excludeUpdated);
		return success;
	}

	
	protected boolean doIdCreatedUpdatedNotes(BerlinModelImportState state, IdentifiableEntity identifiableEntity, ResultSet rs, long id, String namespace)
			throws SQLException{
		boolean excludeUpdated = false;
		return doIdCreatedUpdatedNotes(state, identifiableEntity, rs, id, namespace, excludeUpdated);
	}
	
	protected boolean doCreatedUpdatedNotes(BerlinModelImportState state, AnnotatableEntity annotatableEntity, ResultSet rs)
			throws SQLException{
		boolean excludeUpdated = false;
		return doCreatedUpdatedNotes(state, annotatableEntity, rs, excludeUpdated);
	}
	
	protected boolean doCreatedUpdatedNotes(BerlinModelImportState state, AnnotatableEntity annotatableEntity, ResultSet rs, boolean excludeUpdated)
			throws SQLException{

		BerlinModelImportConfigurator config = state.getConfig();
		Object createdWhen = rs.getObject("Created_When");
		String createdWho = rs.getString("Created_Who");
		createdWho = handleHieraciumPilosella(createdWho);
		Object updatedWhen = null;
		String updatedWho = null;
		if (excludeUpdated == false){
			try {
				updatedWhen = rs.getObject("Updated_When");
				updatedWho = rs.getString("Updated_who");
			} catch (SQLException e) {
				//Table "Name" has no updated when/who
			}
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
		doNotes(annotatableEntity, notes);
		return success;
	}

	/**
	 * Adds a note to the annotatable entity.
	 * Nothing happens if annotatableEntity is <code>null</code> or notes is empty or <code>null</code>.
	 * @param annotatableEntity
	 * @param notes
	 */
	protected void doNotes(AnnotatableEntity annotatableEntity, String notes) {
		if (CdmUtils.isNotEmpty(notes) && annotatableEntity != null ){
			String notesString = String.valueOf(notes);
			if (notesString.length() > 65530 ){
				notesString = notesString.substring(0, 65530) + "...";
				logger.warn("Notes string is longer than 65530 and was truncated: " + annotatableEntity);
			}
			Annotation notesAnnotation = Annotation.NewInstance(notesString, Language.DEFAULT());
			//notesAnnotation.setAnnotationType(AnnotationType.EDITORIAL());
			//notes.setCommentator(bmiConfig.getCommentator());
			annotatableEntity.addAnnotation(notesAnnotation);
		}
	}
	
	/**
	 * Special usecase for EDITWP6 import where in the createdWho field the original ID is stored
	 * @param createdWho
	 * @return
	 */
	private String handleHieraciumPilosella(String createdWho) {
		String result = createdWho;
		if (result == null){
			return null;
		}else if (result.startsWith("Hieracium_Pilosella import from EM")){
			return "Hieracium_Pilosella import from EM";
		}else{
			return result;
		}
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
	

	/**
	 * 	Searches first in the detail maps then in the ref maps for a reference.
	 *  Returns the reference as soon as it finds it in one of the map, according
	 *  to the order of the map.
	 *  If nomRefDetailFk is <code>null</code> no search on detail maps is performed.
	 *  If one of the maps is <code>null</code> no search on the according map is
	 *  performed. <BR>
	 *  You may define the order of search by the order you pass the maps but
	 *  make sure to always pass the detail maps first.
	 * @param firstDetailMap
	 * @param secondDetailMap
	 * @param firstRefMap
	 * @param secondRefMap
	 * @param nomRefDetailFk
	 * @param nomRefFk
	 * @return
	 */
	protected Reference getReferenceFromMaps(
			Map<String, Reference> firstDetailMap,
			Map<String, Reference> secondDetailMap, 
			Map<String, Reference> firstRefMap,
			Map<String, Reference> secondRefMap,
			String nomRefDetailFk,
			String nomRefFk) {
		Reference ref = null;
		ref = getReferenceDetailFromMaps(firstDetailMap, secondDetailMap, nomRefDetailFk);
		if (ref == null){
			ref = getReferenceOnlyFromMaps(firstRefMap, secondRefMap, nomRefFk);
		}
		return ref;
	}
	
	/**
	 * As getReferenceFromMaps but search is performed only on references, not on
	 * detail maps.
	 * @param firstRefMap
	 * @param secondRefMap
	 * @param nomRefFk
	 * @return
	 */
	protected Reference getReferenceOnlyFromMaps(
			Map<String, Reference> firstRefMap,
			Map<String, Reference> secondRefMap,
			String nomRefFk) {
		Reference ref = null;
		if (firstRefMap != null){
			ref = firstRefMap.get(nomRefFk);
		}else{
			logger.warn("First reference map does not exist");
		}
		if (ref == null){
			if (secondRefMap != null){
				ref = secondRefMap.get(nomRefFk);
			}else{
				logger.warn("Second reference map does not exist");		
			}
		}
		return ref;
	}

	/**
	 * Searches for a reference in the first detail map. If it does not exist it 
	 * searches in the second detail map. Returns null if it does not exist in any map.
	 * A map may be <code>null</code> to avoid search on this map.
	 * @param secondDetailMap 
	 * @param firstDetailMap 
	 * @param nomRefDetailFk 
	 * @return
	 */
	private Reference getReferenceDetailFromMaps(Map<String, Reference> firstDetailMap, Map<String, Reference> secondDetailMap, String nomRefDetailFk) {
		Reference result = null;
		if (nomRefDetailFk != null){
			//get ref
			if (firstDetailMap != null){
				result = firstDetailMap.get(nomRefDetailFk);
			}
			if (result == null && secondDetailMap != null){
				result = secondDetailMap.get(nomRefDetailFk);
			}
		}
		return result;
	}

	
}
