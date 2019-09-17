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
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * This class either retrieves a defined Term from the database or creates and saves it in the database.
 * This is done by first checking the according transformer for term attributes map to an existing
 * CDM term (term that is available by static methods in the term classes), if not it tries to retrieve
 * it via its uuid available through the transformer, if not retrievable it tries to retrieve from the
 * database via the original source, if not retrievable it creates the new and stores it in the database.<BR>
 * Subclasses may decide if they implement a function to use the term immediately via
 * {@link #handleTermWithObject(VersionableEntity, DefinedTermBase)}
 *
 * @author a.mueller
 * @since 12.05.2009
 * @param <TERM>The class of the term to be created or retrieved.
 * @param <TERMED>The class of the object the term is added to.
 * @param <STATE>The class of the import state that is available during import.
 */
public abstract class DbImportDefinedTermCreationMapperBase<TERM extends DefinedTermBase, TERMED extends VersionableEntity, STATE extends DbImportStateBase<?,?>>
        extends DbImportObjectCreationMapperBase<TERMED, STATE>  {

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

	private void makeUuid(ResultSet rs, TERM definedTerm) throws SQLException {
		UUID uuid = getUuidFromTransformer(rs);
		if (uuid != null){
			definedTerm.setUuid(uuid);
		}
	}

	protected abstract TERM createDefinedTerm(ResultSet rs) throws SQLException;

	/**
	 * Returns the term if it is available via the transformer, or via the state (uuidMap or relatedObject)
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

	protected TERM getTermByIdentifier(ResultSet rs) throws SQLException {
		//get object from stat.featur map
		String key = getKeyString(rs);
		if (key == null){
			return null;
		}
		TERM term = (TERM)getState().getRelatedObject(objectToCreateNamespace, key);
		return term;
	}

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
	 */
	protected abstract void saveTermToState(TERM term);

	protected abstract TERM getTermFromState(UUID uuid);

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

	protected abstract UUID getUuidFromTransformer(String key, IInputTransformer transformer) throws UndefinedTransformerMethodException;

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

	protected abstract TERM getTermFromTransformer(String key, IInputTransformer transformer) throws UndefinedTransformerMethodException;

	protected String getKeyString(ResultSet rs) throws SQLException {
		Object oKey = rs.getString(dbIdAttribute);
		if (oKey == null){
			return null;
		}
		String key = String.valueOf(oKey);
		key = key.trim();
		return key;
	}

	@Override
	protected TERMED doInvoke(ResultSet rs, TERMED createdObject)
			throws SQLException {
		return createdObject; //not needed because invoke is implemented separately
	}

	@Override
	protected TERMED createObject(ResultSet rs) throws SQLException {
		logger.warn("Never should read this");
		return null; //not needed, object is created in createDefineTerm(rs)
	}
}
