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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
//TODO remove ANNOTATABLE by ISourcable (but this is not CDMBase yet therefore not trivial
public abstract class DbImportObjectCreationMapperBase<CREATE extends VersionableEntity, STATE extends DbImportStateBase<?,?>> extends DbImportMultiAttributeMapperBase<CREATE, STATE>  {
	private static final Logger logger = Logger.getLogger(DbImportObjectCreationMapperBase.class);


//******************************* ATTRIBUTES ***************************************/
	protected String dbIdAttribute;
	//TODO get standard namespace from mappingImport
	protected String objectToCreateNamespace;


//********************************* CONSTRUCTOR ****************************************/
	/**
	 * @param mappingImport
	 */
	protected DbImportObjectCreationMapperBase(String dbIdAttribute, String objectToCreateNamespace) {
		super();
		//TODO make it a single attribute mapper
		this.dbIdAttribute = dbIdAttribute;
		this.objectToCreateNamespace = objectToCreateNamespace;
	}

//************************************ METHODS *******************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
    public CREATE invoke(ResultSet rs, CREATE noObject) throws SQLException {
		CREATE result = createObject(rs);
		result = doInvoke(rs, result);
		addOriginalSource(rs, result);
		return result;
	}

	/**
	 * @param rs
	 * @param result
	 * @throws SQLException
	 */
	protected abstract CREATE doInvoke(ResultSet rs, CREATE createdObject) throws SQLException;

	/**
	 * This method creates the object to be created. It needs to be implemented by the concrete classes.
	 * E.g. if you have a TaxonNameCreation class which inherits from this class you need to implement
	 * createObject by creating an empty taxon name.
	 * @param rs The result set
	 * @return The object to be created
	 * @throws SQLException
	 */
	protected abstract CREATE createObject(ResultSet rs) throws SQLException;

	/**
	 * TODO also implemented in CdmImportBase (reduce redundance)
	 * @throws SQLException
	 */
	public void addOriginalSource(ResultSet rs, CREATE cdmBase) throws SQLException {
		if (cdmBase instanceof ISourceable ){
			if (StringUtils.isBlank(dbIdAttribute)){
				return;
			}
			IOriginalSource source;
			ISourceable sourceable = (ISourceable)cdmBase;
			Object id = rs.getObject(dbIdAttribute);
			String strId = String.valueOf(id);
			String idNamespace = this.objectToCreateNamespace;

			Reference citation = getState().getTransactionalSourceReference();

			String microCitation = null;
			if (cdmBase instanceof IdentifiableEntity){
				source = IdentifiableSource.NewDataImportInstance(strId, idNamespace, citation);
			}else if (cdmBase instanceof DescriptionElementBase){
				source = DescriptionElementSource.NewDataImportInstance(strId, idNamespace, citation);
			}else{
				logger.warn("ISourceable not beeing identifiable entities or description element base are not yet supported. CdmBase is of type " + cdmBase.getClass().getSimpleName() + ". Original source not added.");
				return;
			}
			sourceable.addSource(source);
		}
	}




	/**
	 * Returns the transformer from the configuration
	 * @return
	 */
	protected IInputTransformer getTransformer(){
		return getState().getTransformer();
	}

}
