/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.mueller
 * @since 20.03.2008
 */
public abstract class DbImportBase<STATE extends DbImportStateBase<CONFIG, STATE>, CONFIG extends DbImportConfiguratorBase<STATE>>
            extends CdmImportBase<CONFIG, STATE>
            implements ICdmIO<STATE>, IPartitionedIO<STATE> {

    private static final long serialVersionUID = 5539446566014467398L;
    private static final Logger logger = LogManager.getLogger();

	private String dbTableName ;
	private String pluralString;

	public DbImportBase(String tableName, String pluralString) {
		super();
		this.dbTableName = tableName;
		this.pluralString = pluralString;
	}

	@Override
    protected void doInvoke(STATE state){
			//	String strTeamStore = ICdmIO.TEAM_STORE;
			CONFIG config = state.getConfig();
			Source source = config.getSource();
			boolean success = true ;

			logger.info("start make " + getPluralString() + " ...");

			String strIdQuery = getIdQuery(state);
			String strRecordQuery = getRecordQuery(config);

			int recordsPerTransaction = config.getRecordsPerTransaction();
			try{
				ResultSetPartitioner<STATE> partitioner = ResultSetPartitioner.NewInstance(source, strIdQuery, strRecordQuery, recordsPerTransaction);
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

	protected abstract String getRecordQuery(CONFIG config);

	protected abstract String getIdQuery(STATE state);

	protected String getTableName() {
		return dbTableName;
	}

	@Override
    public String getPluralString() {
		return pluralString;
	}

	protected boolean doId(STATE state, ISourceable sourceable, long id, String namespace) {
		return ImportHelper.setOriginalSource(sourceable, state.getTransactionalSourceReference(), id, namespace);
	}

	protected boolean doId(STATE state, ISourceable sourceable, String id, String namespace) {
		return ImportHelper.setOriginalSource(sourceable, state.getTransactionalSourceReference(), id, namespace);
	}

	/**
	 * Adds a note to the annotatable entity.
	 * Nothing happens if annotatableEntity is <code>null</code> or notes is empty or <code>null</code>.
	 * @param annotatableEntity
	 * @param notes
	 */
	protected Annotation doNotes(AnnotatableEntity annotatableEntity, String notes) {
		if (isNotBlank(notes) && annotatableEntity != null ){
			String notesString = String.valueOf(notes);
			if (notesString.length() > 65530 ){
				notesString = notesString.substring(0, 65530) + "...";
				logger.warn("Notes string is longer than 65530 and was truncated: " + annotatableEntity);
			}
			Annotation notesAnnotation = Annotation.NewInstance(notesString, Language.DEFAULT());
			//notesAnnotation.setAnnotationType(AnnotationType.EDITORIAL());
			//notes.setCommentator(bmiConfig.getCommentator());
			annotatableEntity.addAnnotation(notesAnnotation);
			return notesAnnotation;
		}
		return null;
	}

	protected User getUser(STATE state, String userString){
		if (StringUtils.isBlank(userString)){
			return null;
		}
		userString = userString.trim();

		User user = state.getUser(userString);
		if (user == null){
			user = getTransformedUser(userString,state);
		}
		if (user == null){
		    user = getExistingUser(state, userString, user);
		}
		if (user == null){
			user = makeNewUser(userString, state);
		}
		if (user == null){
			throw new RuntimeException ("User must not be null");
		}
		return user;
	}

    protected User getExistingUser(STATE state, String userString, User user) {
        List<User> list = getUserService().listByUsername(userString, MatchMode.EXACT, null, null, null, null, null);
        if (!list.isEmpty()){
            if (list.size()>1){
                logger.warn("More than 1 user with name " + userString + " exists");
            }
            user = list.get(0);
            state.putUser(userString, user);
        }
        return user;
    }

	private User getTransformedUser(String userString, STATE state){
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

	private User makeNewUser(String userString, STATE state){
		String pwd = getRandomPassword();
		User user = User.NewInstance(userString, pwd);
		state.putUser(userString, user);
		getUserService().save(user);
		logger.info("Added new user: " + userString);
		return user;
	}

	private String getRandomPassword(){
		String result = UUID.randomUUID().toString();
		return result;
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
     * Reads a uuid field from the result set and adds its value to the idSet.
     */
    protected void handleForeignUuidKey(ResultSet rs, Set<UUID> idSet, String attributeName)
            throws SQLException {
        String idObj = rs.getString(attributeName);
        if (idObj != null){
            UUID uuid  = UUID.fromString(idObj);
            idSet.add(uuid);
        }
    }

	/**
	 * Returns <code>true</code> if <code>i</code> is a multiple of recordsPerTransaction
	 */
	protected boolean loopNeedsHandling(int i, int recordsPerLoop) {
		startTransaction();
		return (i % recordsPerLoop) == 0;
		}

	protected void doLogPerLoop(int count, int recordsPerLog, String pluralString){
		if ((count % recordsPerLog ) == 0 && count!= 0 ){ logger.info(pluralString + " handled: " + (count));}
	}
}
