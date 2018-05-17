/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * This class either retrieves a defined Term from the database or creates and saves it in the database.
 * This is done by first checking the according transformer for term attributes map to an existing 
 * CDM term (term that is available by static methods in the term classes), if not it tries to retrieve
 * it via its uuid available through the transformer, if not retrievable it tries to retrieve from the
 * database via the original source, if not retrievable it creates the new and stores it in the database.
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 * @param <TERM>The class of the term to be created or retrieved.
 * @param <TERMED>The class of the object the term is added to.
 * @param <STATE>The class of the import state thate is available during import.
 */
public abstract class DbImportDefinedTermCreationMapperBase<TERM extends DefinedTermBase, TERMED extends VersionableEntity, STATE extends DbImportStateBase<?,?>> extends DbImportObjectCreationMapperBase<TERMED, STATE>  {
	private static final Logger logger = Logger.getLogger(DbImportDefinedTermCreationMapperBase.class);
	

//******************************* ATTRIBUTES ***************************************/
	protected String dbTermAttribute;
	protected String dbLabelAttribute;
	protected String dbLabelAbbrevAttribute;
	
//********************************* CONSTRUCTOR ****************************************/
	/**
	 * @param mappingImport
	 */
	protected DbImportDefinedTermCreationMapperBase(String dbIdAttribute, String termNamespace, String dbTermAttribute, String dbLabelAttribute, String dbLabelAbbrevAttribute) {
		super(dbIdAttribute, termNamespace);
		this.dbTermAttribute = dbTermAttribute;
		this.dbLabelAttribute = dbLabelAttribute;
		this.dbLabelAbbrevAttribute = dbLabelAbbrevAttribute;
	}

//************************************ METHODS *******************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.VersionableEntity)
	 */
	@Override
	public TERMED invoke(ResultSet rs, TERMED noObject) throws SQLException {
		String key = getKeyString(rs);
		if (key != null){
			TERM definedTerm = getDefinedTermIfExist(rs);
			if (definedTerm == null){
				definedTerm = createDefinedTerm(rs);
				Reference citation = null;
				getState().getCurrentIO().addOriginalSource(rs, definedTerm, dbIdAttribute, objectToCreateNamespace, citation);
				
				UUID transformerUuid = getUuidFromTransformer(rs);
				if (transformerUuid != null){
					definedTerm.setUuid(transformerUuid);
				}
				getState().getCurrentIO().getTermService().save(definedTerm);
				saveTermToState(definedTerm);
				if (transformerUuid == null){
					getState().addRelatedObject(objectToCreateNamespace, getKeyString(rs), definedTerm);
				}
			}
		}
		return noObject;
	}

	/**
	 * @param rs
	 * @param definedTerm
	 * @throws SQLException
	 */
	private void makeUuid(ResultSet rs, TERM definedTerm) throws SQLException {
		UUID uuid = getUuidFromTransformer(rs);
		if (uuid != null){
			definedTerm.setUuid(uuid);
		}
	}

	/**
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	protected abstract TERM createDefinedTerm(ResultSet rs) throws SQLException;

	/**
	 * Returns the term if it is available via the transformer, or via the state (uuidMap or relatedObject)
	 * @return
	 * @throws SQLException 
	 */
	private TERM getDefinedTermIfExist(ResultSet rs) throws SQLException {
		//get object from transformer, return if not null
		TERM definedTerm = getTermFromTransformer(rs);
		if (definedTerm == null){
			//if null get uuid from transformer
			UUID uuidTerm = getUuidFromTransformer(rs);
			if (uuidTerm != null){
				definedTerm = getTermByUuid(uuidTerm, rs);
			}else{
				definedTerm = getTermByIdentifier(rs);
			}
		}
		return definedTerm;
	}

	/**
	 * @return
	 * @throws SQLException 
	 */
	protected TERM getTermByIdentifier(ResultSet rs) throws SQLException {
		//get object from stat.featur map
		String key = getKeyString(rs);
		if (key == null){
			return null;
		}
		TERM term = (TERM)getState().getRelatedObject(objectToCreateNamespace, key);
		return term;
	}


	/**
	 * @param uuidTerm
	 * @return
	 * @throws SQLException 
	 */
	protected TERM getTermByUuid(UUID uuidTerm, ResultSet rs) throws SQLException{
		TERM term = getTermFromState(uuidTerm);
		if (term == null){
			term = (TERM)getState().getCurrentIO().getTermService().find(uuidTerm);
			if (term != null){
				saveTermToState(term);
			}
		}
		return term;
	}



	/**
	 * Saves the defined term to the state
	 * @param rs
	 */
	protected abstract void saveTermToState(TERM term);

	/**
	 * @param rs
	 * @return
	 */
	protected abstract TERM getTermFromState(UUID uuid);

	
	/**
	 * @return
	 * @throws SQLException 
	 */
	protected UUID getUuidFromTransformer(ResultSet rs) throws SQLException{
		IInputTransformer transformer = getTransformer();
		String key = getKeyString(rs);
		UUID uuid;
		try {
			uuid = getUuidFromTransformer(key, transformer);
		} catch (UndefinedTransformerMethodException e) {
			logger.warn(e.getMessage());
			return null;
		}
		return uuid;
	}

	/**
	 * @return
	 * @throws UndefinedTransformerMethodException 
	 */
	protected abstract UUID getUuidFromTransformer(String key, IInputTransformer transformer) throws UndefinedTransformerMethodException;

	
	/**
	 * @param rs 
	 * @return
	 * @throws SQLException 
	 */
	protected TERM getTermFromTransformer(ResultSet rs) throws SQLException{
		IInputTransformer transformer = getTransformer();
		String key = getKeyString(rs);
		TERM term;
		try {
			term = getTermFromTransformer(key, transformer);
		} catch (UndefinedTransformerMethodException e) {
			logger.warn(e.getMessage());
			return null;
		}
		return term;
	}


	/**
	 * @param key
	 * @param transformer
	 * @return
	 * @throws UndefinedTransformerMethodException 
	 */
	protected abstract TERM getTermFromTransformer(String key, IInputTransformer transformer) throws UndefinedTransformerMethodException;

	/**
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected String getKeyString(ResultSet rs) throws SQLException {
		Object oKey = rs.getString(dbIdAttribute);
		if (oKey == null){
			return null;
		}
		String key = String.valueOf(oKey);
		key = key.trim();
		return key;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#doInvoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.VersionableEntity)
	 */
	@Override
	protected TERMED doInvoke(ResultSet rs, TERMED createdObject)
			throws SQLException {
		return createdObject; //not needed because invoke is implemented separately
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#createObject(java.sql.ResultSet)
	 */
	@Override
	protected TERMED createObject(ResultSet rs) throws SQLException {
		logger.warn("Never should read this");
		return null; //not needed, object is created in createDefineTerm(rs)
	}

	
	
}
