/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public abstract class BerlinModelImportBase extends CdmIoBase<IImportConfigurator> implements ICdmIO<IImportConfigurator> {
	private static final Logger logger = Logger.getLogger(BerlinModelImportBase.class);
	
	public BerlinModelImportBase() {
		super();
	}
	
	protected abstract boolean doInvoke(BerlinModelImportState<BerlinModelImportConfigurator> state);

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){ 
		BerlinModelImportState<BerlinModelImportConfigurator> state = ((BerlinModelImportConfigurator)config).getState();
		state.setConfig((BerlinModelImportConfigurator)config);
		return doInvoke(state);
	}
	
	protected boolean doIdCreatedUpdatedNotes(IImportConfigurator bmiConfig, IdentifiableEntity identifiableEntity, ResultSet rs, long id, String namespace)
			throws SQLException{
		boolean success = true;
		//id
		success &= ImportHelper.setOriginalSource(identifiableEntity, bmiConfig.getSourceReference(), id, namespace);
		//createdUpdateNotes
		success &= doCreatedUpdatedNotes(bmiConfig, identifiableEntity, rs, namespace);
		return success;
	}
	
	
	protected boolean doCreatedUpdatedNotes(IImportConfigurator bmiConfig, AnnotatableEntity annotatableEntity, ResultSet rs, String namespace)
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
		
		//Created When, Who, Updated When Who
		String createdAnnotationString = "Berlin Model record was created By: " + String.valueOf(createdWho) + " (" + String.valueOf(createdWhen) + ") ";
		if (updatedWhen != null && updatedWho != null){
			createdAnnotationString += " and updated By: " + String.valueOf(updatedWho) + " (" + String.valueOf(updatedWhen) + ")";
		}
		Annotation annotation = Annotation.NewInstance(createdAnnotationString, Language.ENGLISH());
		annotation.setCommentator(bmiConfig.getCommentator());
		annotation.setAnnotationType(AnnotationType.TECHNICAL());
		annotatableEntity.addAnnotation(annotation);
		
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
	
}
